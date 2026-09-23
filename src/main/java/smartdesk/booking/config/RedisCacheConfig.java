package smartdesk.booking.config;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import smartdesk.booking.dto.cache.DeskCacheData;
import smartdesk.booking.dto.cache.FloorCacheData;
import smartdesk.booking.dto.cache.TeamCacheData;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Configuration
public class RedisCacheConfig {

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory redisConnectionFactory,
            ObjectMapper objectMapper) {

        JacksonJsonRedisSerializer<DeskCacheData> deskSerializer =
                new JacksonJsonRedisSerializer<>(
                        objectMapper,
                        DeskCacheData.class
                );

        JacksonJsonRedisSerializer<FloorCacheData> floorSerializer =
                new JacksonJsonRedisSerializer<>(
                        objectMapper,
                        FloorCacheData.class
                );

        JacksonJsonRedisSerializer<TeamCacheData> teamSerializer =
                new JacksonJsonRedisSerializer<>(
                        objectMapper,
                        TeamCacheData.class
                );

        RedisCacheConfiguration deskCacheConfiguration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(deskSerializer)
                        );

        RedisCacheConfiguration floorCacheConfiguration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(30))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(floorSerializer)
                        );

        RedisCacheConfiguration teamCacheConfiguration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(30))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(teamSerializer)
                        );

        return RedisCacheManager.builder(redisConnectionFactory)
                .withCacheConfiguration(
                        "deskMetadata",
                        deskCacheConfiguration
                )
                .withCacheConfiguration(
                        "floorMetadata",
                        floorCacheConfiguration
                )
                .withCacheConfiguration(
                        "teamMetadata",
                        teamCacheConfiguration
                )
                .build();
    }
}