package com.planningpoker.notification.infrastructure.client;

import com.planningpoker.notification.application.port.out.RoomQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private RoomServiceClient client;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        client = new RoomServiceClient(restClient);

        // Wire the fluent API chain: restClient.get() -> uri(...) -> retrieve() -> onStatus(...) -> body(...)
        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object[].class)))
                .thenReturn((RestClient.RequestHeadersSpec) requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    }

    // ═══════════════════════════════════════════════════════════════════
    // getParticipantUserIds — happy path
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnParticipantUserIds() {
        UUID roomId = UUID.randomUUID();
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        List<RoomServiceClient.InternalParticipantResponse> participants = List.of(
                new RoomServiceClient.InternalParticipantResponse(user1, "MODERATOR"),
                new RoomServiceClient.InternalParticipantResponse(user2, "VOTER")
        );

        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(participants);

        List<UUID> result = client.getParticipantUserIds(roomId);

        assertThat(result).containsExactly(user1, user2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnEmptyListWhenParticipantsResponseIsNull() {
        UUID roomId = UUID.randomUUID();

        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(null);

        List<UUID> result = client.getParticipantUserIds(roomId);

        assertThat(result).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnEmptyListWhenParticipantsFetchThrows() {
        UUID roomId = UUID.randomUUID();

        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        List<UUID> result = client.getParticipantUserIds(roomId);

        assertThat(result).isEmpty();
    }

    // ═══════════════════════════════════════════════════════════════════
    // getRoom — happy path
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldReturnRoomInfo() {
        UUID roomId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();

        RoomServiceClient.InternalRoomResponse response = new RoomServiceClient.InternalRoomResponse(
                roomId, "Sprint Planning", moderatorId, "ACTIVE"
        );

        when(responseSpec.body(eq(RoomServiceClient.InternalRoomResponse.class))).thenReturn(response);

        RoomQueryPort.RoomInfo result = client.getRoom(roomId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(roomId);
        assertThat(result.name()).isEqualTo("Sprint Planning");
        assertThat(result.moderatorId()).isEqualTo(moderatorId);
        assertThat(result.status()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldReturnNullWhenRoomResponseIsNull() {
        UUID roomId = UUID.randomUUID();

        when(responseSpec.body(eq(RoomServiceClient.InternalRoomResponse.class))).thenReturn(null);

        RoomQueryPort.RoomInfo result = client.getRoom(roomId);

        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullWhenRoomFetchThrows() {
        UUID roomId = UUID.randomUUID();

        when(responseSpec.body(eq(RoomServiceClient.InternalRoomResponse.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        RoomQueryPort.RoomInfo result = client.getRoom(roomId);

        assertThat(result).isNull();
    }

    // ═══════════════════════════════════════════════════════════════════
    // Internal DTOs
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldCreateInternalRoomResponseRecord() {
        UUID id = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();

        RoomServiceClient.InternalRoomResponse response = new RoomServiceClient.InternalRoomResponse(
                id, "My Room", moderatorId, "ACTIVE"
        );

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("My Room");
        assertThat(response.moderatorId()).isEqualTo(moderatorId);
        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldCreateInternalParticipantResponseRecord() {
        UUID userId = UUID.randomUUID();

        RoomServiceClient.InternalParticipantResponse response = new RoomServiceClient.InternalParticipantResponse(
                userId, "VOTER"
        );

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.role()).isEqualTo("VOTER");
    }
}
