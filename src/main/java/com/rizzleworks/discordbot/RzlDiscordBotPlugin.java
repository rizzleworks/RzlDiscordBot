package com.rizzleworks.discordbot;

import com.rizzleworks.discordbot.command.RzlDiscordCommand;
import com.rizzleworks.discordbot.discord.DiscordWebhookSender;
import com.rizzleworks.discordbot.listener.PlayerEventListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.http.HttpClient;

public class RzlDiscordBotPlugin extends JavaPlugin {

    private HttpClient httpClient;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        var rzlCommand = getCommand(RzlDiscordCommand.COMMAND_NAME);
        if (rzlCommand != null) {
            var executor = new RzlDiscordCommand(this);
            rzlCommand.setExecutor(executor);
            rzlCommand.setTabCompleter(executor);
        }

        String webhookUrl = getConfig().getString("webhook-url", "");
        if (webhookUrl == null || webhookUrl.isBlank()) {
            getLogger().warning("No webhook URL configured! Set 'webhook-url' in plugins/RzlDiscordBot/config.yml");
            getLogger().warning("Plugin will not send any messages until a webhook URL is provided.");
            return;
        }

        httpClient = HttpClient.newHttpClient();
        String botName = getConfig().getString("bot-name", "Minecraft Server");
        String botIconUrl = getConfig().getString("bot-icon-url", "");

        var webhookSender = new DiscordWebhookSender(httpClient, webhookUrl, botName, botIconUrl, getLogger());
        var listener = new PlayerEventListener(this, webhookSender);

        getServer().getPluginManager().registerEvents(listener, this);
        getLogger().info("RzlDiscordBot enabled — posting events to Discord.");
    }

    @Override
    public void onDisable() {
        if (httpClient != null) {
            httpClient.close();
        }
        getLogger().info("RzlDiscordBot disabled.");
    }
}
