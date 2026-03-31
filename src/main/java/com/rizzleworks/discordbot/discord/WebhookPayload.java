package com.rizzleworks.discordbot.discord;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for a Discord webhook payload.
 * Models the top-level structure from the Discord webhook API.
 */
public class WebhookPayload {

    private String username;
    private String avatarUrl;
    private final List<Embed> embeds = new ArrayList<>();

    public WebhookPayload username(String username) {
        this.username = username;
        return this;
    }

    public WebhookPayload avatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return this;
    }

    public WebhookPayload addEmbed(Embed embed) {
        this.embeds.add(embed);
        return this;
    }

    public String toJson() {
        var sb = new StringBuilder();
        sb.append("{");
        sb.append("\"username\":\"").append(JsonUtil.escape(username)).append("\"");
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            sb.append(",\"avatar_url\":\"").append(JsonUtil.escape(avatarUrl)).append("\"");
        }
        if (!embeds.isEmpty()) {
            sb.append(",\"embeds\":[");
            for (int i = 0; i < embeds.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(embeds.get(i).toJson());
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }
}
