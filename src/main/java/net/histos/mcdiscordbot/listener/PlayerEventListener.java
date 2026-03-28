package net.histos.mcdiscordbot.listener;

import net.histos.mcdiscordbot.discord.DiscordWebhookSender;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEventListener implements Listener {

    private final DiscordWebhookSender webhookSender;
    private final FileConfiguration config;

    public PlayerEventListener(DiscordWebhookSender webhookSender, FileConfiguration config) {
        this.webhookSender = webhookSender;
        this.config = config;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!config.getBoolean("events.player-join", true)) return;

        var player = event.getPlayer();
        int color = config.getInt("colors.join", 5763719);

        webhookSender.send(
                player.getName(),
                player.getUniqueId().toString(),
                color,
                player.getName() + " joined the server",
                null
        );
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

        webhookSender.send(
                player.getName(),
                player.getUniqueId().toString(),
                color,
                deathMessage,
                null
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
