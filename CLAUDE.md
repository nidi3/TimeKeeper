# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

This is an IntelliJ IDEA project without Gradle/Maven. Build and run through the IDE:
- Open in IntelliJ IDEA
- Build: Build → Build Project (or Cmd+F9)
- Run: Run `main()` in `Main.kt`

No tests or linting are currently configured.

## Code Style

Prefer Kotlin standard library functions over Java equivalents:
- `exitProcess()` instead of `System.exit()`
- `kotlin.time.Duration` (e.g., `30.seconds`) instead of `java.time.Duration`
- `showError()` for error dialogs
- `require()` / `check()` for preconditions
- `use {}` for resources
- `buildString {}` instead of `StringBuilder`
- `runCatching {}` instead of try-catch
- String templates `"$var"` instead of `String.format()`
- `lazy {}` for lazy initialization
- `takeIf {}` / `takeUnless {}` for conditional returns
- Trailing lambda syntax for SAM interfaces
- Sealed classes for state management with `when (val s = state)` for smart casting
- Scope functions (`apply`, `also`, `let`, `run`, `with`) for concise initialization
- Private file-level helpers at end of file

## Architecture

TimeKeeper is a Kotlin/Swing desktop application that runs as a system tray timer for tracking work sessions.

**Package**: `com.timekeeper`

### Components

- **Main.kt** - Entry point, `TimerState` sealed class, and `TimeKeeperApp` class. Manages system tray icon, timer loop, and idle detection (auto-stops after 30s of inactivity).

- **TimeTracker.kt** - Session persistence layer. Contains `TimeSession` data class and `TimeTracker` class that stores sessions to `~/.timekeeper_data.txt` in pipe-delimited format (`startTime|endTime|autoStopped`).

- **OverviewWindow.kt** - Singleton statistics window with tabbed views for daily and weekly session summaries. Use `OverviewWindow.show(timeTracker)` to display.

- **Extensions.kt** - Kotlin extension functions (e.g., `Duration.format()`).

### Data Flow

1. User starts/stops timer via system tray menu
2. `TimeKeeperApp` creates `TimeSession` on stop and passes to `TimeTracker`
3. `TimeTracker` appends to in-memory list and persists to file
4. `OverviewWindow` reads sessions from `TimeTracker` for display
