#!/usr/bin/env python3
"""将鸿蒙 Release .app 包上传至华为 AppGallery Connect（AGC）草稿版本。

仅上传并绑定软件包，不自动提交审核，方便在 AGC 控制台核对素材、说明与测试账号后再手动提审。
所需环境变量或命令行参数：
  - AGC_CLIENT_ID: AGC Connect API Client ID
  - AGC_CLIENT_SECRET: AGC Connect API Client Secret
  - AGC_APP_ID: 鸿蒙应用在 AGC 中的 App ID
  - --file: 待上传的 .app 文件路径
"""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

AGC_BASE_URL = "https://connect-api.cloud.huawei.com"


def sha256_file(file_path: Path) -> str:
    digest = hashlib.sha256()
    with file_path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def http_json(
    method: str,
    url: str,
    headers: dict[str, str] | None = None,
    payload: dict | None = None,
    timeout: int = 60,
) -> dict:
    data = None
    req_headers = dict(headers or {})
    if payload is not None:
        data = json.dumps(payload).encode("utf-8")
        req_headers.setdefault("Content-Type", "application/json")
    request = urllib.request.Request(url, data=data, headers=req_headers, method=method)
    try:
        with urllib.request.urlopen(request, timeout=timeout) as response:
            raw = response.read().decode("utf-8")
            return json.loads(raw) if raw else {}
    except urllib.error.HTTPError as exc:
        body = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"AGC HTTP {exc.code} ({method} {url}): {body}") from exc


def get_access_token(client_id: str, client_secret: str) -> str:
    result = http_json(
        "POST",
        f"{AGC_BASE_URL}/api/oauth2/v1/token",
        payload={
            "grant_type": "client_credentials",
            "client_id": client_id,
            "client_secret": client_secret,
        },
    )
    token = result.get("access_token")
    if not token:
        raise RuntimeError(f"获取 AGC access_token 失败: {result}")
    return token


def get_obs_upload_info(
    token: str,
    client_id: str,
    app_id: str,
    file_name: str,
    content_length: int,
    file_sha256: str,
    release_type: int,
) -> dict:
    query = urllib.parse.urlencode(
        {
            "appId": app_id,
            "fileName": file_name,
            "contentLength": str(content_length),
            "sha256": file_sha256,
            "releaseType": str(release_type),
        }
    )
    url = f"{AGC_BASE_URL}/api/publish/v2/upload-url/for-obs?{query}"
    result = http_json(
        "GET",
        url,
        headers={
            "Authorization": f"Bearer {token}",
            "client_id": client_id,
            "Content-Type": "application/json",
        },
    )
    ret = result.get("ret", {})
    if ret and ret.get("code") not in (0, "0", None):
        raise RuntimeError(f"申请 OBS 上传地址失败: {result}")
    return result


def upload_binary_to_obs(upload_url: str, headers: dict[str, str], file_path: Path) -> None:
    req_headers = {str(k): str(v) for k, v in headers.items()}
    req_headers.setdefault("Content-Type", "application/octet-stream")
    req_headers["Content-Length"] = str(file_path.stat().st_size)
    with file_path.open("rb") as stream:
        request = urllib.request.Request(upload_url, data=stream, headers=req_headers, method="PUT")
        try:
            with urllib.request.urlopen(request, timeout=600) as response:
                if response.status not in (200, 201, 204):
                    body = response.read().decode("utf-8", errors="replace")
                    raise RuntimeError(f"OBS 上传返回异常状态码 {response.status}: {body}")
        except urllib.error.HTTPError as exc:
            body = exc.read().decode("utf-8", errors="replace")
            raise RuntimeError(f"OBS 上传失败 HTTP {exc.code}: {body}") from exc


def update_app_file_info(
    token: str,
    client_id: str,
    app_id: str,
    file_name: str,
    file_dest_url: str,
    release_type: int,
) -> dict:
    query = urllib.parse.urlencode({"appId": app_id, "releaseType": str(release_type)})
    url = f"{AGC_BASE_URL}/api/publish/v2/app-file-info?{query}"
    result = http_json(
        "PUT",
        url,
        headers={
            "Authorization": f"Bearer {token}",
            "client_id": client_id,
            "Content-Type": "application/json",
        },
        payload={
            "fileType": 5,
            "files": [
                {
                    "fileName": file_name,
                    "fileDestUrl": file_dest_url,
                }
            ],
        },
    )
    ret = result.get("ret", {})
    if ret and ret.get("code") not in (0, "0", None):
        raise RuntimeError(f"绑定应用安装包信息失败: {result}")
    return result


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Upload HarmonyOS .app package to Huawei AGC")
    parser.add_argument("--file", required=True, help="Path to signed HarmonyOS .app file")
    parser.add_argument("--client-id", default=os.environ.get("AGC_CLIENT_ID", ""))
    parser.add_argument("--client-secret", default=os.environ.get("AGC_CLIENT_SECRET", ""))
    parser.add_argument("--app-id", default=os.environ.get("AGC_APP_ID", ""))
    parser.add_argument("--release-type", type=int, default=1, help="1=正式发布(默认), 3=开放式测试")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    file_path = Path(args.file).resolve()
    if not file_path.is_file():
        print(f"错误: 找不到待上传文件 {file_path}", file=sys.stderr)
        return 1
    if not args.client_id or not args.client_secret or not args.app_id:
        print("错误: 必须提供 AGC_CLIENT_ID、AGC_CLIENT_SECRET 和 AGC_APP_ID", file=sys.stderr)
        return 1

    file_size = file_path.stat().st_size
    file_sha256 = sha256_file(file_path)
    print(f"准备上传 {file_path.name} ({file_size} bytes, sha256={file_sha256[:12]}...)")

    token = get_access_token(args.client_id, args.client_secret)
    print("1/3 已获取 AGC access_token")

    obs_info = get_obs_upload_info(
        token=token,
        client_id=args.client_id,
        app_id=args.app_id,
        file_name=file_path.name,
        content_length=file_size,
        file_sha256=file_sha256,
        release_type=args.release_type,
    )
    url_info = obs_info.get("urlInfo") or {}
    upload_url = url_info.get("url") or obs_info.get("uploadUrl")
    obs_headers = url_info.get("headers") or obs_info.get("headers") or {}
    object_id = obs_info.get("objectId") or url_info.get("objectId") or ""
    if not upload_url:
        raise RuntimeError(f"响应中未包含上传地址: {obs_info}")

    print("2/3 正在上传 .app 到华为云 OBS...")
    upload_binary_to_obs(upload_url, obs_headers, file_path)

    file_dest_url = object_id or upload_url.split("?", 1)[0]
    print("3/3 正在将安装包绑定到 AGC 草稿版本...")
    update_result = update_app_file_info(
        token=token,
        client_id=args.client_id,
        app_id=args.app_id,
        file_name=file_path.name,
        file_dest_url=file_dest_url,
        release_type=args.release_type,
    )
    print(f"上传完成: {json.dumps(update_result, ensure_ascii=False)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
