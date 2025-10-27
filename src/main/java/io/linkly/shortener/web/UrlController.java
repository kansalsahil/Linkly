package io.linkly.shortener.web;

import io.linkly.shortener.service.UrlService;
import io.linkly.shortener.service.AnalyticsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping
@Validated
public class UrlController {

    private final UrlService urlService;
    private final AnalyticsService analyticsService;
    private static final Logger log = LoggerFactory.getLogger(UrlController.class);

    public UrlController(UrlService urlService, AnalyticsService analyticsService) {
        this.urlService = urlService;
        this.analyticsService = analyticsService;
    }

    public static class ShortenRequest {
        @NotBlank
        public String longUrl;

        public String getLongUrl() { return longUrl; }
        public void setLongUrl(String longUrl) { this.longUrl = longUrl; }
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<Map<String, String>> shorten(@Valid @RequestBody ShortenRequest payload) {
        log.info("POST /api/shorten");
        String code = urlService.shorten(payload.longUrl);
        String shortUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/s/")
                .path(code)
                .toUriString();
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", shortUrl));
    }

    @GetMapping("/s/{code}")
    public ResponseEntity<?> redirect(@PathVariable("code") String code, jakarta.servlet.http.HttpServletRequest request) {
        log.info("GET /s/{}", code);
        return urlService.resolve(code)
                .map(url -> {
                    String ip = resolveClientIp(request);
                    String ua = request.getHeader("User-Agent");
                    String referer = request.getHeader("Referer");
                    analyticsService.recordHit(code, ip, ua, referer);
                    return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(url))
                        .build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private String resolveClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String first = xff.split(",")[0].trim();
            if (!first.isEmpty() && !"unknown".equalsIgnoreCase(first)) {
                return first;
            }
        }
        String xReal = request.getHeader("X-Real-IP");
        if (xReal != null && !xReal.isBlank() && !"unknown".equalsIgnoreCase(xReal)) {
            return xReal.trim();
        }
        return request.getRemoteAddr();
    }
}


