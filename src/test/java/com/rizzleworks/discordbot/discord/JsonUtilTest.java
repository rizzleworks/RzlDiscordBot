package com.rizzleworks.discordbot.discord;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonUtilTest {

    @Test
    void escapesDoubleQuotes() {
        assertThat(JsonUtil.escape("say \"hello\"")).isEqualTo("say \\\"hello\\\"");
    }

    @Test
    void escapesBackslashes() {
        assertThat(JsonUtil.escape("path\\to\\file")).isEqualTo("path\\\\to\\\\file");
    }

    @Test
    void escapesNewlines() {
        assertThat(JsonUtil.escape("line1\nline2")).isEqualTo("line1\\nline2");
    }

    @Test
    void escapesCarriageReturns() {
        assertThat(JsonUtil.escape("line1\rline2")).isEqualTo("line1\\rline2");
    }

    @Test
    void escapesTabs() {
        assertThat(JsonUtil.escape("col1\tcol2")).isEqualTo("col1\\tcol2");
    }

    @Test
    void nullReturnsEmptyString() {
        assertThat(JsonUtil.escape(null)).isEmpty();
    }

    @Test
    void cleanStringPassesThrough() {
        assertThat(JsonUtil.escape("hello world")).isEqualTo("hello world");
    }

    @Test
    void escapesMultipleSpecialCharacters() {
        assertThat(JsonUtil.escape("a\"b\\c\nd")).isEqualTo("a\\\"b\\\\c\\nd");
    }
}
