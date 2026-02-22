package me.derluuz.playtimecommand.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.ChatFormatting;
import java.util.Optional;
import com.mojang.authlib.GameProfile;
import me.derluuz.playtimecommand.utils.OfflinePlayerUtils;
import static me.derluuz.playtimecommand.utils.FormatPlaytime.formatPlaytime;

public class RootCommand {

    // Suggest all known players (online + offline cache)
    private static final SuggestionProvider<CommandSourceStack> ALL_PLAYERS = (context, builder) -> {
        return SharedSuggestionProvider.suggest(
                OfflinePlayerUtils.getAllOfflinePlayerNames(context.getSource().getServer()),
                builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
            net.minecraft.commands.CommandBuildContext registryAccess,
            Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("playtime")
                .executes(RootCommand::execute)
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(ALL_PLAYERS)
                        .executes(RootCommand::executeOtherPlayer))
                .then(Commands.literal("--top")
                        .executes(TopCommand::execute)
                        .then(Commands.argument("page", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1))
                                .executes(TopCommand::execute)))
                .then(Commands.literal("remove")
                        // Permission check moved to execute method for safety
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(ALL_PLAYERS)
                                .executes(RemoveCommand::execute)))
                .then(Commands.literal("--about")
                        .executes(AboutCommand::execute))
                .then(Commands.literal("--help")
                        .executes(HelpCommand::execute)));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        // ask for username if console
        if (!source.isPlayer()) {
            source.sendFailure(Component.literal("Please specify a player: /playtime <player>"));
            return 0;
        }

        try {
            ServerPlayer player = source.getPlayerOrException();
            return printPlaytime(player.getScoreboardName(),
                    player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)), source, true);
        } catch (Exception e) {
            source.sendFailure(Component.literal("An error occurred while executing the command!"));
            return 0;
        }
    }

    private static int executeOtherPlayer(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        try {
            String playerName = StringArgumentType.getString(context, "player");
            ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayerByName(playerName);

            if (targetPlayer != null) {
                // Player is online
                int playtime = targetPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
                return printPlaytime(targetPlayer.getScoreboardName(), playtime, source,
                        source.isPlayer() && source.getPlayer() == targetPlayer);
            } else {
                // Player is offline, try to find in cache
                Optional<GameProfile> profileOptional = OfflinePlayerUtils.getProfileByName(source.getServer(),
                        playerName);

                if (profileOptional.isPresent()) {
                    GameProfile profile = profileOptional.get();
                    int playtime = OfflinePlayerUtils.getOfflinePlaytime(source.getServer(),
                            OfflinePlayerUtils.getProfileId(profile));

                    if (playtime == 0) {
                        // Could mean actual 0 or file not found.
                        // We'll treat it as valid but maybe warn if it seems suspicious?
                        // For now, just print it.
                    }

                    return printPlaytime(OfflinePlayerUtils.getProfileName(profile), playtime, source, false);
                } else {
                    source.sendFailure(
                            Component.literal("Player '" + playerName + "' not found (never joined or not in cache)!"));
                    return 0;
                }
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("An error occurred while executing the command!"));
            e.printStackTrace(); // Log error for debugging
            return 0;
        }
    }

    private static int printPlaytime(String playerName, int playtimeTicks, CommandSourceStack source, boolean isSelf) {
        String formattedTime = formatPlaytime(playtimeTicks);

        Component message = Component.empty()
                .append(Component.literal(isSelf ? "Your playtime is " : playerName + "'s playtime is ")
                        .withStyle(ChatFormatting.GOLD))
                .append(Component.literal(formattedTime).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        source.sendSuccess(() -> message, false);
        return 1;
    }
}
