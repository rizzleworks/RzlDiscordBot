package com.rizzleworks.discordbot.discord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscordWebhookSenderTest {

    private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/123/abc";

    @Mock
    private HttpClient httpClient;

    @Mock
    private Logger logger;

    private DiscordWebhookSender sender;

    @BeforeEach
    void setUp() {
        sender = new DiscordWebhookSender(httpClient, WEBHOOK_URL, "TestBot", "", logger);
    }

    @SuppressWarnings("unchecked")
    private void mockResponse(int statusCode) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        lenient().when(response.body()).thenReturn("");
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(response));
    }

    @Test
    void sendsToCorrectUrlWithJsonContentType() {
        mockResponse(204);
        sender.send("Steve", "uuid-123", 0, "joined", null);

        verify(httpClient).sendAsync(
                argThat(req ->
                        req.uri().toString().equals(WEBHOOK_URL) &&
                        req.headers().firstValue("Content-Type").orElse("").equals("application/json")),
                any());
    }

    @Test
    void sendsPostRequest() {
        mockResponse(204);
        sender.send("Steve", "uuid-123", 0, "joined", null);

        verify(httpClient).sendAsync(
                argThat(req -> req.method().equals("POST")),
                any());
    }

    @Test
    void successfulSendNoWarnings() {
        mockResponse(204);
        sender.send("Steve", "uuid-123", 0, "joined", null);

        verify(logger, never()).warning(anyString());
    }

    @Test
    void rateLimitLogsWarning() {
        mockResponse(429);
        sender.send("Steve", "uuid-123", 0, "joined", null);

        verify(logger).warning(contains("rate limit"));
    }

    @Test
    void errorResponseLogsWarning() {
        mockResponse(500);
        sender.send("Steve", "uuid-123", 0, "joined", null);

        verify(logger).warning(contains("500"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void connectionFailureLogsWarning() {
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Connection refused")));

        sender.send("Steve", "uuid-123", 0, "joined", null);

        verify(logger).warning(contains("Connection refused"));
    }
}
