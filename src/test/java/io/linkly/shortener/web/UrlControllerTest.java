package io.linkly.shortener.web;

import io.linkly.shortener.service.UrlService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UrlController.class)
class UrlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UrlService urlService;

    @MockBean
    private io.linkly.shortener.service.AnalyticsService analyticsService;

    @Test
    void shortenReturnsShortUrl() throws Exception {
        when(urlService.shorten(anyString())).thenReturn("abc123");

        mockMvc.perform(post("/api/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"longUrl\":\"https://example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").exists());
    }

    @Test
    void redirectIssues302WhenFound() throws Exception {
        when(urlService.resolve("xyz")).thenReturn(Optional.of("https://example.com/x"));
        mockMvc.perform(get("/s/xyz"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/x"));
    }

    @Test
    void redirect404WhenMissing() throws Exception {
        when(urlService.resolve("missing")).thenReturn(Optional.empty());
        mockMvc.perform(get("/s/missing"))
                .andExpect(status().isNotFound());
    }
}


