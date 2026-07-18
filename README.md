# PlaytimePlus

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/PlaytimePlus?logo=modrinth&color=00AF5C&label=downloads)](https://modrinth.com/mod/PlaytimePlus)
[![Fabric](https://img.shields.io/badge/loader-fabric-blue?logo=fabric)](https://fabricmc.net/)

**PlaytimePlus** is a lightweight, server-side Fabric mod for tracking and displaying player activity — built from scratch for the Fabric ecosystem.

### ✨ Features
- **No setup required** — Works straight out of the box with zero configuration needed.
- **Reliable playtime data** — Reads directly from Minecraft's built-in statistics for accurate results.
- **Offline player lookup** — View the playtime of players who aren't currently online.
- **Interactive leaderboard** — Clickable page navigation to browse through the most active players.
- **Player removal** — Remove players from tracking (e.g. Carpet bots) with `/playtime remove <name>`.
- **Geyser compatible** — Fully supports Bedrock players via Geyser, including their usernames.
- **Minimal footprint** — Extremely lightweight, with virtually no performance impact.
- **Update notifications** — Checks for new versions on startup and notifies you in the server console.

### 🖼️ Screenshots
**Chat output**

![Chat Showcase](https://raw.githubusercontent.com/DerLuuz/PlaytimeCommand/refs/heads/main/images/chat_showcase.png)

**Leaderboard view**

![Leaderboard](https://raw.githubusercontent.com/DerLuuz/PlaytimeCommand/refs/heads/main/images/leaderboard.png)


### 💻 Commands
- `/playtime` — View your own playtime
- `/playtime <username>` — View another player's playtime (works for online and offline players)
- `/playtime --top <page>` — Browse the playtime leaderboard (clickable navigation)
- `/playtime remove <username>` — Remove a player from tracking (OP only)
- `/playtime --about` — Display mod info
- `/playtime --help` — Show available commands

# Download
You can download the mod on [Modrinth](https://modrinth.com/mod/PlaytimePlus).

# Development
> [!WARNING]
> This section is meant for developers who want to contribute to the mod or build it locally.
> 
> If you just want to use the mod, you can ignore this and [download the mod](https://modrinth.com/mod/PlaytimePlus) instead.

> [!NOTE]
> You need to have [Git](https://git-scm.com/) & [JDK 21](https://adoptium.net/temurin/releases?version=21&os=any&arch=any) installed on your computer.

To build the mod locally, you have to:
1. Clone the repository and navigate to it

   ```
   git clone https://codeberg.org/DerLuuz/PlaytimePlus; cd PlaytimePlus
   ``` 
2. Build the mod using Gradle

    **Linux**
    ```
    ./gradlew build
    ```
    **Windows**
    ```
    gradlew.bat build
    ```
3. The built mod will be located in `build/libs/`
