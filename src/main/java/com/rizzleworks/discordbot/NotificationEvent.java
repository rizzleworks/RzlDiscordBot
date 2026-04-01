package com.rizzleworks.discordbot;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum NotificationEvent {
    JOIN("join", "events.player-join", "colors.join", 5763719),
    LEAVE("leave", "events.player-leave", "colors.leave", 15548997),
    DEATH("death", "events.player-death", "colors.death", 2303786),
    ADVANCEMENT("advancement", "events.player-advancement", "colors.advancement", 15844367);

    private static final Map<String, NotificationEvent> BY_SHORT_NAME =
            Arrays.stream(values()).collect(Collectors.toMap(NotificationEvent::shortName, Function.identity()));

    public static final List<String> SHORT_NAMES =
            Arrays.stream(values()).map(NotificationEvent::shortName).toList();

    private final String shortName;
    private final String toggleKey;
    private final String colorKey;
    private final int defaultColor;

    NotificationEvent(String shortName, String toggleKey, String colorKey, int defaultColor) {
        this.shortName = shortName;
        this.toggleKey = toggleKey;
        this.colorKey = colorKey;
        this.defaultColor = defaultColor;
    }

    public String shortName() {
        return shortName;
    }

    public String toggleKey() {
        return toggleKey;
    }

    public String colorKey() {
        return colorKey;
    }

    public int defaultColor() {
        return defaultColor;
    }

    public static NotificationEvent fromShortName(String name) {
        return BY_SHORT_NAME.get(name.toLowerCase());
    }
}
