#!/usr/bin/env python3
"""将鸿蒙 Release .app 包上传至华为 AppGallery Connect（AGC）草稿版本。

仅上传并绑定软件包，不自动提交审核，方便在 AGC 控制台核对素材、说明与测试账号后再手动提审。
遵循华为 AGC HarmonyOS Publishing API 流程：
  1. POST /api/oauth2/v1/token              获取 API Client access_token
  2. GET  /api/publish/v2/upload-url/for-obs 获取华为云 OBS 预签名上传地址与 objectId
  3. PUT  <urlInfo.url>                     直传 .app 二进制包至 OBS
  4. PUT  /api/publish/v3/app-package-info  将 objectId 绑定至 HarmonyOS 应用软件包

所需环境变量或命令行参数：
  - AGC_CLIENT_ID: AGC Connect API（开发者级）Client ID
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
        if not any(k.lower() == "content-type" for k in req_headers):
            req_headers["Content-Type"] = "application/json"
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


def build_obs_request_headers(headers: dict[str, str], file_size: int) -> dict[str, str]:
    """完整保留华为 OBS 预签名返回的请求头，仅在缺失 Content-Length 时补充（避免破坏 SigV4 签名）。"""
    req_headers = {str(k): str(v) for k, v in headers.items()}
    lower_keys = {k.lower() for k in req_headers}
    if "content-length" not in lower_keys:
        req_headers["Content-Length"] = str(file_size)
    return req_headers


def upload_binary_to_obs(
    upload_url: str,
    headers: dict[str, str],
    file_path: Path,
    method: str = "PUT",
) -> None:
    req_headers = build_obs_request_headers(headers, file_path.stat().st_size)
    with file_path.open("rb") as stream:
        request = urllib.request.Request(
            upload_url,
            data=stream,
            headers=req_headers,
            method=method.upper(),
        )
        try:
            with urllib.request.urlopen(request, timeout=600) as response:
                if response.status not in (200, 201, 204):
                    body = response.read().decode("utf-8", errors="replace")
                    raise RuntimeError(f"OBS 上传返回异常状态码 {response.status}: {body}")
        except urllib.error.HTTPError as exc:
            body = exc.read().decode("utf-8", errors="replace")
            raise RuntimeError(f"OBS 上传失败 HTTP {exc.code}: {body}") from exc


def update_app_package_info(
    token: str,
    client_id: str,
    app_id: str,
    file_name: str,
    object_id: str,
    release_type: int,
) -> dict:
    """调用 HarmonyOS Publishing API v3 将已上传至 OBS 的 .app 软件包绑定到应用草稿。"""
    query_params: dict[str, str] = {"appId": app_id}
    if release_type:
        query_params["releaseType"] = str(release_type)
    query = urllib.parse.urlencode(query_params)
    url = f"{AGC_BASE_URL}/api/publish/v3/app-package-info?{query}"
    result = http_json(
        "PUT",
        url,
        headers={
            "Authorization": f"Bearer {token}",
            "client_id": client_id,
            "Content-Type": "application/json",
        },
        payload={
            "fileName": file_name,
            "objectId": object_id,
        },
    )
    ret = result.get("ret", {})
    if ret and ret.get("code") not in (0, "0", None):
        raise RuntimeError(f"绑定 HarmonyOS 软件包信息失败: {result}")
    return result


def parse_args(argv: list[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Upload HarmonyOS .app package to Huawei AGC")
    parser.add_argument("--file", required=True, help="Path to signed HarmonyOS .app file")
    parser.add_argument("--client-id", default=os.environ.get("AGC_CLIENT_ID", ""))
    parser.add_argument("--client-secret", default=os.environ.get("AGC_CLIENT_SECRET", ""))
    parser.add_argument("--app-id", default=os.environ.get("AGC_APP_ID", ""))
    parser.add_argument("--release-type", type=int, default=1, help="1=正式发布(默认), 3=开放式测试")
    return parser.parse_args(argv)


def append_step_summary(lines: list[str]) -> None:
    summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
    if not summary_path:
        return
    with Path(summary_path).open("a", encoding="utf-8") as handle:
        handle.write("\n".join(lines) + "\n")


def main(argv: list[str] | None = None) -> int:
    args = parse_args(argv)
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
    upload_method = str(url_info.get("method") or "PUT").upper()
    obs_headers = url_info.get("headers") or obs_info.get("headers") or {}
    object_id = url_info.get("objectId") or obs_info.get("objectId") or ""
    if not upload_url:
        raise RuntimeError(f"响应中未包含 OBS 上传地址: {obs_info}")
    if not object_id:
        raise RuntimeError(f"响应中未包含 objectId: {obs_info}")

    print(f"2/3 正在通过 {upload_method} 上传 .app 到华为云 OBS...")
    upload_binary_to_obs(upload_url, obs_headers, file_path, method=upload_method)

    print("3/3 正在将安装包绑定到 AGC HarmonyOS 草稿版本...")
    update_result = update_app_package_info(
        token=token,
        client_id=args.client_id,
        app_id=args.app_id,
        file_name=file_path.name,
        object_id=object_id,
        release_type=args.release_type,
    )
    package_id = update_result.get("packageId") or update_result.get("pkgId") or ""
    print(f"上传完成: {json.dumps(update_result, ensure_ascii=False)}")
    append_step_summary(
        [
            "### 华为 AppGallery Connect 上传结果",
            f"- 发布类型: `{args.release_type}`",
            f"- Object ID: `{object_id}`",
            *( [f"- Package ID: `{package_id}`"] if package_id else [] ),
        ]
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
