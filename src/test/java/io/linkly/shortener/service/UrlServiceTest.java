package io.linkly.shortener.service;

import io.linkly.shortener.domain.UrlMapping;
import io.linkly.shortener.repo.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UrlServiceTest {

    private UrlMappingRepository repository;
    private CacheManager cacheManager;
    private Cache cache;
    private UrlService service;

    @BeforeEach
    void setup() {
        repository = mock(UrlMappingRepository.class);
        cacheManager = mock(CacheManager.class);
        cache = mock(Cache.class);
        when(cacheManager.getCache("codeToUrl")).thenReturn(cache);
        service = new UrlService(repository, cacheManager);
    }

    @Test
    void shortenStoresAndReturnsBase62() {
        UrlMapping saved = new UrlMapping("https://example.com");
        saved.setId(125);
        when(repository.save(any(UrlMapping.class))).thenReturn(saved);

        String code = service.shorten("https://example.com");
        assertEquals("21", code); // 125 in base62 = 21
    }

    @Test
    void resolveReadsFromCacheFirstThenDb() {
        when(cache.get("1z", String.class)).thenReturn(null);
        UrlMapping row = new UrlMapping("https://example.com/abc");
        row.setId(123);
        when(repository.findById(123)).thenReturn(Optional.of(row));

        Optional<String> result = service.resolve("1z"); // 123 base62
        assertTrue(result.isPresent());
        assertEquals("https://example.com/abc", result.get());
    }
}


