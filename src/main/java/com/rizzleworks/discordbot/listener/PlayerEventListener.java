package com.rizzleworks.discordbot.listener;

import com.rizzleworks.discordbot.discord.DiscordWebhookSender;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PlayerEventListener implements Listener {

    private final Plugin plugin;
    private final DiscordWebhookSender webhookSender;
    private final FileConfiguration config;

    public PlayerEventListener(Plugin plugin, DiscordWebhookSender webhookSender, FileConfiguration config) {
        this.plugin = plugin;
        this.webhookSender = webhookSender;
        this.config = config;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!config.getBoolean("events.player-join", true)) return;

        var player = event.getPlayer();
        String playerName = player.getName();
        String playerUuid = player.getUniqueId().toString();
        int color = config.getInt("colors.join", 5763719);

        // Delay stat reading by 1 second (20 ticks) — stats may return 0 if read
        // immediately during PlayerJoinEvent because player data isn't fully loaded yet.
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            String firstPlayed = new SimpleDateFormat("MMM d, yyyy").format(new Date(player.getFirstPlayed()));
            int playTimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
            long totalMinutes = playTimeTicks / 1200L;
            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;
            String description = "First joined: " + firstPlayed + " \u2022 Play time: " + hours + "h " + minutes + "m";

            webhookSender.send(playerName, playerUuid, color, playerName + " joined the server", description);
        }, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!config.getBoolean("events.player-leave", true)) return;

        var player = event.getPlayer();
        int color = config.getInt("colors.leave", 15548997);

        webhookSender.send(
                player.getName(),
                player.getUniqueId().toString(),
                color,
                player.getName() + " left the server",
                null
        );
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!config.getBoolean("events.player-death", true)) return;

        var player = event.getEntity();
        int color = config.getInt("colors.death", 2303786);

        String deathMessage = player.getName() + " died";
        var deathComponent = event.deathMessage();
        if (deathComponent != null) {
            deathMessage = PlainTextComponentSerializer.plainText().serialize(deathComponent);
        }

        var loc = player.getLocation();
        String coordinates = "at %d, %d, %d".formatted(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        webhookSender.send(
                player.getName(),
                player.getUniqueId().toString(),
                color,
                deathMessage,
                coordinates
        );
    }

    @EventHandler
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        if (!config.getBoolean("events.player-advancement", true)) return;

        // Filter out recipe unlocks and hidden advancements
        var display = event.getAdvancement().getDisplay();
        if (display == null) return;

        var player = event.getPlayer();
        int color = config.getInt("colors.advancement", 15844367);

        String advancementTitle = PlainTextComponentSerializer.plainText().serialize(display.title());
        String advancementDesc = PlainTextComponentSerializer.plainText().serialize(display.description());

        webhookSender.send(
                player.getName(),
                player.getUniqueId().toString(),
                color,
                player.getName() + " earned [" + advancementTitle + "]",
                advancementDesc
        );
    }

    // TODO: Custom Bukkit event support
    // Future: Register additional listeners for arbitrary Bukkit events from other plugins
    // (e.g., land claims, economy transactions, etc.)
    // Design: Create a config section mapping event class names to webhook templates
}
