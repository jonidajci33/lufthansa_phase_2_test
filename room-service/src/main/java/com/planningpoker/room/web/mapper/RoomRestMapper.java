package com.planningpoker.room.web.mapper;

import com.planningpoker.room.domain.Room;
import com.planningpoker.room.web.dto.InternalRoomResponse;
import com.planningpoker.room.web.dto.RoomResponse;

import java.util.Collections;
import java.util.List;

/**
 * Static mapper from {@link Room} domain objects to room response DTOs.
 * <p>
 * Delegates to {@link DeckTypeRestMapper} for nested deck type conversion.
 * Pure utility class — no framework imports, no instantiation.
 */
public final class RoomRestMapper {

    private RoomRestMapper() {
        // utility class
    }

    /**
     * Converts a {@link Room} domain object to a {@link RoomResponse} DTO.
     *
     * @param room the domain object (may be null)
     * @return the response DTO, or null if the domain object is null
     */
    public static RoomResponse toResponse(Room room) {
        if (room == null) {
            return null;
        }
        return new RoomResponse(
                room.getId(),
                room.getName(),
                room.getDescription(),
                room.getModeratorId(),
                DeckTypeRestMapper.toResponse(room.getDeckType()),
                room.getShortCode(),
                room.getStatus(),
                room.getMaxParticipants(),
                room.getParticipants() != null ? room.getParticipants().size() : 0,
                room.getCreatedAt()
        );
    }

    /**
     * Converts a list of {@link Room} domain objects to a list of {@link RoomResponse} DTOs.
     *
     * @param rooms the domain objects (may be null)
     * @return an unmodifiable list of response DTOs, never null
     */
    public static List<RoomResponse> toResponseList(List<Room> rooms) {
        if (rooms == null) {
            return Collections.emptyList();
        }
        return rooms.stream()
                .map(RoomRestMapper::toResponse)
                .toList();
    }

    /**
     * Converts a {@link Room} domain object to a lightweight {@link InternalRoomResponse} DTO
     * for service-to-service communication.
     *
     * @param room the domain object (may be null)
     * @return the internal response DTO, or null if the domain object is null
     */
    public static InternalRoomResponse toInternalResponse(Room room) {
        if (room == null) {
            return null;
        }
        return new InternalRoomResponse(
                room.getId(),
                room.getName(),
                room.getModeratorId(),
                room.getStatus(),
                DeckTypeRestMapper.toResponse(room.getDeckType())
        );
    }
}
