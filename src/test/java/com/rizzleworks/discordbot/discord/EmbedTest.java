package com.rizzleworks.discordbot.discord;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmbedTest {

    @Test
    void allFieldsSet() {
        String json = new Embed()
                .title("Player joined")
                .description("Welcome back!")
                .color(5763719)
                .thumbnailUrl("https://mc-heads.net/avatar/abc/64")
                .toJson();

        assertThat(json).isEqualTo(
                "{\"title\":\"Player joined\",\"description\":\"Welcome back!\",\"color\":5763719,\"thumbnail\":{\"url\":\"https://mc-heads.net/avatar/abc/64\"}}"
        );
    }

    @Test
    void nullDescriptionOmitted() {
        String json = new Embed()
                .title("Player joined")
                .color(5763719)
                .toJson();

        assertThat(json).isEqualTo("{\"title\":\"Player joined\",\"color\":5763719}");
    }

    @Test
    void emptyDescriptionOmitted() {
        String json = new Embed()
                .title("Player joined")
                .description("")
                .color(5763719)
                .toJson();

        assertThat(json).isEqualTo("{\"title\":\"Player joined\",\"color\":5763719}");
    }

    @Test
    void nullThumbnailOmitted() {
        String json = new Embed()
                .title("Player joined")
                .color(0)
                .toJson();

        assertThat(json).isEqualTo("{\"title\":\"Player joined\",\"color\":0}");
    }

    @Test
    void specialCharactersInTitleEscaped() {
        String json = new Embed()
                .title("Player \"Steve\" joined")
                .color(0)
                .toJson();

        assertThat(json).isEqualTo("{\"title\":\"Player \\\"Steve\\\" joined\",\"color\":0}");
    }

    @Test
    void specialCharactersInDescriptionEscaped() {
        String json = new Embed()
                .title("Death")
                .description("Killed by \"Creeper\"\nBoom!")
                .color(0)
                .toJson();

        assertThat(json).isEqualTo(
                "{\"title\":\"Death\",\"description\":\"Killed by \\\"Creeper\\\"\\nBoom!\",\"color\":0}"
        );
    }

    @Test
    void colorIncludedAsInteger() {
        String json = new Embed()
                .title("Test")
                .color(16711680)
                .toJson();

        assertThat(json).contains("\"color\":16711680")
                .doesNotContain("\"color\":\"");
    }
}
