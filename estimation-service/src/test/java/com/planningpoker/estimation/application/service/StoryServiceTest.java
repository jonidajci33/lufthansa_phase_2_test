package com.planningpoker.estimation.application.service;

import com.planningpoker.estimation.application.port.out.EstimationEventPublisherPort;
import com.planningpoker.estimation.application.port.out.RoomValidationPort;
import com.planningpoker.estimation.application.port.out.StoryPersistencePort;
import com.planningpoker.estimation.application.port.out.VotingNotificationPort;
import com.planningpoker.estimation.domain.Page;
import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.domain.StoryStatus;
import com.planningpoker.estimation.web.dto.CreateStoryRequest;
import com.planningpoker.estimation.web.dto.UpdateStoryRequest;
import com.planningpoker.shared.error.BusinessException;
import com.planningpoker.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoryServiceTest {

    @Mock
    private StoryPersistencePort storyPersistencePort;

    @Mock
    private RoomValidationPort roomValidationPort;

    @Mock
    private EstimationEventPublisherPort eventPublisherPort;

    @Mock
    private VotingNotificationPort notificationPort;

    @InjectMocks
    private StoryService storyService;

    // ── Helpers ───────────────────────────────────────────────────────

    private static final UUID ROOM_ID = UUID.randomUUID();
    private static final UUID MODERATOR_ID = UUID.randomUUID();

    private static Story existingStory() {
        return Story.create(ROOM_ID, "Login Page", "Estimate the login page", 0);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Create
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldCreateStorySuccessfully() {
        CreateStoryRequest request = new CreateStoryRequest(
                ROOM_ID, "Login Page", "Estimate the login page");

        when(roomValidationPort.roomExists(ROOM_ID)).thenReturn(true);
        when(roomValidationPort.isModeratorOf(ROOM_ID, MODERATOR_ID)).thenReturn(true);
        when(storyPersistencePort.countByRoomId(ROOM_ID)).thenReturn(0L);
        when(storyPersistencePort.save(any(Story.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Story result = storyService.create(request, MODERATOR_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Login Page");
        assertThat(result.getRoomId()).isEqualTo(ROOM_ID);
        assertThat(result.getStatus()).isEqualTo(StoryStatus.PENDING);
        assertThat(result.getSortOrder()).isZero();

        verify(storyPersistencePort).save(any(Story.class));
        verify(eventPublisherPort).publishStoryCreated(result);
    }

    @Test
    void shouldThrowWhenNonModeratorCreatesStory() {
        UUID nonModerator = UUID.randomUUID();
        CreateStoryRequest request = new CreateStoryRequest(
                ROOM_ID, "Login Page", null);

        when(roomValidationPort.roomExists(ROOM_ID)).thenReturn(true);
        when(roomValidationPort.isModeratorOf(ROOM_ID, nonModerator)).thenReturn(false);

        assertThatThrownBy(() -> storyService.create(request, nonModerator))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("NOT_MODERATOR");
                });

        verify(storyPersistencePort, never()).save(any());
        verifyNoInteractions(eventPublisherPort);
    }

    @Test
    void shouldPublishEventOnCreate() {
        CreateStoryRequest request = new CreateStoryRequest(
                ROOM_ID, "Story", null);

        when(roomValidationPort.roomExists(ROOM_ID)).thenReturn(true);
        when(roomValidationPort.isModeratorOf(ROOM_ID, MODERATOR_ID)).thenReturn(true);
        when(storyPersistencePort.countByRoomId(ROOM_ID)).thenReturn(0L);
        when(storyPersistencePort.save(any(Story.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Story result = storyService.create(request, MODERATOR_ID);

        verify(eventPublisherPort).publishStoryCreated(result);
    }

    @Test
    void shouldAssignSortOrderBasedOnExistingCount() {
        CreateStoryRequest request = new CreateStoryRequest(
                ROOM_ID, "Third Story", null);

        when(roomValidationPort.roomExists(ROOM_ID)).thenReturn(true);
        when(roomValidationPort.isModeratorOf(ROOM_ID, MODERATOR_ID)).thenReturn(true);
        when(storyPersistencePort.countByRoomId(ROOM_ID)).thenReturn(2L);
        when(storyPersistencePort.save(any(Story.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Story result = storyService.create(request, MODERATOR_ID);

        assertThat(result.getSortOrder()).isEqualTo(2);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Update
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldUpdateStoryByModerator() {
        Story story = existingStory();
        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(roomValidationPort.isModeratorOf(ROOM_ID, MODERATOR_ID)).thenReturn(true);
        when(storyPersistencePort.save(any(Story.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateStoryRequest request = new UpdateStoryRequest("Updated Title", "Updated Desc");
        Story result = storyService.update(story.getId(), request, MODERATOR_ID);

        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getDescription()).isEqualTo("Updated Desc");
        verify(eventPublisherPort).publishStoryUpdated(result);
    }

    @Test
    void shouldThrowWhenNonModeratorUpdates() {
        Story story = existingStory();
        UUID nonModerator = UUID.randomUUID();
        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(roomValidationPort.isModeratorOf(ROOM_ID, nonModerator)).thenReturn(false);

        UpdateStoryRequest request = new UpdateStoryRequest("New", null);

        assertThatThrownBy(() -> storyService.update(story.getId(), request, nonModerator))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("NOT_MODERATOR");
                });

        verify(storyPersistencePort, never()).save(any());
    }

    @Test
    void shouldPublishEventOnUpdate() {
        Story story = existingStory();
        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(roomValidationPort.isModeratorOf(ROOM_ID, MODERATOR_ID)).thenReturn(true);
        when(storyPersistencePort.save(any(Story.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateStoryRequest request = new UpdateStoryRequest("New", null);
        Story result = storyService.update(story.getId(), request, MODERATOR_ID);

        verify(eventPublisherPort).publishStoryUpdated(result);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Delete
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldDeleteStoryByModerator() {
        Story story = existingStory();
        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(roomValidationPort.isModeratorOf(ROOM_ID, MODERATOR_ID)).thenReturn(true);

        storyService.delete(story.getId(), MODERATOR_ID);

        verify(storyPersistencePort).deleteById(story.getId());
        verify(eventPublisherPort).publishStoryDeleted(story.getId(), ROOM_ID);
    }

    @Test
    void shouldThrowWhenNonModeratorDeletes() {
        Story story = existingStory();
        UUID nonModerator = UUID.randomUUID();
        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(roomValidationPort.isModeratorOf(ROOM_ID, nonModerator)).thenReturn(false);

        assertThatThrownBy(() -> storyService.delete(story.getId(), nonModerator))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("NOT_MODERATOR");
                });

        verify(storyPersistencePort, never()).deleteById(any());
    }

    @Test
    void shouldPublishEventOnDelete() {
        Story story = existingStory();
        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(roomValidationPort.isModeratorOf(ROOM_ID, MODERATOR_ID)).thenReturn(true);

        storyService.delete(story.getId(), MODERATOR_ID);

        verify(eventPublisherPort).publishStoryDeleted(story.getId(), ROOM_ID);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GetById
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldReturnStoryWhenExists() {
        Story story = existingStory();
        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));

        Story result = storyService.getById(story.getId());

        assertThat(result).isEqualTo(story);
    }

    @Test
    void shouldThrowWhenStoryNotFound() {
        UUID id = UUID.randomUUID();
        when(storyPersistencePort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storyService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ═══════════════════════════════════════════════════════════════════
    // List
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldReturnPaginatedStories() {
        Story story = existingStory();
        Page<Story> page = new Page<>(List.of(story), 1L);
        when(storyPersistencePort.findByRoomId(ROOM_ID, 0, 20)).thenReturn(page);

        Page<Story> result = storyService.listByRoom(ROOM_ID, 0, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1L);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Reorder
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldReorderStoriesByModerator() {
        Story story1 = Story.create(ROOM_ID, "Story A", null, 0);
        Story story2 = Story.create(ROOM_ID, "Story B", null, 1);
        List<Story> stories = new ArrayList<>(List.of(story1, story2));

        when(roomValidationPort.isModeratorOf(ROOM_ID, MODERATOR_ID)).thenReturn(true);
        when(storyPersistencePort.findByRoomIdOrderBySortOrder(ROOM_ID)).thenReturn(stories);
        when(storyPersistencePort.save(any(Story.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Reverse order
        List<UUID> reorderedIds = List.of(story2.getId(), story1.getId());
        storyService.reorder(ROOM_ID, reorderedIds, MODERATOR_ID);

        assertThat(story2.getSortOrder()).isZero();
        assertThat(story1.getSortOrder()).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenNonModeratorReorders() {
        UUID nonModerator = UUID.randomUUID();
        when(roomValidationPort.isModeratorOf(ROOM_ID, nonModerator)).thenReturn(false);

        assertThatThrownBy(() -> storyService.reorder(ROOM_ID, List.of(UUID.randomUUID()), nonModerator))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("NOT_MODERATOR");
                });
    }
}
