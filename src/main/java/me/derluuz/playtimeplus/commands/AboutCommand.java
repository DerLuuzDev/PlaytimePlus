package me.derluuz.playtimeplus.commands;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import me.derluuz.playtimeplus.PlaytimePlus;

public class AboutCommand {

    public static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        Component aboutMessage = Component.empty()
                .append(Component.literal("⏱ PlaytimePlus")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .append(Component.literal(" v" + PlaytimePlus.getCurrentVersion())
                        .withStyle(ChatFormatting.GREEN))
                .append(Component.literal(" by ")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal("DerLuuz")
                        .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));

        source.sendSuccess(() -> aboutMessage, false);
        return 1;
    }
}
