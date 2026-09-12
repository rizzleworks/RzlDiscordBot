package com.rizzleworks.discordbot;

import com.rizzleworks.discordbot.listener.PlayerEventListener;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// JavaPlugin cannot be constructed outside a PluginClassLoader, so this is a
// partial mock: real startWebhook/onDisable with the server accessors stubbed.
@ExtendWith(MockitoExtension.class)
class RzlDiscordBotPluginTest {

    private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/1/abc";

    @Mock private FileConfiguration config;
    @Mock private Logger logger;
    @Mock private Server server;
    @Mock private PluginManager pluginManager;

    private RzlDiscordBotPlugin plugin;

    @BeforeEach
    void setUp() {
        plugin = mock(RzlDiscordBotPlugin.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        doReturn(config).when(plugin).getConfig();
        doReturn(logger).when(plugin).getLogger();

        // Stubbed leniently so the tests asserting no registration can still
        // reach the plugin manager, which makes never() meaningful.
        lenient().doReturn(server).when(plugin).getServer();
        lenient().doReturn(pluginManager).when(server).getPluginManager();
    }

    @Test
    void missingWebhookUrlSkipsListenerRegistration() {
        withWebhookUrl("");

        plugin.startWebhook();

        verify(logger).warning(contains("No webhook URL configured"));
        verify(logger).warning(contains("will not send any messages"));
        verify(pluginManager, never()).registerEvents(any(), any());
    }

    @Test
    void blankWebhookUrlIsTreatedAsMissing() {
        withWebhookUrl("   ");

        plugin.startWebhook();

        verify(pluginManager, never()).registerEvents(any(), any());
    }

    @Test
    void nullWebhookUrlIsTreatedAsMissing() {
        withWebhookUrl(null);

        plugin.startWebhook();

        verify(pluginManager, never()).registerEvents(any(), any());
    }

    @Test
    void configuredWebhookUrlRegistersTheListener() {
        withWebhookConfig();

        plugin.startWebhook();

        verify(pluginManager).registerEvents(any(PlayerEventListener.class), eq(plugin));
        verify(logger).info(contains("RzlDiscordBot enabled"));
    }

    @Test
    void disableWithoutAnHttpClientDoesNotThrow() {
        withWebhookUrl("");
        plugin.startWebhook();

        assertThatCode(plugin::onDisable).doesNotThrowAnyException();

        verify(logger).info(contains("RzlDiscordBot disabled"));
    }

    @Test
    void disableClosesTheHttpClient() {
        try (MockedStatic<HttpClient> httpClients = mockStatic(HttpClient.class)) {
            HttpClient client = mock(HttpClient.class);
            httpClients.when(HttpClient::newHttpClient).thenReturn(client);
            withWebhookConfig();
            plugin.startWebhook();

            plugin.onDisable();

            verify(client).close();
        }
    }

    private void withWebhookUrl(String url) {
        doReturn(url).when(config).getString(eq("webhook-url"), anyString());
    }

    private void withWebhookConfig() {
        withWebhookUrl(WEBHOOK_URL);
        doReturn("Test Server").when(config).getString(eq("bot-name"), anyString());
        doReturn("").when(config).getString(eq("bot-icon-url"), anyString());
    }
}
