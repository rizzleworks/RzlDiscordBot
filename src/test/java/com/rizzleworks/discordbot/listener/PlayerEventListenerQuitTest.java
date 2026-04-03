package com.rizzleworks.discordbot.listener;

import com.rizzleworks.discordbot.NotificationEvent;
import com.rizzleworks.discordbot.discord.DiscordWebhookSender;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PlayerEventListenerQuitTest {

    @Nested
    @ExtendWith(MockitoExtension.class)
    class QuitEvent {

        @Mock private Plugin plugin;
        @Mock private DiscordWebhookSender webhookSender;
        @Mock private FileConfiguration config;
        @Mock private PlayerQuitEvent event;
        @Mock private Player player;

        private PlayerEventListener listener;

        private static final UUID PLAYER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");

        @BeforeEach
        void setUp() {
            when(plugin.getConfig()).thenReturn(config);
            listener = new PlayerEventListener(plugin, webhookSender);
        }

        @Test
        void quitShowsSessionDuration() {
            when(config.getBoolean(NotificationEvent.LEAVE.toggleKey(), true)).thenReturn(true);
            when(config.getInt(NotificationEvent.LEAVE.colorKey(), NotificationEvent.LEAVE.defaultColor())).thenReturn(15548997);
            when(event.getPlayer()).thenReturn(player);
            when(player.getName()).thenReturn("Steve");
            when(player.getUniqueId()).thenReturn(PLAYER_UUID);
            // 5h 10m = 310 minutes = 372000 ticks
            // Join at 100000, quit at 472000 → session = 372000 ticks
            when(player.getStatistic(Statistic.PLAY_ONE_MINUTE)).thenReturn(472000);

            listener.trackSessionJoin(PLAYER_UUID, 100000);

            listener.onPlayerQuit(event);

            verify(webhookSender).send(
                    eq("Steve"),
                    eq("00000000-0000-0000-0000-000000000001"),
                    eq(15548997),
                    eq("Steve left the server"),
                    eq("Steve played for 5h 10m")
            );
        }

        @Test
        void quitWithNoJoinTrackingShowsLessThanOneMinute() {
            when(config.getBoolean(NotificationEvent.LEAVE.toggleKey(), true)).thenReturn(true);
            when(config.getInt(NotificationEvent.LEAVE.colorKey(), NotificationEvent.LEAVE.defaultColor())).thenReturn(15548997);
            when(event.getPlayer()).thenReturn(player);
            when(player.getName()).thenReturn("Steve");
            when(player.getUniqueId()).thenReturn(PLAYER_UUID);
            when(player.getStatistic(Statistic.PLAY_ONE_MINUTE)).thenReturn(500000);

            listener.onPlayerQuit(event);

            verify(webhookSender).send(
                    eq("Steve"),
                    eq("00000000-0000-0000-0000-000000000001"),
                    eq(15548997),
                    eq("Steve left the server"),
                    eq("Steve played for <1m")
            );
        }

        @Test
        void quitDisabledDoesNotSend() {
            when(config.getBoolean(NotificationEvent.LEAVE.toggleKey(), true)).thenReturn(false);

            listener.onPlayerQuit(event);

            verifyNoInteractions(webhookSender);
        }
    }

    @Nested
    class FormatSessionDuration {

        @Test
        void hoursAndMinutes() {
            assertEquals("5h 10m", PlayerEventListener.formatSessionDuration(372000));
        }

        @Test
        void exactHour() {
            assertEquals("1h 0m", PlayerEventListener.formatSessionDuration(72000));
        }

        @Test
        void minutesOnly() {
            assertEquals("45m", PlayerEventListener.formatSessionDuration(54000));
        }

        @Test
        void oneMinute() {
            assertEquals("1m", PlayerEventListener.formatSessionDuration(1200));
        }

        @Test
        void lessThanOneMinute() {
            assertEquals("<1m", PlayerEventListener.formatSessionDuration(1199));
        }

        @Test
        void zeroTicks() {
            assertEquals("<1m", PlayerEventListener.formatSessionDuration(0));
        }
    }
}
