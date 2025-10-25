package io.linkly.shortener.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "url_hit_counts")
public class UrlHitCount {

    @Id
    private Integer id; // same as UrlMapping id

    @Column(name = "hits", nullable = false)
    private Long hits = 0L;

    public UrlHitCount() {}

    public UrlHitCount(Integer id) {
        this.id = id;
        this.hits = 0L;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getHits() {
        return hits;
    }

    public void setHits(Long hits) {
        this.hits = hits;
    }
}


