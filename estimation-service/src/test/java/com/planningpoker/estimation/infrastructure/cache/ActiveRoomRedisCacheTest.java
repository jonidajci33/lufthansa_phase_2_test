package com.planningpoker.estimation.infrastructure.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActiveRoomRedisCacheTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ActiveRoomRedisCache cache;

    @Captor
    private ArgumentCaptor<String> keyCaptor;

    @Captor
    private ArgumentCaptor<String> valueCaptor;

    @Captor
    private ArgumentCaptor<Duration> ttlCaptor;

    private static final UUID ROOM_ID = UUID.fromString("aaaa1111-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID STORY_ID = UUID.fromString("bbbb2222-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final String EXPECTED_KEY = "pp:estimation:active-story:" + ROOM_ID;

    // ═══════════════════════════════════════════════════════════════════
    // cacheActiveStory
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldCacheActiveStoryWithCorrectKeyAndTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        cache.cacheActiveStory(ROOM_ID, STORY_ID);

        verify(valueOperations).set(
                keyCaptor.capture(),
                valueCaptor.capture(),
                ttlCaptor.capture()
        );

        assertThat(keyCaptor.getValue()).isEqualTo(EXPECTED_KEY);
        assertThat(valueCaptor.getValue()).isEqualTo(STORY_ID.toString());
        assertThat(ttlCaptor.getValue()).isEqualTo(Duration.ofHours(2));
    }

    @Test
    void shouldBuildCorrectKeyPrefix() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        UUID differentRoom = UUID.fromString("dddd4444-dddd-dddd-dddd-dddddddddddd");
        cache.cacheActiveStory(differentRoom, STORY_ID);

        verify(valueOperations).set(
                eq("pp:estimation:active-story:" + differentRoom),
                eq(STORY_ID.toString()),
                eq(Duration.ofHours(2))
        );
    }

    // ═══════════════════════════════════════════════════════════════════
    // getActiveStory
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldReturnStoryId_whenCacheHit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(EXPECTED_KEY)).thenReturn(STORY_ID.toString());

        Optional<UUID> result = cache.getActiveStory(ROOM_ID);

        assertThat(result).isPresent().contains(STORY_ID);
    }

    @Test
    void shouldReturnEmpty_whenCacheMiss() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(EXPECTED_KEY)).thenReturn(null);

        Optional<UUID> result = cache.getActiveStory(ROOM_ID);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldParseUuidCorrectly_fromRedisValue() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        UUID specificId = UUID.fromString("eeee5555-eeee-eeee-eeee-eeeeeeeeeeee");
        when(valueOperations.get(EXPECTED_KEY)).thenReturn(specificId.toString());

        Optional<UUID> result = cache.getActiveStory(ROOM_ID);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(specificId);
    }

    // ═══════════════════════════════════════════════════════════════════
    // evictActiveStory
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldDeleteKey_whenEvicting() {
        cache.evictActiveStory(ROOM_ID);

        verify(redisTemplate).delete(EXPECTED_KEY);
    }

    @Test
    void shouldEvictCorrectKey_forDifferentRooms() {
        UUID room1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID room2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

        cache.evictActiveStory(room1);
        cache.evictActiveStory(room2);

        verify(redisTemplate).delete("pp:estimation:active-story:" + room1);
        verify(redisTemplate).delete("pp:estimation:active-story:" + room2);
    }
}
