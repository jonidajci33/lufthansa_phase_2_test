package com.planningpoker.estimation.application.service;

import com.planningpoker.estimation.application.port.in.CreateStoryUseCase;
import com.planningpoker.estimation.application.port.in.DeleteStoryUseCase;
import com.planningpoker.estimation.application.port.in.GetStoryUseCase;
import com.planningpoker.estimation.application.port.in.ListStoriesUseCase;
import com.planningpoker.estimation.application.port.in.ReorderStoriesUseCase;
import com.planningpoker.estimation.application.port.in.UpdateStoryUseCase;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service that orchestrates story-related use cases.
 * <p>
 * Coordinates persistence, event publishing, and room validation
 * without leaking infrastructure details into the domain model.
 */
@Service
@Transactional
public class StoryService implements CreateStoryUseCase, GetStoryUseCase,
        ListStoriesUseCase, UpdateStoryUseCase, DeleteStoryUseCase,
        ReorderStoriesUseCase {

    private final StoryPersistencePort storyPersistencePort;
    private final RoomValidationPort roomValidationPort;
    private final EstimationEventPublisherPort eventPublisherPort;
    private final VotingNotificationPort notificationPort;

    public StoryService(StoryPersistencePort storyPersistencePort,
                        RoomValidationPort roomValidationPort,
                        EstimationEventPublisherPort eventPublisherPort,
                        VotingNotificationPort notificationPort) {
        this.storyPersistencePort = storyPersistencePort;
        this.roomValidationPort = roomValidationPort;
        this.eventPublisherPort = eventPublisherPort;
        this.notificationPort = notificationPort;
    }

    // -- CreateStoryUseCase -----------------------------------------------

    @Override
    public Story create(CreateStoryRequest request, UUID requesterId) {
        if (!roomValidationPort.roomExists(request.roomId())) {
            throw new ResourceNotFoundException("Room", request.roomId());
        }

        if (!roomValidationPort.isModeratorOf(request.roomId(), requesterId)) {
            throw new BusinessException("NOT_MODERATOR",
                    "Only the room moderator can create stories");
        }

        int sortOrder = (int) storyPersistencePort.countByRoomId(request.roomId());
        Story story = Story.create(request.roomId(), request.title(),
                request.description(), sortOrder);

        Story saved = storyPersistencePort.save(story);
        notificationPort.notifyStoryAdded(saved.getRoomId(), saved);
        eventPublisherPort.publishStoryCreated(saved);
        return saved;
    }

    // -- GetStoryUseCase --------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Story getById(UUID id) {
        return storyPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Story", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Story> getByRoomId(UUID roomId) {
        return storyPersistencePort.findByRoomIdOrderBySortOrder(roomId);
    }

    // -- ListStoriesUseCase -----------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<Story> listByRoom(UUID roomId, int offset, int limit) {
        return storyPersistencePort.findByRoomId(roomId, offset, limit);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Story> listAll(int offset, int limit) {
        return storyPersistencePort.findAll(offset, limit);
    }

    // -- UpdateStoryUseCase -----------------------------------------------

    @Override
    public Story update(UUID id, UpdateStoryRequest request, UUID requesterId) {
        Story story = storyPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Story", id));

        if (!roomValidationPort.isModeratorOf(story.getRoomId(), requesterId)) {
            throw new BusinessException("NOT_MODERATOR",
                    "Only the room moderator can update stories");
        }

        story.update(request.title(), request.description());

        Story saved = storyPersistencePort.save(story);
        notificationPort.notifyStoryUpdated(saved.getRoomId(), saved);
        eventPublisherPort.publishStoryUpdated(saved);
        return saved;
    }

    // -- DeleteStoryUseCase -----------------------------------------------

    @Override
    public void delete(UUID id, UUID requesterId) {
        Story story = storyPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Story", id));

        if (!roomValidationPort.isModeratorOf(story.getRoomId(), requesterId)) {
            throw new BusinessException("NOT_MODERATOR",
                    "Only the room moderator can delete stories");
        }

        if (story.getStatus() != StoryStatus.PENDING) {
            throw new BusinessException("STORY_NOT_PENDING",
                    "Only stories with PENDING status can be deleted");
        }

        storyPersistencePort.deleteById(id);
        notificationPort.notifyStoryDeleted(story.getRoomId(), id);
        eventPublisherPort.publishStoryDeleted(id, story.getRoomId());
    }

    // -- ReorderStoriesUseCase --------------------------------------------

    @Override
    public void reorder(UUID roomId, List<UUID> storyIds, UUID requesterId) {
        if (!roomValidationPort.isModeratorOf(roomId, requesterId)) {
            throw new BusinessException("NOT_MODERATOR",
                    "Only the room moderator can reorder stories");
        }

        List<Story> stories = storyPersistencePort.findByRoomIdOrderBySortOrder(roomId);

        if (stories.size() != storyIds.size()) {
            throw new BusinessException("INVALID_REORDER",
                    "Story ID list size does not match the number of stories in the room");
        }

        for (int i = 0; i < storyIds.size(); i++) {
            UUID storyId = storyIds.get(i);
            Story story = stories.stream()
                    .filter(s -> s.getId().equals(storyId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("STORY_NOT_IN_ROOM",
                            "Story " + storyId + " does not belong to room " + roomId));
            story.setSortOrder(i);
            storyPersistencePort.save(story);
        }
    }
}
