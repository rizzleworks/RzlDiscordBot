package net.histos.mcdiscordbot;

import net.histos.mcdiscordbot.discord.DiscordWebhookSender;
import net.histos.mcdiscordbot.listener.PlayerEventListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.http.HttpClient;

public class McDiscordBotPlugin extends JavaPlugin {

    private HttpClient httpClient;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String webhookUrl = getConfig().getString("webhook-url", "");
        if (webhookUrl == null || webhookUrl.isBlank()) {
            getLogger().warning("No webhook URL configured! Set 'webhook-url' in plugins/McDiscordBot/config.yml");
            getLogger().warning("Plugin will not send any messages until a webhook URL is provided.");
            return;
        }

        httpClient = HttpClient.newHttpClient();
        String botName = getConfig().getString("bot-name", "Minecraft Server");

        var webhookSender = new DiscordWebhookSender(httpClient, webhookUrl, botName, getLogger());
        var listener = new PlayerEventListener(webhookSender, getConfig());

        getServer().getPluginManager().registerEvents(listener, this);
        getLogger().info("McDiscordBot enabled — posting events to Discord.");
    }

    @Override
    public void onDisable() {
        if (httpClient != null) {
            httpClient.close();
        }
        getLogger().info("McDiscordBot disabled.");
    }
}
