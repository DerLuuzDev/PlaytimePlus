package me.derluuz.playtimeplus.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.derluuz.playtimeplus.utils.OfflinePlayerUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class RemoveCommand {

    public static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");
        MinecraftServer server = source.getServer();

        // Check permission (OP Level 2+)
        if (source.isPlayer()) {
            boolean hasPerm = false;
            try {
                // Direct check via reflection helper
                if (OfflinePlayerUtils.isOp(server, source.getPlayerOrException().getGameProfile())) {
                    hasPerm = true;
                }
            } catch (Exception e) {
            }
            if (!hasPerm) {
                source.sendFailure(Component.literal("You do not have permission to remove player stats."));
                return 0;
            }
        } else {
            // Console always has permission
        }

        // Try to find the profile
        var profileOpt = OfflinePlayerUtils.getProfileByName(server, playerName);

        if (profileOpt.isEmpty()) {
            source.sendFailure(Component.literal("Player '" + playerName + "' not found in cache."));
            return 0;
        }

        GameProfile profile = profileOpt.get();
        java.util.UUID uuid = OfflinePlayerUtils.getProfileId(profile);

        if (uuid == null) {
            source.sendFailure(Component.literal("Could not resolve UUID for player '" + playerName + "'."));
            return 0;
        }

        // Delete the stats file
        boolean deleted = OfflinePlayerUtils.deletePlayerStats(server, uuid);

        if (deleted) {
            source.sendSuccess(() -> Component.literal("Successfully removed stats for ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(playerName).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(". They will no longer appear in the leaderboard or suggestions.")
                            .withStyle(ChatFormatting.GREEN)),
                    true);
            return 1;
        } else {
            source.sendFailure(Component.literal("Failed to delete stats file for '" + playerName
                    + "'. It might not exist or the server couldn't access it."));
            return 0;
        }
    }
}
