package me.derluuz.playtimeplus.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.lang.reflect.Method;

public class OfflinePlayerUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger("PlaytimePlus");

    // Helper to get name via reflection since direct access is failing compilation
    public static String getProfileName(GameProfile profile) {
        try {
            Method m = profile.getClass().getMethod("getName");
            return (String) m.invoke(profile);
        } catch (Exception e) {
            // Try "name" record style or field
            try {
                Method m = profile.getClass().getMethod("name");
                return (String) m.invoke(profile);
            } catch (Exception e2) {
                return "Unknown";
            }
        }
    }

    // Helper to get ID via reflection
    public static UUID getProfileId(GameProfile profile) {
        try {
            Method m = profile.getClass().getMethod("getId");
            return (UUID) m.invoke(profile);
        } catch (Exception e) {
            try {
                Method m = profile.getClass().getMethod("id");
                return (UUID) m.invoke(profile);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    public static boolean isOp(MinecraftServer server, GameProfile profile) {
        if (server == null || profile == null)
            return false;
        try {
            // Try standard check first (might fail if signatures don't match)
            try {
                // Use reflection to call isOp to avoid compile-time error
                Method m = server.getPlayerList().getClass().getMethod("isOp", GameProfile.class);
                if ((boolean) m.invoke(server.getPlayerList(), profile))
                    return true;
            } catch (Throwable t) {
            }

            // Reflective check on OpList
            Object opList = server.getPlayerList().getOps();
            // StoredUserList has a map field
            java.lang.reflect.Field mapField = null;
            Class<?> clazz = opList.getClass();
            while (clazz != null && mapField == null) {
                for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
                    if (java.util.Map.class.isAssignableFrom(f.getType())) {
                        mapField = f;
                        break;
                    }
                }
                clazz = clazz.getSuperclass();
            }

            if (mapField != null) {
                mapField.setAccessible(true);
                java.util.Map<?, ?> map = (java.util.Map<?, ?>) mapField.get(opList);
                UUID targetId = getProfileId(profile);
                if (targetId == null)
                    return false;

                for (Object key : map.keySet()) {
                    // key might be GameProfile or NameAndId
                    // We check if we can extract UUID from key
                    UUID keyId = null;
                    if (key instanceof GameProfile) {
                        keyId = getProfileId((GameProfile) key);
                    } else {
                        // Try reflection for "id" or "getId" on unknown key type (NameAndId)
                        try {
                            Method m = key.getClass().getMethod("getId");
                            keyId = (UUID) m.invoke(key);
                        } catch (Exception e) {
                            try {
                                Method m = key.getClass().getMethod("id");
                                keyId = (UUID) m.invoke(key);
                            } catch (Exception e2) {
                            }
                        }
                    }

                    if (keyId != null && keyId.equals(targetId)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to check OP status reflectively", e);
        }
        return false;
    }

    public static boolean hasStatsFile(MinecraftServer server, UUID uuid) {
        if (uuid == null)
            return false;
        Path statsDirPath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.PLAYER_STATS_DIR);
        File statsFile = statsDirPath.resolve(uuid.toString() + ".json").toFile();
        return statsFile.exists();
    }

    public static boolean deletePlayerStats(MinecraftServer server, UUID uuid) {
        if (uuid == null)
            return false;
        try {
            Path statsDirPath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.PLAYER_STATS_DIR);
            File statsFile = statsDirPath.resolve(uuid.toString() + ".json").toFile();
            if (statsFile.exists()) {
                return statsFile.delete();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to delete stats for UUID: " + uuid, e);
        }
        return false;
    }

    public static int getOfflinePlaytime(MinecraftServer server, UUID uuid) {
        if (uuid == null)
            return 0;
        try {
            Path statsDirPath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.PLAYER_STATS_DIR);
            File statsFile = statsDirPath.resolve(uuid.toString() + ".json").toFile();

            if (!statsFile.exists())
                return 0;

            try (Reader reader = new FileReader(statsFile)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                if (json.has("stats")) {
                    JsonObject stats = json.getAsJsonObject("stats");
                    if (stats.has("minecraft:custom")) {
                        JsonObject custom = stats.getAsJsonObject("minecraft:custom");
                        if (custom.has("minecraft:play_time")) {
                            return custom.get("minecraft:play_time").getAsInt();
                        }
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Failed to read offline stats for UUID: " + uuid, e);
        }

        return 0;
    }

    public static Optional<GameProfile> getProfileByName(MinecraftServer server, String name) {
        return getAllProfiles(server).stream()
                .filter(profile -> getProfileName(profile).equalsIgnoreCase(name))
                .findFirst();
    }

    public static java.util.Collection<String> getAllOfflinePlayerNames(MinecraftServer server) {
        return getAllProfiles(server).stream()
                .filter(profile -> hasStatsFile(server, getProfileId(profile)))
                .map(OfflinePlayerUtils::getProfileName)
                .collect(java.util.stream.Collectors.toList());
    }

    public static java.util.List<GameProfile> getAllProfiles(MinecraftServer server) {
        java.util.List<GameProfile> profiles = new java.util.ArrayList<>();
        try {
            File userCacheFile = new File("usercache.json");

            if (!userCacheFile.exists()) {
                // Try to find if we can locate simpler path, but for now rely on CWD/default.
                if (!userCacheFile.exists())
                    return profiles;
            }

            try (Reader reader = new FileReader(userCacheFile)) {
                JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
                for (JsonElement element : jsonArray) {
                    JsonObject obj = element.getAsJsonObject();
                    String pName = obj.get("name").getAsString();
                    String uuidStr = obj.get("uuid").getAsString();
                    UUID uuid = UUID.fromString(uuidStr);
                    profiles.add(new GameProfile(uuid, pName));
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load usercache.json", e);
        }
        return profiles;
    }
}
