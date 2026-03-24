package com.planningpoker.estimation.application.port.out;

import java.util.Optional;
import java.util.UUID;

/**
 * Secondary (driven) port for caching active story per room.
 */
public interface ActiveRoomCachePort {

    void cacheActiveStory(UUID roomId, UUID storyId);

    Optional<UUID> getActiveStory(UUID roomId);

    void evictActiveStory(UUID roomId);
}
