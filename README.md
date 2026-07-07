# Chest ESP Mod

A small Fabric client-side mod for Minecraft 1.21.1 that highlights chests, ender chests, barrels, and copper chests.

## Features

- **Highlights** for Chests (yellow), Ender Chests (purple), Barrels (orange), and Copper Chests (cyan)
- **Configurable UI**: Press `H` to toggle each block type on/off
- **Config File**: Settings are saved to `config/chest-esp.json`
- **Minimal Dependencies**: Only requires Fabric API
- **Copper Chest Support**: Automatically detects copper chests from any mod

## Installation

1. **Install Fabric Loader** for Minecraft 1.21.1
2. **Download Fabric API** for 1.21.1
3. **Place `chest-esp-1.0.0.jar`** in your `mods` folder
4. **Launch Minecraft** with the Fabric profile

## Usage

- Press **H** to open the config screen
- Toggle each block type on/off individually
- Close the screen with "Done"
- Config is automatically saved

## Building

Requires Java 21+ and Gradle:

```bash
./gradlew build
```

The built JAR will be in `build/libs/chest-esp-1.0.0.jar`

## License

MIT
