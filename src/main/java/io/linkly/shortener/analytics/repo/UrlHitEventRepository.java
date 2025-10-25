package io.linkly.shortener.analytics.repo;

import io.linkly.shortener.analytics.domain.UrlHitEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlHitEventRepository extends JpaRepository<UrlHitEvent, Long> {
}


