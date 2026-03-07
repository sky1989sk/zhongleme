"""
版本管理 API：以历史版本列表为唯一数据源。
- /api/v1/version：仅返回最新一条，供 App 检测更新并自动更新。
- /api/v1/changelog：返回完整历史，供关于页展示。
- 请求日志：记录客户端信息与 API 调用参数，持久化到 logs/app.log。
"""
import json
import logging
from logging.handlers import RotatingFileHandler
from pathlib import Path

from flask import Flask, jsonify, request, send_from_directory

app = Flask(__name__)

BASE_DIR = Path(__file__).resolve().parent
CHANGELOG_FILE = BASE_DIR / "changelog.json"
RELEASES_DIR = BASE_DIR / "releases"
LOGS_DIR = BASE_DIR / "logs"
LOG_FILE = LOGS_DIR / "app.log"
LOG_MAX_BYTES = 2 * 1024 * 1024  # 2MB
LOG_BACKUP_COUNT = 5


def setup_logging() -> logging.Logger:
    LOGS_DIR.mkdir(parents=True, exist_ok=True)
    logger = logging.getLogger("update_server")
    logger.setLevel(logging.INFO)
    if logger.handlers:
        return logger
    fh = RotatingFileHandler(
        LOG_FILE,
        maxBytes=LOG_MAX_BYTES,
        backupCount=LOG_BACKUP_COUNT,
        encoding="utf-8",
    )
    fh.setFormatter(
        logging.Formatter(
            "%(asctime)s [%(levelname)s] %(message)s",
            datefmt="%Y-%m-%d %H:%M:%S",
        )
    )
    logger.addHandler(fh)
    return logger


log = setup_logging()


@app.before_request
def log_request():
    """记录每个请求的客户端信息与调用参数。"""
    client_ip = request.environ.get("HTTP_X_FORWARDED_FOR") or request.remote_addr or "unknown"
    log.info(
        "REQUEST | client=%s | method=%s | path=%s | args=%s | user_agent=%s",
        client_ip,
        request.method,
        request.path,
        dict(request.args),
        (request.user_agent.string if request.user_agent else "")[:200],
    )


@app.after_request
def log_response(response):
    """请求结束后记录状态码。"""
    client_ip = request.environ.get("HTTP_X_FORWARDED_FOR") or request.remote_addr or "unknown"
    log.info(
        "RESPONSE | client=%s | path=%s | status=%s",
        client_ip,
        request.path,
        response.status_code,
    )
    return response


def load_versions() -> list:
    with open(CHANGELOG_FILE, "r", encoding="utf-8") as f:
        return json.load(f)


@app.route("/api/v1/version", methods=["GET"])
def get_version():
    """仅返回最新版本（列表第一条），供 App 查询并自动更新。"""
    try:
        versions = load_versions()
        if not versions:
            return jsonify({"error": "no versions"}), 404
        latest = versions[0]
        # 保证 App 需要的字段存在
        data = {
            "versionCode": latest.get("versionCode"),
            "versionName": latest.get("versionName", ""),
            "downloadUrl": latest.get("downloadUrl", ""),
            "releaseNotes": latest.get("releaseNotes", ""),
            "minVersionCode": latest.get("minVersionCode", 1),
        }
        return jsonify(data)
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route("/api/v1/changelog", methods=["GET"])
def get_changelog():
    """返回完整历史版本记录（发布版本、版本信息、变更记录），供关于页展示。"""
    try:
        versions = load_versions()
        return jsonify({"versions": versions})
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route("/releases/<path:filename>", methods=["GET"])
def download_apk(filename):
    """提供 APK 文件下载（可选，若 downloadUrl 指向本机则用此路由）。"""
    if not RELEASES_DIR.is_dir():
        return jsonify({"error": "releases not configured"}), 404
    return send_from_directory(RELEASES_DIR, filename, as_attachment=True)


@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"})


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
