package me.derluuz.playtimeplus;

import me.derluuz.playtimeplus.commands.RootCommand;
import me.derluuz.playtimeplus.utils.UpdateChecker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaytimePlus implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("PlaytimePlus");

    @Override
    public void onInitialize() {
        long startTime = System.currentTimeMillis();

        LOGGER.info("[PlaytimePlus] Initialization started");

        CommandRegistrationCallback.EVENT.register(RootCommand::register);
        LOGGER.info("[PlaytimePlus] Registered command");

        UpdateChecker.checkUpdate();

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("[PlaytimePlus] Shutting down");
        });

        long endTime = System.currentTimeMillis();
        LOGGER.info("[PlaytimePlus] Mod startup completed in {}ms", endTime - startTime);
    }

    public static String getCurrentVersion() {
        try {
            return FabricLoader.getInstance()
                    .getModContainer("playtimeplus")
                    .map(container -> container.getMetadata().getVersion().getFriendlyString())
                    .orElse("unknown");
        } catch (Exception e) {
            LOGGER.warn("[PlaytimePlus] Could not load current version from Fabric loader, using fallback", e);
            return "unknown";
        }
    }
}
