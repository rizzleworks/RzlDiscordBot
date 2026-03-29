package net.histos.mcdiscordbot.listener;

import net.histos.mcdiscordbot.discord.DiscordWebhookSender;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerEventListenerDeathTest {

    @Mock private Plugin plugin;
    @Mock private DiscordWebhookSender webhookSender;
    @Mock private FileConfiguration config;
    @Mock private PlayerDeathEvent event;
    @Mock private Player player;
    @Mock private World world;

    private PlayerEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new PlayerEventListener(plugin, webhookSender, config);
    }

    @Test
    void deathSendsMessageWithCoordinates() {
        when(config.getBoolean("events.player-death", true)).thenReturn(true);
        when(config.getInt("colors.death", 2303786)).thenReturn(2303786);
        when(event.getEntity()).thenReturn(player);
        when(player.getName()).thenReturn("Steve");
        when(player.getUniqueId()).thenReturn(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"));
        when(event.deathMessage()).thenReturn(Component.text("Steve was blown up by Creeper"));
        when(player.getLocation()).thenReturn(new Location(world, 123.7, 64.0, -456.3));

        listener.onPlayerDeath(event);

        verify(webhookSender).send(
                eq("Steve"),
                eq("00000000-0000-0000-0000-000000000001"),
                eq(2303786),
                eq("Steve was blown up by Creeper"),
                eq("at 123, 64, -457")
        );
    }

    @Test
    void deathWithNullComponentUsesFallbackMessage() {
        when(config.getBoolean("events.player-death", true)).thenReturn(true);
        when(config.getInt("colors.death", 2303786)).thenReturn(2303786);
        when(event.getEntity()).thenReturn(player);
        when(player.getName()).thenReturn("Steve");
        when(player.getUniqueId()).thenReturn(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"));
        when(event.deathMessage()).thenReturn(null);
        when(player.getLocation()).thenReturn(new Location(world, 0.0, 100.0, 0.0));

        listener.onPlayerDeath(event);

        verify(webhookSender).send(
                eq("Steve"),
                eq("00000000-0000-0000-0000-000000000001"),
                eq(2303786),
                eq("Steve died"),
                eq("at 0, 100, 0")
        );
    }

    @Test
    void deathDisabledDoesNotSend() {
        when(config.getBoolean("events.player-death", true)).thenReturn(false);

        listener.onPlayerDeath(event);

        verifyNoInteractions(webhookSender);
    }
}
