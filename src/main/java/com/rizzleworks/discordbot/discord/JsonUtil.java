package com.rizzleworks.discordbot.discord;

/**
 * Shared JSON string escaping utility.
 */
final class JsonUtil {

    private JsonUtil() {}

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
