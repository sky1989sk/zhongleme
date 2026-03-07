# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

All commands run from the project root. Use `gradlew` on Windows (no `./` prefix needed in bash).

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run lint
./gradlew lint

# Clean build artifacts
./gradlew clean
```

## Tech Stack

- **Language:** Kotlin 2.0.21
- **UI:** Jetpack Compose with Material3
- **Database:** Room 2.6.1 (SQLite ORM)
- **Annotation processing:** KSP 2.0.21-1.0.27
- **Navigation:** Compose Navigation
- **Serialization:** Gson 2.11.0
- **DI:** Manual service locator via `AppContainer`
- **Min SDK:** 26 (Android 8.0), **Target SDK:** 35

## Architecture

Clean Architecture with three layers:

**Domain** (`domain/`) — pure Kotlin, no Android dependencies
- `model/` — data classes: `LotteryType`, `PlayType`, `LotteryRule`, `LotteryNumber`, `DanTuoConfig`, `GenerateResult`, `HistoryRecord`, `PrizeResult`, `WonStatus`, `GeneratorStrategy`
- `generator/LotteryGenerator.kt` — interface for number generation
- `repository/` — `HistoryRepository` and `PrizeResultRepository` interfaces
- `util/IssueCalculator` — computes issue number (YYYYNNN) from timestamp; SSQ draws Tue/Thu/Sun, DLT draws Mon/Wed/Sat, cutoff 20:00 CST

**Use Cases** (`usecase/`) — orchestrate domain logic
- `GenerateNumbersUseCase` — validates input and delegates to `LotteryGenerator`, saves to history
- `QueryHistoryUseCase` — filters history by lottery type
- `DeleteHistoryUseCase` — removes individual or all records
- `UpdateWonStatusUseCase` — updates `WonStatus` on a history record

**Infrastructure** (`infra/`) — Android/external implementations
- `algorithm/DefaultRandomGenerator` — implements `LotteryGenerator` using `SecureRandom`
- `local/RoomHistoryRepository` — implements `HistoryRepository` via Room DAO
- `local/db/` — `AppDatabase`, `HistoryDao`, `HistoryEntity`
- `remote/StubPrizeResultRepository` — placeholder for future remote API; always returns null

**UI** (`ui/`) — Compose screens + ViewModels
- `lottery/` — number generation screen (`LotteryScreen` + `LotteryViewModel`)
- `history/` — history screen (`HistoryScreen` + `HistoryViewModel`)
- `navigation/NavGraph.kt` — bottom-tab navigation between the two screens
- `components/` — reusable composables: `NumberBall`, `ResultCard`, `StyledCard`, `StyledButton`, `SegmentedControl`
- `theme/` — three design styles: `MATERIAL` (default), `HARMONY` (鸿蒙), `IOS26`; active style provided via `LocalDesignStyle` composition local; user picks via `StylePickerDialog`
- `settings/StylePickerDialog` — lets user switch design style at runtime; preference persisted in `data/StylePreference`

**Dependency wiring:** `AppContainer` (created in `LotteryApplication.onCreate()`) instantiates all dependencies and is accessed from `MainActivity`.

## Domain Concepts

Two supported lottery types:
- **SSQ (双色球):** 6 red balls from 1–33, 1 blue ball from 1–16
- **DLT (超级大乐透):** 5 front balls from 1–35, 2 back balls from 1–12

Three play modes (`PlayType`):
- **Standard (普通):** generate N independent combinations
- **Multiple (复式):** pick more numbers than required; system generates all valid combinations
- **Dan Tuo (胆拖):** fix guaranteed numbers (胆/Dan) + random fill numbers (拖/Tuo)

Generation rules per type are defined in `LotteryRule` and consumed by `GenerateNumbersUseCase`.

Five generator strategies (`GeneratorStrategy`): `PURE_RANDOM`, `TAIL_PRIORITY`, `HOT_COLD_BALANCE`, `ODD_EVEN_BALANCE`, `BIG_SMALL_BALANCE`.

`WonStatus` tracks prize check state per history record: `UNKNOWN` (default), `WON`, `NOT_WON`.

## Database

Room database (`lottery_app.db`, version 1) with a single table `history_records`:
- Fields: `id` (auto-increment), `lotteryType`, `playType`, `resultJson`, `createdAt`
- JSON serialization of `GenerateResult` via `ResultJsonConverter` (Gson)
- Accessed only through `HistoryRepository` interface; never directly from UI
