package com.rizzleworks.discordbot.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.rizzleworks.discordbot.NotificationEvent;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.java.JavaPlugin;

public class RzlDiscordCommand {

    public static final String COMMAND_NAME = "rzldiscord";
    public static final String PERMISSION = "rzldiscordbot.admin";

    private final JavaPlugin plugin;

    public RzlDiscordCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(Commands commands) {
        var command = Commands.literal(COMMAND_NAME)
                .requires(source -> source.getSender().hasPermission(PERMISSION))
                .then(Commands.literal("toggle")
                        .then(Commands.argument("event", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    for (String name : NotificationEvent.SHORT_NAMES) {
                                        builder.suggest(name);
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(this::handleToggle)))
                .then(Commands.literal("status")
                        .executes(this::handleStatus))
                .then(Commands.literal("reload")
                        .executes(this::handleReload))
                .build();

        commands.register(command, "Manage RzlDiscordBot event toggles");
    }

    @SuppressWarnings("UnstableApiUsage")
    private int handleToggle(CommandContext<CommandSourceStack> ctx) {
        var sender = ctx.getSource().getSender();
        String eventName = StringArgumentType.getString(ctx, "event");

        NotificationEvent notificationEvent = NotificationEvent.fromShortName(eventName);
        if (notificationEvent == null) {
            sender.sendMessage(Component.text(
                    "Unknown event: " + eventName + ". Options: " + String.join(", ", NotificationEvent.SHORT_NAMES),
                    NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        boolean current = plugin.getConfig().getBoolean(notificationEvent.toggleKey(), true);
        boolean updated = !current;
        plugin.getConfig().set(notificationEvent.toggleKey(), updated);
        plugin.saveConfig();

        String state = updated ? "enabled" : "disabled";
        sender.sendMessage(Component.text(
                eventName + " notifications " + state,
                updated ? NamedTextColor.GREEN : NamedTextColor.RED));
        return Command.SINGLE_SUCCESS;
    }

    @SuppressWarnings("UnstableApiUsage")
    private int handleStatus(CommandContext<CommandSourceStack> ctx) {
        var sender = ctx.getSource().getSender();
        sender.sendMessage(Component.text("--- RzlDiscordBot Event Status ---", NamedTextColor.GOLD));
        for (NotificationEvent event : NotificationEvent.values()) {
            boolean enabled = plugin.getConfig().getBoolean(event.toggleKey(), true);
            NamedTextColor color = enabled ? NamedTextColor.GREEN : NamedTextColor.RED;
            String state = enabled ? "enabled" : "disabled";
            sender.sendMessage(Component.text("  " + event.shortName() + ": " + state, color));
        }
        return Command.SINGLE_SUCCESS;
    }

    @SuppressWarnings("UnstableApiUsage")
    private int handleReload(CommandContext<CommandSourceStack> ctx) {
        plugin.reloadConfig();
        ctx.getSource().getSender().sendMessage(Component.text("Configuration reloaded.", NamedTextColor.GREEN));
        return Command.SINGLE_SUCCESS;
    }
}
