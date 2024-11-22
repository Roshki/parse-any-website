package com.website_parser.parser.service;

import com.website_parser.parser.model.Website;
import lombok.RequiredArgsConstructor;
import org.ehcache.PersistentCacheManager;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CacheService {
    private final PersistentCacheManager cacheManager;

    public Website getWebsiteCache(String key) {
        Website w = cacheManager.getCache("website", String.class, Website.class).get(key);
        System.out.println(w == null ? "no cache" : "cache!");
        return w;

    }

    public void setWebsiteCache(String key, Website value) {
        cacheManager.getCache("website", String.class, Website.class).put(key, value);
    }

}
