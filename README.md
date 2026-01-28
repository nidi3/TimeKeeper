# TimeKeeper

A macOS system tray timer for tracking work sessions.

## Features

- Start/stop timer from system tray
- Auto-stops on idle/sleep detection
- Daily and weekly session overview

## Build

```bash
mvn compile
```

## Package

```bash
mvn clean package jpackage:jpackage -DskipTests
```

Creates `target/dist/TimeKeeper-*.dmg`
