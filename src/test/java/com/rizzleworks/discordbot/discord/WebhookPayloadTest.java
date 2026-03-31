package com.rizzleworks.discordbot.discord;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookPayloadTest {

    @Test
    void fullPayloadWithOneEmbed() {
        String json = new WebhookPayload()
                .username("Bot")
                .avatarUrl("https://example.com/icon.png")
                .addEmbed(new Embed().title("Hello").color(0))
                .toJson();

        assertThat(json).isEqualTo(
                "{\"username\":\"Bot\",\"avatar_url\":\"https://example.com/icon.png\",\"embeds\":[{\"title\":\"Hello\",\"color\":0}]}"
        );
    }

    @Test
    void blankAvatarUrlOmitted() {
        String json = new WebhookPayload()
                .username("Bot")
                .avatarUrl("")
                .addEmbed(new Embed().title("Hello").color(0))
                .toJson();

        assertThat(json).doesNotContain("avatar_url");
    }

    @Test
    void nullAvatarUrlOmitted() {
        String json = new WebhookPayload()
                .username("Bot")
                .addEmbed(new Embed().title("Hello").color(0))
                .toJson();

        assertThat(json).doesNotContain("avatar_url");
    }

    @Test
    void multipleEmbeds() {
        String json = new WebhookPayload()
                .username("Bot")
                .addEmbed(new Embed().title("First").color(1))
                .addEmbed(new Embed().title("Second").color(2))
                .toJson();

        assertThat(json).contains("\"embeds\":[{\"title\":\"First\",\"color\":1},{\"title\":\"Second\",\"color\":2}]");
    }

    @Test
    void noEmbedsOmitsArray() {
        String json = new WebhookPayload()
                .username("Bot")
                .toJson();

        assertThat(json).doesNotContain("embeds");
    }

    @Test
    void specialCharactersInUsernameEscaped() {
        String json = new WebhookPayload()
                .username("Bot \"v2\"")
                .toJson();

        assertThat(json).contains("\"username\":\"Bot \\\"v2\\\"\"");
    }
}
