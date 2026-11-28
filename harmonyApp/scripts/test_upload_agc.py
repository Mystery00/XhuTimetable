from __future__ import annotations

import json
import sys
import tempfile
import unittest
from pathlib import Path
from unittest import mock

sys.path.insert(0, str(Path(__file__).resolve().parent))
import upload_agc


class UploadAgcTests(unittest.TestCase):
    def test_build_obs_request_headers_preserves_signed_headers_without_duplicates(self) -> None:
        signed = {
            "Authorization": "AWS4-HMAC-SHA256 ...",
            "x-amz-date": "20261128T060000Z",
            "content-type": "application/octet-stream",
        }
        headers = upload_agc.build_obs_request_headers(signed, file_size=12345)
        self.assertEqual(headers["content-type"], "application/octet-stream")
        self.assertNotIn("Content-Type", headers)
        self.assertEqual(headers["Content-Length"], "12345")

        signed_with_len = {"content-length": "999", "Host": "obs.example.com"}
        headers2 = upload_agc.build_obs_request_headers(signed_with_len, file_size=12345)
        self.assertEqual(headers2["content-length"], "999")
        self.assertNotIn("Content-Length", headers2)

    def test_main_calls_v2_obs_and_v3_app_package_info(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            app_file = Path(tmp) / "XhuTimetable-1.7.0-894-harmony-release.app"
            app_file.write_bytes(b"fake-harmony-app-binary")
            summary_file = Path(tmp) / "summary.md"

            calls: list[tuple[str, str, dict | None, dict | None]] = []

            def fake_http_json(method, url, headers=None, payload=None, timeout=60):
                calls.append((method, url, headers, payload))
                if url.endswith("/api/oauth2/v1/token"):
                    return {"access_token": "tok-123"}
                if "/api/publish/v2/upload-url/for-obs?" in url:
                    return {
                        "ret": {"code": 0, "msg": "success"},
                        "urlInfo": {
                            "url": "https://obs.huawei.example/CN/20261128/test.app",
                            "method": "PUT",
                            "objectId": "CN/20261128/test.app",
                            "headers": {
                                "Authorization": "OBS-SIG",
                                "Content-Type": "application/octet-stream",
                            },
                        },
                    }
                if "/api/publish/v3/app-package-info?" in url:
                    return {
                        "ret": {"code": 0, "msg": "success"},
                        "packageId": "157300001",
                    }
                raise AssertionError(f"Unexpected URL: {url}")

            uploaded: list[tuple[str, dict, Path, str]] = []

            def fake_upload(url, headers, path, method="PUT"):
                uploaded.append((url, headers, path, method))

            with (
                mock.patch.object(upload_agc, "http_json", side_effect=fake_http_json),
                mock.patch.object(upload_agc, "upload_binary_to_obs", side_effect=fake_upload),
                mock.patch.dict(
                    upload_agc.os.environ,
                    {"GITHUB_STEP_SUMMARY": str(summary_file)},
                    clear=False,
                ),
            ):
                rc = upload_agc.main(
                    [
                        "--file",
                        str(app_file),
                        "--client-id",
                        "cid-1",
                        "--client-secret",
                        "sec-1",
                        "--app-id",
                        "app-999",
                        "--release-type",
                        "1",
                    ]
                )

            self.assertEqual(rc, 0)
            self.assertEqual(len(calls), 3)
            self.assertEqual(calls[0][0], "POST")
            self.assertEqual(calls[1][0], "GET")
            self.assertIn("appId=app-999", calls[1][1])
            self.assertIn("releaseType=1", calls[1][1])
            self.assertEqual(calls[2][0], "PUT")
            self.assertIn("/api/publish/v3/app-package-info?", calls[2][1])
            self.assertEqual(
                calls[2][3],
                {
                    "fileName": app_file.name,
                    "objectId": "CN/20261128/test.app",
                },
            )
            self.assertEqual(len(uploaded), 1)
            self.assertEqual(uploaded[0][0], "https://obs.huawei.example/CN/20261128/test.app")
            self.assertEqual(uploaded[0][3], "PUT")

            summary_text = summary_file.read_text(encoding="utf-8")
            self.assertIn("157300001", summary_text)
            self.assertIn("CN/20261128/test.app", summary_text)

    def test_missing_object_id_raises(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            app_file = Path(tmp) / "test.app"
            app_file.write_bytes(b"abc")

            def fake_http_json(method, url, headers=None, payload=None, timeout=60):
                if url.endswith("/api/oauth2/v1/token"):
                    return {"access_token": "tok-123"}
                return {
                    "ret": {"code": 0, "msg": "success"},
                    "urlInfo": {"url": "https://obs.example/test.app", "headers": {}},
                }

            with mock.patch.object(upload_agc, "http_json", side_effect=fake_http_json):
                with self.assertRaisesRegex(RuntimeError, "objectId"):
                    upload_agc.main(
                        [
                            "--file",
                            str(app_file),
                            "--client-id",
                            "cid",
                            "--client-secret",
                            "sec",
                            "--app-id",
                            "app",
                        ]
                    )


if __name__ == "__main__":
    unittest.main()
