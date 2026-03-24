package com.planningpoker.room.web;

import com.planningpoker.room.application.port.in.GetRoomUseCase;
import com.planningpoker.room.application.port.in.ListParticipantsUseCase;
import com.planningpoker.room.domain.Room;
import com.planningpoker.room.domain.RoomParticipant;
import com.planningpoker.room.web.dto.InternalParticipantResponse;
import com.planningpoker.room.web.dto.InternalRoomResponse;
import com.planningpoker.room.web.mapper.ParticipantRestMapper;
import com.planningpoker.room.web.mapper.RoomRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Internal REST controller for service-to-service room lookups.
 * <p>
 * No authentication required — access is controlled at the SecurityConfig level
 * via {@code permitAll} on {@code /internal/**}.
 */
@RestController
@RequestMapping("/internal/rooms")
public class InternalRoomController {

    private final GetRoomUseCase getRoomUseCase;
    private final ListParticipantsUseCase listParticipantsUseCase;

    public InternalRoomController(GetRoomUseCase getRoomUseCase,
                                   ListParticipantsUseCase listParticipantsUseCase) {
        this.getRoomUseCase = getRoomUseCase;
        this.listParticipantsUseCase = listParticipantsUseCase;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get room by ID (internal)", description = "Lightweight room lookup for service-to-service communication.")
    @ApiResponse(responseCode = "200", description = "Room found")
    @ApiResponse(responseCode = "404", description = "Room not found")
    public ResponseEntity<InternalRoomResponse> getById(@PathVariable UUID id) {
        Room room = getRoomUseCase.getById(id);
        return ResponseEntity.ok(RoomRestMapper.toInternalResponse(room));
    }

    @GetMapping("/{id}/participants")
    @Operation(summary = "List room participants (internal)", description = "Lightweight participant list for service-to-service communication.")
    @ApiResponse(responseCode = "200", description = "Participant list")
    @ApiResponse(responseCode = "404", description = "Room not found")
    public ResponseEntity<List<InternalParticipantResponse>> listParticipants(@PathVariable UUID id) {
        List<RoomParticipant> participants = listParticipantsUseCase.listParticipants(id);
        List<InternalParticipantResponse> response = participants.stream()
                .map(ParticipantRestMapper::toInternalResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
}
