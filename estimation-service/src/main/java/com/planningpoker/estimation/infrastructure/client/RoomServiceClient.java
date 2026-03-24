package com.planningpoker.estimation.infrastructure.client;

import com.planningpoker.estimation.application.port.out.RoomValidationPort;
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
 * REST client adapter that implements {@link RoomValidationPort} by calling the
 * Room Service's internal endpoints.
 * <p>
 * Returns safe defaults on error (room does not exist, not moderator, empty participant list).
 */
@Component
public class RoomServiceClient implements RoomValidationPort {

    private static final Logger log = LoggerFactory.getLogger(RoomServiceClient.class);

    private final RestClient restClient;

    public RoomServiceClient(@Qualifier("roomRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public boolean roomExists(UUID roomId) {
        try {
            var response = restClient.get()
                    .uri("/internal/rooms/{id}", roomId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        // 404 is expected — room not found
                    })
                    .toBodilessEntity();

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.warn("Failed to check room existence for roomId={}. Cause: {}", roomId, ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean isModeratorOf(UUID roomId, UUID userId) {
        try {
            InternalRoomResponse room = restClient.get()
                    .uri("/internal/rooms/{id}", roomId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        // 404 is expected — room not found
                    })
                    .body(InternalRoomResponse.class);

            return room != null && userId.equals(room.moderatorId());
        } catch (Exception ex) {
            log.warn("Failed to check moderator status for roomId={}, userId={}. Cause: {}",
                    roomId, userId, ex.getMessage());
            return false;
        }
    }

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

    // ── Internal DTOs for Room Service responses ──────────────────────

    private record InternalRoomResponse(
            UUID id,
            String name,
            UUID moderatorId,
            String status
    ) {}

    private record InternalParticipantResponse(
            UUID userId,
            String role
    ) {}
}
