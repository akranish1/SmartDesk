package smartdesk.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import smartdesk.booking.dto.cache.DeskCacheData;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DeskCacheServiceTest {

    @Autowired
    private DeskCacheService deskCacheService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldCacheDeskMetadata() {

        Long deskId = 1L;

        Cache cache = cacheManager.getCache("deskMetadata");

        assertNotNull(cache);

        cache.clear();

        DeskCacheData first =
                deskCacheService.getDeskMetadata(deskId);

        assertNotNull(first);

        DeskCacheData second =
                deskCacheService.getDeskMetadata(deskId);

        assertNotNull(second);

        assertEquals(first, second);
    }
}