package io.linkly.shortener.service;

import io.linkly.shortener.domain.UrlMapping;
import io.linkly.shortener.repo.UrlMappingRepository;
import io.linkly.shortener.util.Base62;
import java.util.Optional;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UrlService {

    private final UrlMappingRepository urlMappingRepository;
    private final CacheManager cacheManager;
    private static final Logger log = LoggerFactory.getLogger(UrlService.class);

    public UrlService(UrlMappingRepository urlMappingRepository, CacheManager cacheManager) {
        this.urlMappingRepository = urlMappingRepository;
        this.cacheManager = cacheManager;
    }

    @Transactional
    public String shorten(String longUrl) {
        log.info("Shortening URL");
        UrlMapping saved = urlMappingRepository.save(new UrlMapping(longUrl));
        String code = Base62.encode(saved.getId().longValue());
        log.debug("Generated short path '{}' for id {}", code, saved.getId());
        Cache cache = cacheManager.getCache("codeToUrl");
        if (cache != null) {
            cache.put(code, longUrl);
            log.debug("Cached mapping {} -> {}", code, longUrl);
        }
        return code;
    }

    @Transactional(readOnly = true)
    public Optional<String> resolve(String code) {
        log.info("Resolving short path '{}'", code);
        Cache cache = cacheManager.getCache("codeToUrl");
        if (cache != null) {
            String cached = cache.get(code, String.class);
            if (cached != null) {
                log.debug("Cache hit for {}", code);
                return Optional.of(cached);
            }
        }
        long id = Base62.decode(code);
        if (id > Integer.MAX_VALUE) {
            log.warn("Decoded id {} exceeds Integer.MAX_VALUE", id);
            return Optional.empty();
        }
        Optional<UrlMapping> entryOpt = urlMappingRepository.findById((int) id);
        if (entryOpt.isPresent()) {
            String url = entryOpt.get().getOriginalUrl();
            if (cache != null) {
                cache.put(code, url);
                log.debug("Backfilled cache for {}", code);
            }
            return Optional.of(url);
        }
        log.warn("No mapping found for '{}'", code);
        return Optional.empty();
    }
}


