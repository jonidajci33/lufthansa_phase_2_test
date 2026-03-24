package com.planningpoker.room.web.mapper;

import com.planningpoker.room.domain.Invitation;
import com.planningpoker.room.web.dto.InvitationResponse;

import java.util.Collections;
import java.util.List;

/**
 * Static mapper from {@link Invitation} domain objects to {@link InvitationResponse} DTOs.
 * <p>
 * Pure utility class — no framework imports, no instantiation.
 */
public final class InvitationRestMapper {

    private InvitationRestMapper() {
        // utility class
    }

    /**
     * Converts an {@link Invitation} domain object to an {@link InvitationResponse} DTO.
     *
     * @param invitation the domain object (may be null)
     * @return the response DTO, or null if the domain object is null
     */
    public static InvitationResponse toResponse(Invitation invitation) {
        if (invitation == null) {
            return null;
        }
        return new InvitationResponse(
                invitation.getId(),
                invitation.getRoomId(),
                invitation.getEmail(),
                invitation.getToken(),
                invitation.getType(),
                invitation.getStatus(),
                invitation.getExpiresAt()
        );
    }

    /**
     * Converts a list of {@link Invitation} domain objects to a list of {@link InvitationResponse} DTOs.
     *
     * @param invitations the domain objects (may be null)
     * @return an unmodifiable list of response DTOs, never null
     */
    public static List<InvitationResponse> toResponseList(List<Invitation> invitations) {
        if (invitations == null) {
            return Collections.emptyList();
        }
        return invitations.stream()
                .map(InvitationRestMapper::toResponse)
                .toList();
    }
}
