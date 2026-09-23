package smartdesk.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import smartdesk.booking.dto.cache.FloorCacheData;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FloorCacheServiceTest {

    @Autowired
    private FloorCacheService floorCacheService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldCacheFloorMetadata() {

        Long floorId = 1L;

        Cache cache = cacheManager.getCache("floorMetadata");

        assertNotNull(cache);

        cache.clear();

        // First call → PostgreSQL → Redis
        FloorCacheData first =
                floorCacheService.getFloorMetadata(floorId);

        assertNotNull(first);

        // Second call → Redis
        FloorCacheData second =
                floorCacheService.getFloorMetadata(floorId);

        assertNotNull(second);

        assertEquals(first, second);
    }
}