FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY app.py changelog.json ./

# 可选：挂载 releases 目录放 APK
RUN mkdir -p /app/releases

EXPOSE 5000
CMD ["gunicorn", "-b", "0.0.0.0:5000", "-w", "1", "app:app"]
