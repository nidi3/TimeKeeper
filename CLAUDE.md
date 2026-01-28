# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

Maven project. Build and run through IntelliJ IDEA or command line:
- Build: `mvn compile` or Cmd+F9 in IDE
- Run: Run `main()` in `Main.kt`
- Test: `mvn test`
- Package: `mvn clean package jpackage:jpackage -DskipTests` → `target/dist/TimeKeeper-*.dmg`

## Git Commits

Write commit message to `.git/COMMIT_MSG`, then use `git commit -F .git/COMMIT_MSG`. This avoids permission prompts for each unique commit message.

## Code Style

Prefer newer APIs and Kotlin standard library functions over Java equivalents:
- `java.nio.file.Path` with `kotlin.io.path.*` instead of `java.io.File`
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
- Scope functions: when accessing the same object multiple times, always use `apply`/`also`/`let`/`run`/`with`
- Private file-level helpers at end of file
- Use imports, not fully qualified class names

## Architecture

TimeKeeper is a Kotlin/Swing desktop application that runs as a system tray timer for tracking work sessions.

**Package**: `com.timekeeper`

### Components

- **Main.kt** - Entry point, `TimerState` sealed class, and `TimeKeeperApp` class. Manages system tray icon and timer loop.

- **IdleDetector.kt** - Detects idle/sleep and returns last active time for auto-stop. Uses injectable clock for testability.

- **TimeTracker.kt** - Session persistence layer. Contains `TimeSession` data class and `TimeTracker` class that stores sessions to `~/.timekeeper_data.txt` in pipe-delimited format (`startTime|endTime|autoStopped`).

- **OverviewWindow.kt** - Statistics window with tabbed views for daily and weekly session summaries. Handles UI only, delegates text generation to `SessionFormatter`.

- **SessionFormatter.kt** - Generates formatted text for session displays. Separates text handling from UI.

- **TrayIcons.kt** - System tray icons (`TrayIcons.stopped`, `TrayIcons.started`).

- **Extensions.kt** - Kotlin extension functions for formatting (`Duration.format()`, `LocalDateTime.formatTime()`, `LocalDate.formatDate()`).

### Data Flow

1. User starts/stops timer via system tray menu
2. `TimeKeeperApp` creates `TimeSession` on stop and passes to `TimeTracker`
3. `TimeTracker` appends to in-memory list and persists to file
4. `OverviewWindow` reads sessions from `TimeTracker` for display
