package net.histos.mcdiscordbot.discord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
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

    @SuppressWarnings("unchecked")
    private HttpRequest captureRequest() {
        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).sendAsync(captor.capture(), any(HttpResponse.BodyHandler.class));
        return captor.getValue();
    }

    @Test
    void sendsToCorrectUrl() {
        mockResponse(204);
        sender.send("Steve", "uuid-123", 0, "joined", null);

        HttpRequest request = captureRequest();
        assertThat(request.uri().toString()).isEqualTo(WEBHOOK_URL);
    }

    @Test
    void setsContentTypeHeader() {
        mockResponse(204);
        sender.send("Steve", "uuid-123", 0, "joined", null);

        HttpRequest request = captureRequest();
        assertThat(request.headers().firstValue("Content-Type")).hasValue("application/json");
    }

    @Test
    void payloadContainsBotNameAsUsername() {
        mockResponse(204);
        sender.send("Steve", "uuid-123", 5763719, "Steve joined", null);

        HttpRequest request = captureRequest();
        String body = extractBody(request);

        assertThat(body)
                .contains("\"username\":\"TestBot\"")
                .contains("\"title\":\"Steve joined\"")
                .contains("\"color\":5763719")
                .contains("mc-heads.net/avatar/uuid-123/64");
    }

    private static String extractBody(HttpRequest request) {
        return request.bodyPublisher().map(pub -> {
            var buffers = new ArrayList<ByteBuffer>();
            pub.subscribe(new Flow.Subscriber<>() {
                public void onSubscribe(Flow.Subscription subscription) { subscription.request(Long.MAX_VALUE); }
                public void onNext(ByteBuffer item) { buffers.add(item); }
                public void onError(Throwable throwable) {}
                public void onComplete() {}
            });
            int size = buffers.stream().mapToInt(ByteBuffer::remaining).sum();
            var combined = ByteBuffer.allocate(size);
            buffers.forEach(combined::put);
            return new String(combined.array(), StandardCharsets.UTF_8);
        }).orElse("");
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
