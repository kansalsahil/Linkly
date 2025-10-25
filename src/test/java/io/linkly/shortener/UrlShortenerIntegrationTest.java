package io.linkly.shortener;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.util.concurrent.atomic.AtomicReference;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UrlShortenerIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    void createThenRedirect() {
        WebTestClient webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        // Create short URL
        AtomicReference<String> shortUrlRef = new AtomicReference<>();
        webTestClient.post()
                .uri("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"longUrl\":\"https://example.com/integration\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.url").value(v -> shortUrlRef.set(v.toString()));

        String shortUrl = shortUrlRef.get();
        String code = shortUrl.substring(shortUrl.lastIndexOf('/') + 1);

        // Follow redirect
        webTestClient.get()
                .uri("/s/" + code)
                .exchange()
                .expectStatus().isFound()
                .expectHeader().valueEquals("Location", "https://example.com/integration");
    }
}


