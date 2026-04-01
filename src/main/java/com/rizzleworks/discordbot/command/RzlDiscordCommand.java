package com.rizzleworks.discordbot.command;

import com.rizzleworks.discordbot.NotificationEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class RzlDiscordCommand implements CommandExecutor, TabCompleter {

    public static final String COMMAND_NAME = "rzldiscord";

    private enum Subcommand {
        TOGGLE("toggle"),
        STATUS("status"),
        RELOAD("reload");

        private static final List<String> NAMES =
                Arrays.stream(values()).map(Subcommand::label).toList();

        private final String label;

        Subcommand(String label) {
            this.label = label;
        }

        String label() {
            return label;
        }

        static Subcommand fromLabel(String label) {
            for (Subcommand sub : values()) {
                if (sub.label.equals(label)) return sub;
            }
            return null;
        }
    }

    private final JavaPlugin plugin;

    public RzlDiscordCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        Subcommand subcommand = Subcommand.fromLabel(args[0].toLowerCase());
        if (subcommand == null) {
            sendUsage(sender);
            return true;
        }

        return switch (subcommand) {
            case TOGGLE -> handleToggle(sender, args);
            case STATUS -> handleStatus(sender);
            case RELOAD -> handleReload(sender);
        };
    }

    private void sendUsage(CommandSender sender) {
        String subcommands = String.join("|", Subcommand.NAMES);
        sender.sendMessage(Component.text("Usage: /" + COMMAND_NAME + " <" + subcommands + ">", NamedTextColor.YELLOW));
    }

    private boolean handleToggle(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text(
                    "Usage: /" + COMMAND_NAME + " " + Subcommand.TOGGLE.label() + " <" + String.join("|", NotificationEvent.SHORT_NAMES) + ">",
                    NamedTextColor.YELLOW));
            return true;
        }

        String eventName = args[1].toLowerCase();
        NotificationEvent notificationEvent = NotificationEvent.fromShortName(eventName);
        if (notificationEvent == null) {
            sender.sendMessage(Component.text("Unknown event: " + eventName + ". Options: " + String.join(", ", NotificationEvent.SHORT_NAMES), NamedTextColor.RED));
            return true;
        }

        boolean current = plugin.getConfig().getBoolean(notificationEvent.toggleKey(), true);
        boolean updated = !current;
        plugin.getConfig().set(notificationEvent.toggleKey(), updated);
        plugin.saveConfig();

        String state = updated ? "enabled" : "disabled";
        sender.sendMessage(Component.text(eventName + " notifications " + state, updated ? NamedTextColor.GREEN : NamedTextColor.RED));
        return true;
    }

    private boolean handleStatus(CommandSender sender) {
        sender.sendMessage(Component.text("--- RzlDiscordBot Event Status ---", NamedTextColor.GOLD));
        for (NotificationEvent event : NotificationEvent.values()) {
            boolean enabled = plugin.getConfig().getBoolean(event.toggleKey(), true);
            NamedTextColor color = enabled ? NamedTextColor.GREEN : NamedTextColor.RED;
            String state = enabled ? "enabled" : "disabled";
            sender.sendMessage(Component.text("  " + event.shortName() + ": " + state, color));
        }
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage(Component.text("Configuration reloaded.", NamedTextColor.GREEN));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Subcommand.NAMES.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase(Subcommand.TOGGLE.label())) {
            return NotificationEvent.SHORT_NAMES.stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
