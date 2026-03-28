package net.histos.mcdiscordbot.discord;

/**
 * Builder for a Discord embed object.
 * Models the embed structure from the Discord webhook API.
 */
public class Embed {

    private String title;
    private String description;
    private int color;
    private String thumbnailUrl;

    public Embed title(String title) {
        this.title = title;
        return this;
    }

    public Embed description(String description) {
        this.description = description;
        return this;
    }

    public Embed color(int color) {
        this.color = color;
        return this;
    }

    public Embed thumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        return this;
    }

    String toJson() {
        var sb = new StringBuilder();
        sb.append("{");
        sb.append("\"title\":\"").append(JsonUtil.escape(title)).append("\"");
        if (description != null && !description.isEmpty()) {
            sb.append(",\"description\":\"").append(JsonUtil.escape(description)).append("\"");
        }
        sb.append(",\"color\":").append(color);
        if (thumbnailUrl != null) {
            sb.append(",\"thumbnail\":{\"url\":\"").append(JsonUtil.escape(thumbnailUrl)).append("\"}");
        }
        sb.append("}");
        return sb.toString();
    }
}
