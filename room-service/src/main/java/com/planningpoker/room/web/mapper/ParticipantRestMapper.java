package com.planningpoker.room.web.mapper;

import com.planningpoker.room.domain.RoomParticipant;
import com.planningpoker.room.web.dto.InternalParticipantResponse;
import com.planningpoker.room.web.dto.ParticipantResponse;

import java.util.Collections;
import java.util.List;

/**
 * Static mapper from {@link RoomParticipant} domain objects to participant response DTOs.
 * <p>
 * Pure utility class — no framework imports, no instantiation.
 */
public final class ParticipantRestMapper {

    private ParticipantRestMapper() {
        // utility class
    }

    /**
     * Converts a {@link RoomParticipant} domain object to a {@link ParticipantResponse} DTO.
     *
     * @param participant the domain object (may be null)
     * @return the response DTO, or null if the domain object is null
     */
    public static ParticipantResponse toResponse(RoomParticipant participant) {
        if (participant == null) {
            return null;
        }
        return new ParticipantResponse(
                participant.getId(),
                participant.getUserId(),
                participant.getUsername(),
                participant.getRole(),
                participant.getJoinedAt(),
                participant.isConnected()
        );
    }

    /**
     * Converts a list of {@link RoomParticipant} domain objects to a list of {@link ParticipantResponse} DTOs.
     *
     * @param participants the domain objects (may be null)
     * @return an unmodifiable list of response DTOs, never null
     */
    public static List<ParticipantResponse> toResponseList(List<RoomParticipant> participants) {
        if (participants == null) {
            return Collections.emptyList();
        }
        return participants.stream()
                .map(ParticipantRestMapper::toResponse)
                .toList();
    }

    /**
     * Converts a {@link RoomParticipant} domain object to an {@link InternalParticipantResponse} DTO
     * for service-to-service communication.
     *
     * @param participant the domain object (may be null)
     * @return the internal response DTO, or null if the domain object is null
     */
    public static InternalParticipantResponse toInternalResponse(RoomParticipant participant) {
        if (participant == null) {
            return null;
        }
        return new InternalParticipantResponse(
                participant.getUserId(),
                participant.getRole()
        );
    }

    /**
     * Converts a list of {@link RoomParticipant} domain objects to a list of {@link InternalParticipantResponse} DTOs.
     *
     * @param participants the domain objects (may be null)
     * @return an unmodifiable list of internal response DTOs, never null
     */
    public static List<InternalParticipantResponse> toInternalResponseList(List<RoomParticipant> participants) {
        if (participants == null) {
            return Collections.emptyList();
        }
        return participants.stream()
                .map(ParticipantRestMapper::toInternalResponse)
                .toList();
    }
}
