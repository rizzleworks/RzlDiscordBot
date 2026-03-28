package net.histos.mcdiscordbot.discord;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;

/**
 * Sends Discord webhook messages asynchronously using Java's built-in HttpClient.
 * All sends are fire-and-forget — errors are logged but never block the server thread.
 */
public class DiscordWebhookSender {

    private static final String AVATAR_URL_TEMPLATE = "https://mc-heads.net/avatar/%s/64";

    private final HttpClient httpClient;
    private final String webhookUrl;
    private final String botName;
    private final Logger logger;

    public DiscordWebhookSender(HttpClient httpClient, String webhookUrl, String botName, Logger logger) {
        this.httpClient = httpClient;
        this.webhookUrl = webhookUrl;
        this.botName = botName;
        this.logger = logger;
    }

    public void send(String playerName, String playerUuid, int color, String title, String description) {
        String avatarUrl = AVATAR_URL_TEMPLATE.formatted(playerUuid);

        String embedJson = new EmbedBuilder()
                .title(title)
                .description(description)
                .color(color)
                .thumbnailUrl(avatarUrl)
                .toJson();

        String payload = """
                {"username":"%s","avatar_url":"%s","embeds":[%s]}"""
                .formatted(
                        EmbedBuilder.escape(playerName),
                        EmbedBuilder.escape(avatarUrl),
                        embedJson
                );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    int status = response.statusCode();
                    if (status == 429) {
                        logger.warning("Discord rate limit hit. Consider reducing event volume. Response: " + response.body());
                    } else if (status < 200 || status >= 300) {
                        logger.warning("Discord webhook returned HTTP %d: %s".formatted(status, response.body()));
                    }
                })
                .exceptionally(ex -> {
                    logger.warning("Failed to send Discord webhook: " + ex.getMessage());
                    return null;
                });
    }
}
