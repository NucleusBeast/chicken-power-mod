<img src="src/main/resources/assets/chickenpower/icon.png" width="64" height="64" alt="">

# Chicken Power

A Minecraft mod for NeoForge. It uses [Stonecutter](https://stonecutter.kikugie.dev/) to build for many Minecraft versions from one source tree.

Based on [stonecutter-template-neoforge](https://github.com/stonecutter-versioning/stonecutter-template-neoforge).

## Supported versions
Set in `settings.gradle.kts`. Per-version properties are in `stonecutter.properties.toml`.

| Minecraft | NeoForge   |
|-----------|------------|
| 1.21.1    | 21.1.265   |
| 1.21.11   | 21.11.48   |
| 26.2      | 26.2.0.93  |
| 26.3      | 26.3.0.101 |

## Usage
- Use the `"Set active project to ..."` Gradle tasks to change the Minecraft version that `src/` uses.
- Use `./gradlew buildAndCollect` to put the mod jars in `build/libs/{mod version}/`.
- Use `./gradlew :26.3:runClient` (or another version) to start the game.

## Useful links
- [Stonecutter beginner's guide](https://stonecutter.kikugie.dev/wiki/start/)
- [NeoForge docs](https://docs.neoforged.net/)
