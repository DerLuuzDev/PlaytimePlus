package me.derluuz.playtimeplus.commands;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.ChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static me.derluuz.playtimeplus.utils.FormatPlaytime.formatPlaytime;

public class TopCommand {
    private static final Logger LOGGER = LogManager.getLogger(TopCommand.class);

    private static final int PLAYERS_PER_PAGE = 10;
    private static final long CACHE_DURATION_MS = 3000; // 3 seconds
    private static final long LOADING_MESSAGE_DELAY_MS = 500;

    private static List<PlayerPlaytime> cachedPlayerList = null;
    private static long lastCacheTime = 0;

    public static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        // try to get page number from arguments, default to 1 if not provided
        int page = 1;
        try {
            page = IntegerArgumentType.getInteger(context, "page");
        } catch (IllegalArgumentException ignored) {
        }

        final int requestedPage = page;
        long currentTime = System.currentTimeMillis();
        if (cachedPlayerList != null && (currentTime - lastCacheTime) < CACHE_DURATION_MS) {
            Component leaderboard = buildLeaderboardPage(cachedPlayerList, requestedPage);
            source.sendSuccess(() -> leaderboard, false);
            return 1;
        }

        AtomicBoolean calculationComplete = new AtomicBoolean(false);

        // send loading message if it has been more than a sec
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(LOADING_MESSAGE_DELAY_MS);
                if (!calculationComplete.get()) {
                    source.sendSuccess(() -> Component.literal("⏱ Calculating leaderboard...")
                            .withStyle(ChatFormatting.AQUA), false);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // calculate leaderboard asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                List<PlayerPlaytime> playerList = calculatePlayerList(source);

                // cache result
                cachedPlayerList = playerList;
                lastCacheTime = System.currentTimeMillis();
                calculationComplete.set(true);

                // build and print result for requested page
                Component leaderboard = buildLeaderboardPage(playerList, requestedPage);
                source.sendSuccess(() -> leaderboard, false);
            } catch (Exception e) {
                calculationComplete.set(true);
                source.sendFailure(Component.literal("An error occurred while calculating the leaderboard!"));
                LOGGER.error("Error calculating leaderboard", e);
            }
        });

        return 1;
    }

    private static List<PlayerPlaytime> calculatePlayerList(CommandSourceStack source) {
        var server = source.getServer();
        var onlinePlayers = server.getPlayerList().getPlayers();
        var allProfiles = me.derluuz.playtimeplus.utils.OfflinePlayerUtils.getAllProfiles(server);

        Map<UUID, PlayerPlaytime> leaderboardMap = new HashMap<>();

        // Add online players first for most accurate data
        for (ServerPlayer player : onlinePlayers) {
            int playtimeTicks = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
            if (playtimeTicks > 0) {
                leaderboardMap.put(player.getUUID(), new PlayerPlaytime(player.getScoreboardName(), playtimeTicks));
            }
        }

        // Add offline players if not already present
        for (com.mojang.authlib.GameProfile profile : allProfiles) {
            UUID id = me.derluuz.playtimeplus.utils.OfflinePlayerUtils.getProfileId(profile);
            if (id != null && !leaderboardMap.containsKey(id)) {
                int playtime = me.derluuz.playtimeplus.utils.OfflinePlayerUtils.getOfflinePlaytime(server, id);
                if (playtime > 0) {
                    leaderboardMap.put(id, new PlayerPlaytime(
                            me.derluuz.playtimeplus.utils.OfflinePlayerUtils.getProfileName(profile), playtime));
                }
            }
        }

        return leaderboardMap.values().stream()
                .sorted(Comparator.comparingInt(PlayerPlaytime::playtimeTicks).reversed())
                .toList();
    }

    private static Component buildLeaderboardPage(List<PlayerPlaytime> playerList, int page) {
        int totalPlayers = playerList.size();
        int totalPages = (int) Math.ceil((double) totalPlayers / PLAYERS_PER_PAGE);

        // validate page number
        if (page < 1) {
            page = 1;
        }
        if (page > totalPages && totalPages > 0) {
            page = totalPages;
        }

        final int finalPage = page;

        int startIndex = (finalPage - 1) * PLAYERS_PER_PAGE;
        int endIndex = Math.min(startIndex + PLAYERS_PER_PAGE, totalPlayers);

        // build the leaderboard message
        // header
        MutableComponent leaderboard = Component.literal("⏱ ")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal("PlaytimePlus Leaderboard")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .append(Component.literal("\n─────────────────────────\n")
                        .withStyle(ChatFormatting.DARK_GRAY));

        for (int i = startIndex; i < endIndex; i++) {
            PlayerPlaytime pp = playerList.get(i);
            int position = i + 1;
            String formattedTime = formatPlaytime(pp.playtimeTicks());

            // remove period from playtime since its cleaner
            if (formattedTime.endsWith(".")) {
                formattedTime = formattedTime.substring(0, formattedTime.length() - 1);
            }

            leaderboard = leaderboard
                    .append(Component.literal("#" + position + " ")
                            .withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(pp.playerName())
                            .withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" — ")
                            .withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(formattedTime)
                            .withStyle(ChatFormatting.GREEN));

            if (i < endIndex - 1) {
                leaderboard = leaderboard.append(Component.literal("\n"));
            }
        }

        // page navigation (if there are multiple pages)
        if (totalPages > 1) {
            leaderboard = leaderboard
                    .append(Component.literal("\n─────────────────────────\n")
                            .withStyle(ChatFormatting.DARK_GRAY));

            MutableComponent navigation = Component.empty();

            // previous page button
            if (finalPage > 1) {
                Style prevStyle = Style.EMPTY
                        .withColor(ChatFormatting.AQUA)
                        .withClickEvent(new ClickEvent.RunCommand(
                                "/playtime top " + (finalPage - 1)))
                        .withHoverEvent(new HoverEvent.ShowText(
                                Component.literal("Go to page " + (finalPage - 1))
                                        .withStyle(ChatFormatting.GREEN)));
                navigation = navigation.append(Component.literal("« Prev").setStyle(prevStyle));
            } else {
                navigation = navigation.append(Component.literal("« Prev").withStyle(ChatFormatting.DARK_GRAY));
            }

            navigation = navigation
                    .append(Component.literal("  " + finalPage + "/" + totalPages + "  ")
                            .withStyle(ChatFormatting.GRAY));

            // next page button
            if (finalPage < totalPages) {
                Style nextStyle = Style.EMPTY
                        .withColor(ChatFormatting.AQUA)
                        .withClickEvent(new ClickEvent.RunCommand(
                                "/playtime top " + (finalPage + 1)))
                        .withHoverEvent(new HoverEvent.ShowText(
                                Component.literal("Go to page " + (finalPage + 1))
                                        .withStyle(ChatFormatting.GREEN)));
                navigation = navigation.append(Component.literal("Next »").setStyle(nextStyle));
            } else {
                navigation = navigation.append(Component.literal("Next »").withStyle(ChatFormatting.DARK_GRAY));
            }

            leaderboard = leaderboard.append(navigation);
        }

        return leaderboard;
    }

    private record PlayerPlaytime(String playerName, int playtimeTicks) {
    }
}
