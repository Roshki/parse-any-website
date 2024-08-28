package com.website_parser.parser.config;

import com.website_parser.parser.model.Website;
import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.time.Duration;

@Configuration
@EnableCaching
public class EhCacheConfig {

    @Bean
    public PersistentCacheManager persistentCacheManager() {

        return CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence(new File("parserCache/", "data")))
                .withCache("website", CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Website.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder().disk(200, MemoryUnit.GB, true))
                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofDays(7))).build())
                .build(true);
    }

//    @Bean
//    public Cache<String, Website> websiteCache(PersistentCacheManager persistentCacheManager) {
//
//        return persistentCacheManager.createCache("websiteCache",
//                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Website.class,
//                        ResourcePoolsBuilder.newResourcePoolsBuilder()
//                                .heap(200, MemoryUnit.GB)
//                                .disk(200, MemoryUnit.GB, true)));
//    }
}