# LotteryApp / 彩票选号

[English](#english) | [中文](#中文)

---

## English

### Introduction

LotteryApp is an Android app for generating and managing lottery number combinations for **双色球 (SSQ)** and **超级大乐透 (DLT)**. It supports multiple play modes, generation strategies, and persists history locally with correct issue numbers (including market suspension periods).

### Features

- **Two lottery types**
  - **SSQ (双色球):** 6 red balls (1–33) + 1 blue ball (1–16), draws Tue/Thu/Sun
  - **DLT (超级大乐透):** 5 front balls (1–35) + 2 back balls (1–12), draws Mon/Wed/Sat

- **Play modes**
  - **Standard:** Generate N independent combinations
  - **Multiple (复式):** Select more numbers; app generates all valid combinations
  - **Dan-Tuo (胆拖):** Fix “dan” numbers + random “tuo” fill

- **Generation strategies:** Pure random, tail priority, hot/cold balance, odd/even balance, big/small balance

- **History:** Local storage with issue number (YYYYNNN), generation time, and winning status; issue calculation respects Spring Festival and National Day market suspensions

- **UI:** Jetpack Compose with Material3; optional theme styles (Material, Harmony, iOS-like)

### Project Structure

```
app/src/main/java/com/lottery/app/
├── domain/           # Pure Kotlin, no Android
│   ├── model/        # LotteryType, PlayType, GenerateResult, HistoryRecord, etc.
│   ├── generator/    # LotteryGenerator interface
│   ├── repository/   # HistoryRepository, PrizeResultRepository
│   └── util/         # IssueCalculator (issue number, suspension-aware)
├── usecase/          # GenerateNumbers, QueryHistory, DeleteHistory, UpdateWonStatus
├── infra/            # Android implementations
│   ├── algorithm/    # DefaultRandomGenerator (SecureRandom)
│   ├── local/        # Room DB, HistoryDao, HistoryEntity
│   └── remote/       # Stub for future prize API
├── ui/
│   ├── lottery/      # Generation screen + ViewModel
│   ├── history/      # History screen + ViewModel
│   ├── navigation/   # Bottom-tab NavGraph
│   ├── components/   # NumberBall, ResultCard, StyledCard, etc.
│   ├── theme/        # Material / Harmony / iOS styles
│   └── settings/     # StylePickerDialog
├── data/             # StylePreference (DataStore)
└── di/               # AppContainer (manual DI)
```

### Build & Run

From project root (use `gradlew.bat` on Windows if wrapper exists):

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Lint
./gradlew lint

# Clean
./gradlew clean
```

**Requirements:** JDK 17+, Android SDK (compileSdk 35, minSdk 26).

### Tech Stack

| Category   | Choice                    |
| ---------- | ------------------------- |
| Language   | Kotlin 2.0.21             |
| UI         | Jetpack Compose + Material3 |
| Database   | Room 2.6.1                |
| Processing | KSP 2.0.21                |
| Serialization | Gson 2.11.0           |
| DI         | Manual (AppContainer)     |

---

## 中文

### 简介

LotteryApp 是一款 Android 彩票选号应用，支持**双色球**和**超级大乐透**的号码生成与历史管理。支持多种玩法与生成策略，本地保存历史记录，期号计算考虑春节、国庆休市。

### 功能说明

- **彩种**
  - **双色球 (SSQ)：** 红球 6 个（1–33）+ 蓝球 1 个（1–16），周二/四/日开奖
  - **超级大乐透 (DLT)：** 前区 5 个（1–35）+ 后区 2 个（1–12），周一/三/六开奖

- **玩法**
  - **普通：** 随机生成 N 注
  - **复式：** 多选号码，由系统生成全部有效组合
  - **胆拖：** 固定胆码 + 随机拖码

- **生成策略：** 纯随机、尾数优先、冷热均衡、奇偶均衡、大小均衡

- **历史记录：** 本地存储，含期号（YYYYNNN）、生成时间、中奖状态；期号按财政部休市安排计算（春节、国庆）

- **界面：** Jetpack Compose + Material3，可选主题风格（Material、鸿蒙风、类 iOS）

### 代码结构

```
app/src/main/java/com/lottery/app/
├── domain/           # 领域层，纯 Kotlin
│   ├── model/        # 数据模型：彩种、玩法、结果、历史记录等
│   ├── generator/    # 号码生成接口
│   ├── repository/   # 历史、开奖结果仓库接口
│   └── util/         # IssueCalculator（期号计算，含休市）
├── usecase/          # 用例：生成、查询历史、删除、更新中奖状态
├── infra/            # 基础设施
│   ├── algorithm/    # 默认随机生成器（SecureRandom）
│   ├── local/        # Room 数据库、DAO、实体
│   └── remote/       # 未来开奖 API 占位
├── ui/
│   ├── lottery/      # 选号页 + ViewModel
│   ├── history/      # 历史页 + ViewModel
│   ├── navigation/   # 底部 Tab 导航
│   ├── components/   # 号码球、结果卡片等组件
│   ├── theme/        # 主题（Material / 鸿蒙 / iOS）
│   └── settings/     # 风格选择弹窗
├── data/             # 风格偏好（DataStore）
└── di/               # AppContainer 依赖注入
```

### 构建与运行

在项目根目录执行（Windows 下如有 gradle wrapper 可使用 `gradlew.bat`）：

```bash
# 构建 Debug APK
./gradlew assembleDebug

# 安装到已连接设备/模拟器
./gradlew installDebug

# 运行单元测试
./gradlew test

# 代码检查
./gradlew lint

# 清理
./gradlew clean
```

**环境要求：** JDK 17+，Android SDK（compileSdk 35，minSdk 26）。

### 技术栈

| 类别       | 技术                    |
| ---------- | ----------------------- |
| 语言       | Kotlin 2.0.21           |
| UI         | Jetpack Compose + Material3 |
| 数据库     | Room 2.6.1              |
| 注解处理   | KSP 2.0.21              |
| 序列化     | Gson 2.11.0             |
| 依赖注入   | 手动（AppContainer）    |
