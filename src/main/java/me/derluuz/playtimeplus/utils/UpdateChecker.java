package me.derluuz.playtimeplus.utils;

import me.derluuz.playtimeplus.PlaytimePlus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Properties;

public class UpdateChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger("PlaytimePlus Updater");

    public static String latestVersion;
    public static String modrinthLink = "https://modrinth.com/project/PlaytimePlus/versions";

    private static final String CURRENT_VERSION = PlaytimePlus.getCurrentVersion();
    private static final String CODEBERG_PROPERTIES_URL = "https://codeberg.org/DerLuuz/PlaytimePlus/raw/branch/main/gradle.properties";

    public static void checkUpdate() {

        try {
            Properties prop = new Properties();
            prop.load(new URL(CODEBERG_PROPERTIES_URL).openStream());
            latestVersion = prop.getProperty("mod_version");

            if (latestVersion == null || latestVersion.equals("0")) {
                LOGGER.warn("[PlaytimePlus] Version checker returned null or 0. Version checker disabled.");
                return;
            }

            if (!CURRENT_VERSION.equals(latestVersion)) {
                LOGGER.warn("[PlaytimePlus] A newer version {} is available! Please consider updating!",
                        latestVersion);
                LOGGER.warn("[PlaytimePlus] Current version: {}", CURRENT_VERSION);
                LOGGER.warn("[PlaytimePlus] Download here: {}", modrinthLink);
            }
        } catch (Exception e) {
            LOGGER.error("[PlaytimePlus] Failed to check version. Assuming we're on latest version.", e);
        }
    }
}
