package me.derluuz.playtimeplus.commands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class HelpCommand {

    public static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Component helpMessage = Component.empty()
                .append(Component.literal("⏱ PlaytimePlus Help\n")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .append(Component.literal("─────────────────────────\n")
                        .withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal("/playtime")
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(" — View your own playtime\n")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal("/playtime view ")
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal("<username>")
                        .withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" — View another player's playtime\n")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal("/playtime top ")
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal("<page>")
                        .withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" — View the playtime leaderboard\n")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal("/playtime remove ")
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal("<username>")
                        .withStyle(ChatFormatting.WHITE))
                .append(Component.literal(" — Remove a player from tracking\n")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal("/playtime about")
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(" — Show mod information\n")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal("/playtime help")
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(" — Show this help")
                        .withStyle(ChatFormatting.GRAY));

        source.sendSuccess(() -> helpMessage, false);
        return 1;
    }
}
