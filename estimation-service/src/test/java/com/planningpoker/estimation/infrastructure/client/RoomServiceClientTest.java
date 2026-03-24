package com.planningpoker.estimation.infrastructure.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;
import org.springframework.web.client.RestClient.RequestHeadersSpec;
import org.springframework.web.client.RestClient.ResponseSpec;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceClientTest {

    @Mock
    private RestClient restClient;

    @Mock(extraInterfaces = RequestHeadersSpec.class)
    private RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private ResponseSpec responseSpec;

    private RoomServiceClient client;

    private static final UUID ROOM_ID = UUID.fromString("aaaa1111-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID USER_ID = UUID.fromString("bbbb2222-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID MODERATOR_ID = UUID.fromString("cccc3333-cccc-cccc-cccc-cccccccccccc");

    @BeforeEach
    void setUp() {
        client = new RoomServiceClient(restClient);
    }

    @SuppressWarnings("unchecked")
    private void stubGetChain() {
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersUriSpec).when(requestHeadersUriSpec).uri(anyString(), any(Object[].class));
        doReturn(responseSpec).when((RequestHeadersSpec<?>) requestHeadersUriSpec).retrieve();
        doReturn(responseSpec).when(responseSpec).onStatus(any(), any());
    }

    // ═══════════════════════════════════════════════════════════════════
    // roomExists
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    class RoomExists {

        @Test
        void shouldReturnTrue_whenRoomExists() {
            stubGetChain();
            ResponseEntity<Void> okResponse = ResponseEntity.ok().build();
            when(responseSpec.toBodilessEntity()).thenReturn(okResponse);

            boolean result = client.roomExists(ROOM_ID);

            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnFalse_whenRoomNotFound() {
            stubGetChain();
            ResponseEntity<Void> notFoundResponse = ResponseEntity.status(404).build();
            when(responseSpec.toBodilessEntity()).thenReturn(notFoundResponse);

            boolean result = client.roomExists(ROOM_ID);

            assertThat(result).isFalse();
        }

        @Test
        void shouldReturnFalse_whenExceptionOccurs() {
            stubGetChain();
            when(responseSpec.toBodilessEntity()).thenThrow(new RuntimeException("Connection refused"));

            boolean result = client.roomExists(ROOM_ID);

            assertThat(result).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // isModeratorOf
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    class IsModeratorOf {

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnTrue_whenUserIsModerator() {
            stubGetChain();
            Object roomResponse = createInternalRoomResponse(ROOM_ID, "Test Room", MODERATOR_ID, "ACTIVE");
            when(responseSpec.body(any(Class.class))).thenReturn(roomResponse);

            boolean result = client.isModeratorOf(ROOM_ID, MODERATOR_ID);

            assertThat(result).isTrue();
        }

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnFalse_whenUserIsNotModerator() {
            stubGetChain();
            Object roomResponse = createInternalRoomResponse(ROOM_ID, "Test Room", MODERATOR_ID, "ACTIVE");
            when(responseSpec.body(any(Class.class))).thenReturn(roomResponse);

            boolean result = client.isModeratorOf(ROOM_ID, USER_ID);

            assertThat(result).isFalse();
        }

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnFalse_whenRoomNotFound() {
            stubGetChain();
            when(responseSpec.body(any(Class.class))).thenReturn(null);

            boolean result = client.isModeratorOf(ROOM_ID, USER_ID);

            assertThat(result).isFalse();
        }

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnFalse_whenExceptionOccurs() {
            stubGetChain();
            when(responseSpec.body(any(Class.class))).thenThrow(new RuntimeException("Timeout"));

            boolean result = client.isModeratorOf(ROOM_ID, USER_ID);

            assertThat(result).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // getParticipantUserIds
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    class GetParticipantUserIds {

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnUserIds_whenParticipantsExist() {
            stubGetChain();
            UUID user1 = UUID.randomUUID();
            UUID user2 = UUID.randomUUID();
            List<?> participants = List.of(
                    createInternalParticipantResponse(user1, "MODERATOR"),
                    createInternalParticipantResponse(user2, "PARTICIPANT")
            );
            when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(participants);

            List<UUID> result = client.getParticipantUserIds(ROOM_ID);

            assertThat(result).containsExactly(user1, user2);
        }

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnEmptyList_whenNoParticipants() {
            stubGetChain();
            when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(null);

            List<UUID> result = client.getParticipantUserIds(ROOM_ID);

            assertThat(result).isEmpty();
        }

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnEmptyList_whenExceptionOccurs() {
            stubGetChain();
            when(responseSpec.body(any(ParameterizedTypeReference.class)))
                    .thenThrow(new RuntimeException("Service unavailable"));

            List<UUID> result = client.getParticipantUserIds(ROOM_ID);

            assertThat(result).isEmpty();
        }

        @SuppressWarnings("unchecked")
        @Test
        void shouldReturnEmptyList_whenEmptyParticipantsList() {
            stubGetChain();
            when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(List.of());

            List<UUID> result = client.getParticipantUserIds(ROOM_ID);

            assertThat(result).isEmpty();
        }
    }

    // ── Reflection helpers to construct private record instances ──────

    private static Object createInternalRoomResponse(UUID id, String name, UUID moderatorId, String status) {
        try {
            Class<?> clazz = Class.forName(
                    "com.planningpoker.estimation.infrastructure.client.RoomServiceClient$InternalRoomResponse");
            var constructor = clazz.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            return constructor.newInstance(id, name, moderatorId, status);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create InternalRoomResponse", e);
        }
    }

    private static Object createInternalParticipantResponse(UUID userId, String role) {
        try {
            Class<?> clazz = Class.forName(
                    "com.planningpoker.estimation.infrastructure.client.RoomServiceClient$InternalParticipantResponse");
            var constructor = clazz.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            return constructor.newInstance(userId, role);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create InternalParticipantResponse", e);
        }
    }
}
