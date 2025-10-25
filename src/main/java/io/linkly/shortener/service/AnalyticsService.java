package io.linkly.shortener.service;

import io.linkly.shortener.analytics.domain.UrlHitCount;
import io.linkly.shortener.analytics.domain.UrlHitEvent;
import io.linkly.shortener.analytics.repo.UrlHitCountRepository;
import io.linkly.shortener.analytics.repo.UrlHitEventRepository;
import io.linkly.shortener.util.Base62;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AnalyticsService {
    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);
    private final UrlHitCountRepository urlHitCountRepository;
    private final UrlHitEventRepository urlHitEventRepository;

    public AnalyticsService(UrlHitCountRepository urlHitCountRepository, UrlHitEventRepository urlHitEventRepository) {
        this.urlHitCountRepository = urlHitCountRepository;
        this.urlHitEventRepository = urlHitEventRepository;
    }

    @Async
    @Transactional(transactionManager = "analyticsTransactionManager")
    public void recordHit(String code, String ip, String userAgent, String referer) {
        try {
            long id = Base62.decode(code);
            if (id > Integer.MAX_VALUE) {
                return;
            }
            Integer pk = (int) id;
            int updated = urlHitCountRepository.incrementHits(pk);
            if (updated == 0) {
                UrlHitCount counter = new UrlHitCount(pk);
                counter.setHits(1L);
                urlHitCountRepository.save(counter);
            }
            UrlHitEvent event = new UrlHitEvent();
            event.setUrlId(pk);
            event.setIp(ip);
            event.setUserAgent(userAgent);
            event.setReferer(referer);
            urlHitEventRepository.save(event);
        } catch (Exception e) {
            log.warn("analytics record failed for {}: {}", code, e.getMessage());
        }
    }
}


