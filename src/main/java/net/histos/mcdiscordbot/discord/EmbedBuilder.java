package net.histos.mcdiscordbot.discord;

/**
 * Lightweight builder for Discord webhook embed JSON payloads.
 */
public class EmbedBuilder {

    private String title;
    private String description;
    private int color;
    private String thumbnailUrl;

    public EmbedBuilder title(String title) {
        this.title = title;
        return this;
    }

    public EmbedBuilder description(String description) {
        this.description = description;
        return this;
    }

    public EmbedBuilder color(int color) {
        this.color = color;
        return this;
    }

    public EmbedBuilder thumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        return this;
    }

    public String toJson() {
        var sb = new StringBuilder();
        sb.append("{");
        sb.append("\"title\":\"").append(escape(title)).append("\"");
        if (description != null && !description.isEmpty()) {
            sb.append(",\"description\":\"").append(escape(description)).append("\"");
        }
        sb.append(",\"color\":").append(color);
        if (thumbnailUrl != null) {
            sb.append(",\"thumbnail\":{\"url\":\"").append(escape(thumbnailUrl)).append("\"}");
        }
        sb.append("}");
        return sb.toString();
    }

    static String escape(String input) {
        if (input == null) return "";
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
