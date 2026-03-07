# 彩票 App 版本管理服务

部署在 Ubuntu 上。**以历史版本列表为唯一数据源**（`changelog.json`）：每条记录包含发布版本、版本信息、变更记录。Android App **仅请求最新版本**并据此做自动更新。

## 数据源

- **changelog.json**：历史版本数组，**按时间倒序（最新在前）**。每条包含：
  - `versionCode` / `versionName`：发布版本
  - `releaseDate`：发布日期
  - `releaseNotes`：变更记录
  - **仅最新一条**需包含 `downloadUrl`、`minVersionCode`（供 App 更新使用）

## API

- `GET /api/v1/version` — **仅返回最新一条**，供 App 检测并自动更新
- `GET /api/v1/changelog` — 返回完整历史版本列表，供关于页展示
- `GET /releases/<filename>` — 下载 APK（需在 `releases/` 下放置文件）
- `GET /health` — 健康检查

### 版本接口返回示例

```json
{
  "versionCode": 2,
  "versionName": "1.0.1",
  "downloadUrl": "https://your-domain.com/releases/zhongleme-release.apk",
  "releaseNotes": "1. 修复期数计算\n2. 新增自动更新",
  "minVersionCode": 1
}
```

- `versionCode`: 整数，用于比较（当前发布版本）
- `versionName`: 展示用，如 "1.0.1"
- `downloadUrl`: APK 完整下载地址
- `releaseNotes`: 更新说明，支持多行
- `minVersionCode`: 最低支持的 versionCode，低于此版本可提示强制更新

### 更新记录接口（changelog.json 结构）

每条历史版本可包含：`versionCode`、`versionName`、`releaseDate`、`releaseNotes`；**最新一条**另加 `downloadUrl`、`minVersionCode`。

```json
[
  {
    "versionCode": 1,
    "versionName": "1.0.0",
    "releaseDate": "2026-03-07",
    "releaseNotes": "首次发布。\n- 双色球/大乐透选号",
    "downloadUrl": "https://your-domain.com/releases/zhongleme-release.apk",
    "minVersionCode": 1
  }
]
```

## 本地运行

```bash
cd update-server
pip install -r requirements.txt
python app.py
# 访问 http://localhost:5000/api/v1/version
```

## Ubuntu 部署（Docker）

```bash
cd update-server
docker build -t lottery-update-server .
docker run -d -p 5000:5000 -v $(pwd)/changelog.json:/app/changelog.json -v $(pwd)/releases:/app/releases --name update-server lottery-update-server
```

## Ubuntu 部署（systemd + 虚拟环境）

```bash
sudo apt update && sudo apt install -y python3-venv
cd /opt
sudo git clone <your-repo> lottery-app && cd lottery-app/update-server
sudo python3 -m venv venv
sudo ./venv/bin/pip install -r requirements.txt
sudo cp changelog.json changelog.json.bak
```

创建 systemd 服务 `/etc/systemd/system/lottery-update.service`：

```ini
[Unit]
Description=LotteryApp Version Update Server
After=network.target

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/lottery-app/update-server
Environment="PATH=/opt/lottery-app/update-server/venv/bin"
ExecStart=/opt/lottery-app/update-server/venv/bin/gunicorn -b 0.0.0.0:5000 -w 1 --timeout 60 app:app
Restart=always

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable lottery-update
sudo systemctl start lottery-update
```

## 发布新版本

1. 编辑 **changelog.json**：在数组**开头**插入新版本条目，包含 `versionCode`、`versionName`、`releaseDate`、`releaseNotes`，以及 **downloadUrl**、**minVersionCode**（仅最新一条需要）。
2. 若使用 `/releases/` 提供下载，将新 APK 放到 `releases/` 目录。
3. 若用 Docker，重启容器使 `changelog.json` 生效；若用 systemd，每次请求会读文件，无需重启。

## 故障排查

- **App 显示「响应 200」但「失败: unexpected end of stream」**：说明 HTTP 头已返回，但响应体未完整发送。常见原因：  
  - Gunicorn worker 超时被杀死：增大 `--timeout`（如 60）。  
  - 前端反向代理（如 Nginx）缓冲或提前关连接：检查 proxy 配置，确保不截断小响应体。  
  - 网络中断：客户端已做异常捕获与日志，可重试或检查服务器/网络稳定性。
