package com.planningpoker.notification.infrastructure.client;

import com.planningpoker.notification.application.port.out.RoomQueryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * REST client for calling Room Service internal endpoints.
 * Returns safe defaults (empty list / null) on error to avoid
 * cascading failures between services.
 */
@Component
public class RoomServiceClient implements RoomQueryPort {

    private static final Logger log = LoggerFactory.getLogger(RoomServiceClient.class);

    private final RestClient restClient;

    public RoomServiceClient(@Qualifier("roomRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Fetches participant user IDs for a room.
     *
     * @param roomId the room ID
     * @return list of participant user IDs, or empty list on error
     */
    @Override
    public List<UUID> getParticipantUserIds(UUID roomId) {
        try {
            List<InternalParticipantResponse> participants = restClient.get()
                    .uri("/internal/rooms/{id}/participants", roomId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        // 404 is expected — room not found
                    })
                    .body(new ParameterizedTypeReference<>() {});

            if (participants == null) {
                return Collections.emptyList();
            }

            return participants.stream()
                    .map(InternalParticipantResponse::userId)
                    .toList();
        } catch (Exception ex) {
            log.warn("Failed to fetch participants for roomId={}. Cause: {}", roomId, ex.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Fetches room details by ID.
     *
     * @param roomId the room ID
     * @return room details, or null on error
     */
    @Override
    public RoomInfo getRoom(UUID roomId) {
        try {
            InternalRoomResponse resp = restClient.get()
                    .uri("/internal/rooms/{id}", roomId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        // 404 is expected — room not found
                    })
                    .body(InternalRoomResponse.class);
            return resp != null ? new RoomInfo(resp.id(), resp.name(), resp.moderatorId(), resp.status()) : null;
        } catch (Exception ex) {
            log.warn("Failed to fetch room for roomId={}. Cause: {}", roomId, ex.getMessage());
            return null;
        }
    }

    // ── Internal DTOs for Room Service responses ──────────────────────

    public record InternalRoomResponse(
            UUID id,
            String name,
            UUID moderatorId,
            String status
    ) {}

    public record InternalParticipantResponse(
            UUID userId,
            String role
    ) {}
}
