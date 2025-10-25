package io.linkly.shortener.analytics.repo;

import io.linkly.shortener.analytics.domain.UrlHitCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlHitCountRepository extends JpaRepository<UrlHitCount, Integer> {
    @Modifying
    @Query("update UrlHitCount u set u.hits = u.hits + 1 where u.id = :id")
    int incrementHits(@Param("id") Integer id);
}


