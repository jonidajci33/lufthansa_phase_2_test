package com.planningpoker.estimation.infrastructure.cache;

import com.planningpoker.estimation.application.port.out.ActiveRoomCachePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Redis-based implementation of {@link ActiveRoomCachePort}.
 * Stores the currently active (voting) story per room.
 * <p>
 * Key pattern: {@code pp:estimation:active-story:{roomId}}
 * TTL: 2 hours
 */
@Component
public class ActiveRoomRedisCache implements ActiveRoomCachePort {

    private static final Logger log = LoggerFactory.getLogger(ActiveRoomRedisCache.class);
    private static final String KEY_PREFIX = "pp:estimation:active-story:";
    private static final Duration TTL = Duration.ofHours(2);

    private final StringRedisTemplate redisTemplate;

    public ActiveRoomRedisCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void cacheActiveStory(UUID roomId, UUID storyId) {
        String key = buildKey(roomId);
        redisTemplate.opsForValue().set(key, storyId.toString(), TTL);
        log.debug("Cached active story={} for room={}", storyId, roomId);
    }

    @Override
    public Optional<UUID> getActiveStory(UUID roomId) {
        String key = buildKey(roomId);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(value));
    }

    @Override
    public void evictActiveStory(UUID roomId) {
        String key = buildKey(roomId);
        redisTemplate.delete(key);
        log.debug("Evicted active story cache for room={}", roomId);
    }

    private String buildKey(UUID roomId) {
        return KEY_PREFIX + roomId;
    }
}
