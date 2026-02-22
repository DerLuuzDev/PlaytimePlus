# PlaytimeCommand

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/playtimecommand?logo=modrinth&color=00AF5C&label=downloads)](https://modrinth.com/project/PlaytimeCommand)
[![Versions](https://img.shields.io/modrinth/game-versions/playtimecommand?logo=minecraft)](https://modrinth.com/project/PlaytimeCommand)
[![Fabric](https://img.shields.io/badge/loader-fabric-blue?logo=fabric)](https://fabricmc.net/)

Playtime Command is a lightweight server-side Fabric mod that allows players to check their own playtime or look up the playtime of any other player — whether online or offline.

### ✨ Features
- **No setup required** — Works straight out of the box with zero configuration needed.
- **Reliable playtime data** — Reads directly from Minecraft's built-in statistics, so the playtime shown is always accurate.
- **Offline player lookup** — View the playtime of players who aren't currently online.
- **Leaderboard** — Displays a ranked list of the most active players on the server.
- **Geyser compatible** — Fully supports Bedrock players via Geyser, including their usernames.
- **Minimal footprint** — Extremely lightweight, with virtually no performance impact. Designed to stay compatible across Minecraft updates without needing frequent mod updates.
- **Update notifications** — Checks for new versions on startup and notifies you in the server console.

### 🖼️ Screenshots
**Chat output**

![Chat Showcase](https://raw.githubusercontent.com/DerLuuz/PlaytimeCommand/refs/heads/main/images/chat_showcase.png)

**Leaderboard view**

![Leaderboard](https://raw.githubusercontent.com/DerLuuz/PlaytimeCommand/refs/heads/main/images/leaderboard.png)


### 💻 Commands
- `/playtime` — View your own playtime
- `/playtime <username>` — View another player's playtime (works for online and offline players)
- `/playtime --top <page>` — Browse the playtime leaderboard
- `/playtime --about` — Display mod info
- `/playtime --help` — Show available commands

# Download
You can download the mod [here](https://modrinth.com/project/PlaytimeCommand).

# Development
> [!WARNING]
> This section is meant for developers who want to contribute to the mod or build it locally.
> 
> If you just want to use the mod, you can ignore this and [download the mod](https://github.com/DerLuuz/PlaytimeCommand?tab=readme-ov-file#download) instead.

> [!NOTE]
> You need to have [Git](https://git-scm.com/) & [JDK 21](https://adoptium.net/temurin/releases?version=21&os=any&arch=any) installed on your computer.

To build the mod locally, you have to:
1. Clone the repository and navigate to it

   ```
   git clone https://github.com/DerLuuz/PlaytimeCommand; cd PlaytimeCommand
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
