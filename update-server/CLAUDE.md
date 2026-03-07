# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Local dev
pip install -r requirements.txt
python app.py                    # runs on http://localhost:5000

# Docker
docker build -t lottery-update-server .
docker run -d -p 5000:5000 \
  -v $(pwd)/changelog.json:/app/changelog.json \
  -v $(pwd)/releases:/app/releases \
  --name update-server lottery-update-server

# Systemd (Ubuntu, after setup)
sudo systemctl start lottery-update
sudo systemctl status lottery-update
```

## Architecture

Single-file Flask app (`app.py`). **`changelog.json` is the only data source** — no database, no migrations.

### API Endpoints

| Route | Purpose |
|---|---|
| `GET /api/v1/version` | Returns **first entry only** from `changelog.json` — used by App for auto-update |
| `GET /api/v1/changelog` | Returns full array — used by App's About page |
| `GET /releases/<filename>` | Serves APK files from `releases/` directory |
| `GET /health` | Health check |

### `changelog.json` Schema

Array sorted **newest-first**. Each entry:
- `versionCode` (int) — used for numeric comparison
- `versionName` (string) — display string, e.g. `"1.0.2"`
- `releaseDate` (string) — `"YYYY-MM-DD"`
- `releaseNotes` (string) — supports `\n`
- `downloadUrl` (string) — **only needed on the first/latest entry**
- `minVersionCode` (int) — **only needed on the first/latest entry**; App may force-update if current < this

### Logging

`RotatingFileHandler` writes to `logs/app.log` (2MB × 5 backups, UTF-8). Every request and response is logged with client IP, method, path, args, user-agent, and status code.

## Releasing a New Version

1. Prepend a new entry to `changelog.json` with all fields including `downloadUrl` and `minVersionCode`.
2. If hosting APK locally, place the file in `releases/`.
3. Docker: `docker restart update-server`. Systemd: no restart needed (file is read on every request).
