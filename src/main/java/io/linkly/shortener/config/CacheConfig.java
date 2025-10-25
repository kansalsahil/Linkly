package io.linkly.shortener.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class CacheConfig {

    @Bean
    @Profile({"none"})
    public CacheManager inMemoryCacheManager() {
        return new ConcurrentMapCacheManager("codeToUrl");
    }
}


