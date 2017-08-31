package io.loli.maikaze.kancolle

import org.ehcache.Cache
import org.ehcache.PersistentCacheManager
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.MemoryUnit
import org.ehcache.expiry.Duration
import org.ehcache.expiry.Expirations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import java.util.concurrent.TimeUnit

/**
 * Created by chocotan on 2017/8/31.
 */
@Component
public class KcsCacheService {
    @Autowired
    KancolleProperties properties

    PersistentCacheManager cacheManager

    Cache<String, KcsCacheObject> cache;

    @PostConstruct
    def init() {

        // Acquire the default cache provider
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .with(CacheManagerBuilder.persistence(new File(properties.cachePath, "kcs")))
                .withCache("kcs",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, KcsCacheObject.class,
                        ResourcePoolsBuilder.newResourcePoolsBuilder().disk(1024, MemoryUnit.MB, true))
                        .withExpiry(Expirations.timeToLiveExpiration(Duration.of(20, TimeUnit.DAYS)))
                        .withSizeOfMaxObjectGraph(4000)
        ).build(true)
        cache = cacheManager.getCache("kcs", String, KcsCacheObject);

    }


    def get(String key) {
        cache.get(key)
    }

    def put(String key, KcsCacheObject object) {
        cache.put(key, object)
    }


    @PreDestroy
    def destroy() {
        cacheManager.close();
    }
}
