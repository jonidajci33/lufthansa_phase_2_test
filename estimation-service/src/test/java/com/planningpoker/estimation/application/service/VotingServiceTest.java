package com.planningpoker.estimation.application.service;

import com.planningpoker.estimation.application.port.out.ActiveRoomCachePort;
import com.planningpoker.estimation.application.port.out.EstimationEventPublisherPort;
import com.planningpoker.estimation.application.port.out.RoomValidationPort;
import com.planningpoker.estimation.application.port.out.StoryPersistencePort;
import com.planningpoker.estimation.application.port.out.VotePersistencePort;
import com.planningpoker.estimation.application.port.out.VotingNotificationPort;
import com.planningpoker.estimation.domain.Story;
import com.planningpoker.estimation.domain.StoryStatus;
import com.planningpoker.estimation.domain.Vote;
import com.planningpoker.estimation.domain.VotingResult;
import com.planningpoker.estimation.web.dto.SubmitVoteRequest;
import com.planningpoker.shared.error.BusinessException;
import com.planningpoker.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
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
class VotingServiceTest {

    @Mock
    private StoryPersistencePort storyPersistencePort;

    @Mock
    private VotePersistencePort votePersistencePort;

    @Mock
    private RoomValidationPort roomValidationPort;

    @Mock
    private EstimationEventPublisherPort eventPublisherPort;

    @Mock
    private VotingNotificationPort votingNotificationPort;

    @Mock
    private ActiveRoomCachePort activeRoomCachePort;

    @InjectMocks
    private VotingService votingService;

    // ── Helpers ───────────────────────────────────────────────────────

    private static final UUID ROOM_ID = UUID.randomUUID();
    private static final UUID MODERATOR_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();

    private static Story pendingStory() {
        return Story.create(ROOM_ID, "Login Page", "Desc", 0);
    }

    private static Story votingStory() {
        Story story = pendingStory();
        story.startVoting();
        return story;
    }

    private static Story votedStory() {
        Story story = votingStory();
        UUID user1 = UUID.randomUUID();
        story.addVote(Vote.create(story.getId(), user1, "5", new BigDecimal("5")));
        story.finishVoting();
        return story;
    }

    // ═══════════════════════════════════════════════════════════════════
    // startVoting
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldStartVotingAsModerator() {
        Story story = pendingStory();
        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(roomValidationPort.isModeratorOf(ROOM_ID, MODERATOR_ID)).thenReturn(true);
        when(storyPersistencePort.save(any(Story.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Story result = votingService.startVoting(story.getId(), MODERATOR_ID);

        assertThat(result.getStatus()).isEqualTo(StoryStatus.VOTING);
        verify(activeRoomCachePort).cacheActiveStory(ROOM_ID, story.getId());
        verify(eventPublisherPort).publishVotingStarted(result, MODERATOR_ID);
        verify(votePersistencePort).deleteByStoryId(story.getId());
    }

    @Test
    void shouldThrowWhenNonModeratorStartsVoting() {
        Story story = pendingStory();
        UUID nonModerator = UUID.randomUUID();
        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(roomValidationPort.isModeratorOf(ROOM_ID, nonModerator)).thenReturn(false);

        assertThatThrownBy(() -> votingService.startVoting(story.getId(), nonModerator))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("NOT_MODERATOR");
                });

        verify(storyPersistencePort, never()).save(any());
        verifyNoInteractions(activeRoomCachePort);
    }

    @Test
    void shouldThrowWhenStartingVotingOnAlreadyVotingStory() {
        Story story = votingStory();
        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(roomValidationPort.isModeratorOf(ROOM_ID, MODERATOR_ID)).thenReturn(true);

        assertThatThrownBy(() -> votingService.startVoting(story.getId(), MODERATOR_ID))
                .isInstanceOf(IllegalStateException.class);

        verify(storyPersistencePort, never()).save(any());
    }

    @Test
    void shouldPublishEventOnStartVoting() {
        Story story = pendingStory();
        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(roomValidationPort.isModeratorOf(ROOM_ID, MODERATOR_ID)).thenReturn(true);
        when(storyPersistencePort.save(any(Story.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Story result = votingService.startVoting(story.getId(), MODERATOR_ID);

        verify(eventPublisherPort).publishVotingStarted(result, MODERATOR_ID);
    }

    @Test
    void shouldCacheActiveStoryOnStartVoting() {
        Story story = pendingStory();
        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(roomValidationPort.isModeratorOf(ROOM_ID, MODERATOR_ID)).thenReturn(true);
        when(storyPersistencePort.save(any(Story.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        votingService.startVoting(story.getId(), MODERATOR_ID);

        verify(activeRoomCachePort).cacheActiveStory(ROOM_ID, story.getId());
    }

    // ═══════════════════════════════════════════════════════════════════
    // submitVote
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldSubmitVoteSuccessfully() {
        Story story = votingStory();
        SubmitVoteRequest request = new SubmitVoteRequest("5", new BigDecimal("5"));

        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(votePersistencePort.findByStoryIdAndUserId(story.getId(), USER_ID))
                .thenReturn(Optional.empty());
        when(votePersistencePort.save(any(Vote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Vote result = votingService.submitVote(request, story.getId(), USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEqualTo("5");
        verify(votingNotificationPort).notifyVoteSubmitted(eq(ROOM_ID), eq(story.getId()), eq(1));
        verify(eventPublisherPort).publishVoteSubmitted(eq(story.getId()), eq(ROOM_ID), eq(1));
    }

    @Test
    void shouldReplaceExistingVoteFromSameUser() {
        Story story = votingStory();
        UUID existingVoteId = UUID.randomUUID();
        Vote existingVote = new Vote(existingVoteId, story.getId(), USER_ID, "3",
                new BigDecimal("3"), true, Instant.now(), Instant.now());

        SubmitVoteRequest request = new SubmitVoteRequest("8", new BigDecimal("8"));

        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(votePersistencePort.findByStoryIdAndUserId(story.getId(), USER_ID))
                .thenReturn(Optional.of(existingVote));
        when(votePersistencePort.save(any(Vote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Vote result = votingService.submitVote(request, story.getId(), USER_ID);

        assertThat(result.getId()).isEqualTo(existingVoteId);
        assertThat(result.getValue()).isEqualTo("8");
    }

    @Test
    void shouldThrowWhenSubmittingVoteOnNonVotingStory() {
        Story story = pendingStory();
        SubmitVoteRequest request = new SubmitVoteRequest("5", new BigDecimal("5"));

        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));

        assertThatThrownBy(() -> votingService.submitVote(request, story.getId(), USER_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("VOTING_NOT_OPEN");
                });

        verify(votePersistencePort, never()).save(any());
    }

    @Test
    void shouldNotifyWebSocketOnVoteSubmit() {
        Story story = votingStory();
        SubmitVoteRequest request = new SubmitVoteRequest("5", new BigDecimal("5"));

        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(votePersistencePort.findByStoryIdAndUserId(story.getId(), USER_ID))
                .thenReturn(Optional.empty());
        when(votePersistencePort.save(any(Vote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        votingService.submitVote(request, story.getId(), USER_ID);

        verify(votingNotificationPort).notifyVoteSubmitted(eq(ROOM_ID), eq(story.getId()), eq(1));
    }

    @Test
    void shouldPublishEventOnVoteSubmit() {
        Story story = votingStory();
        SubmitVoteRequest request = new SubmitVoteRequest("5", new BigDecimal("5"));

        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(votePersistencePort.findByStoryIdAndUserId(story.getId(), USER_ID))
                .thenReturn(Optional.empty());
        when(votePersistencePort.save(any(Vote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        votingService.submitVote(request, story.getId(), USER_ID);

        verify(eventPublisherPort).publishVoteSubmitted(eq(story.getId()), eq(ROOM_ID), eq(1));
    }

    // ═══════════════════════════════════════════════════════════════════
    // finishVoting
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldFinishVotingAsModerator() {
        Story story = votingStory();
        Vote vote = Vote.create(story.getId(), USER_ID, "5", new BigDecimal("5"));
        List<Vote> votes = List.of(vote);

        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(roomValidationPort.isModeratorOf(ROOM_ID, MODERATOR_ID)).thenReturn(true);
        when(votePersistencePort.findByStoryId(story.getId())).thenReturn(votes);
        when(storyPersistencePort.save(any(Story.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VotingResult result = votingService.finishVoting(story.getId(), MODERATOR_ID);

        assertThat(result).isNotNull();
        assertThat(result.storyId()).isEqualTo(story.getId());
        assertThat(result.totalVotes()).isEqualTo(1);
        verify(votingNotificationPort).notifyVotingResults(eq(ROOM_ID), any(VotingResult.class));
        verify(activeRoomCachePort).evictActiveStory(ROOM_ID);
    }

    @Test
    void shouldThrowWhenNonModeratorFinishesVoting() {
        Story story = votingStory();
        UUID nonModerator = UUID.randomUUID();
        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(roomValidationPort.isModeratorOf(ROOM_ID, nonModerator)).thenReturn(false);

        assertThatThrownBy(() -> votingService.finishVoting(story.getId(), nonModerator))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("NOT_MODERATOR");
                });

        verify(storyPersistencePort, never()).save(any());
    }

    @Test
    void shouldThrowWhenFinishingVotingOnNonVotingStory() {
        Story story = pendingStory();
        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(roomValidationPort.isModeratorOf(ROOM_ID, MODERATOR_ID)).thenReturn(true);
        when(votePersistencePort.findByStoryId(story.getId())).thenReturn(List.of());

        assertThatThrownBy(() -> votingService.finishVoting(story.getId(), MODERATOR_ID))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldEvictCacheOnFinishVoting() {
        Story story = votingStory();
        Vote vote = Vote.create(story.getId(), USER_ID, "5", new BigDecimal("5"));

        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(roomValidationPort.isModeratorOf(ROOM_ID, MODERATOR_ID)).thenReturn(true);
        when(votePersistencePort.findByStoryId(story.getId())).thenReturn(List.of(vote));
        when(storyPersistencePort.save(any(Story.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        votingService.finishVoting(story.getId(), MODERATOR_ID);

        verify(activeRoomCachePort).evictActiveStory(ROOM_ID);
    }

    @Test
    void shouldPublishEventOnFinishVoting() {
        Story story = votingStory();
        Vote vote = Vote.create(story.getId(), USER_ID, "5", new BigDecimal("5"));

        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(roomValidationPort.isModeratorOf(ROOM_ID, MODERATOR_ID)).thenReturn(true);
        when(votePersistencePort.findByStoryId(story.getId())).thenReturn(List.of(vote));
        when(storyPersistencePort.save(any(Story.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        votingService.finishVoting(story.getId(), MODERATOR_ID);

        verify(eventPublisherPort).publishVotingFinished(
                eq(story.getId()), eq(ROOM_ID), any(VotingResult.class));
    }

    @Test
    void shouldNotifyWebSocketWithResultsOnFinish() {
        Story story = votingStory();
        Vote vote = Vote.create(story.getId(), USER_ID, "5", new BigDecimal("5"));

        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(roomValidationPort.isModeratorOf(ROOM_ID, MODERATOR_ID)).thenReturn(true);
        when(votePersistencePort.findByStoryId(story.getId())).thenReturn(List.of(vote));
        when(storyPersistencePort.save(any(Story.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        votingService.finishVoting(story.getId(), MODERATOR_ID);

        verify(votingNotificationPort).notifyVotingResults(eq(ROOM_ID), any(VotingResult.class));
    }

    // ═══════════════════════════════════════════════════════════════════
    // getVotes
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void shouldReturnVotesWhenVoted() {
        Story story = votedStory();
        Vote vote = Vote.create(story.getId(), USER_ID, "5", new BigDecimal("5"));
        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));
        when(votePersistencePort.findByStoryId(story.getId())).thenReturn(List.of(vote));

        List<Vote> result = votingService.getVotes(story.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValue()).isEqualTo("5");
    }

    @Test
    void shouldThrowWhenGettingVotesBeforeVotingFinished() {
        Story story = votingStory();
        when(storyPersistencePort.findById(story.getId())).thenReturn(Optional.of(story));

        assertThatThrownBy(() -> votingService.getVotes(story.getId()))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException bex = (BusinessException) ex;
                    assertThat(bex.getCode()).isEqualTo("VOTING_NOT_FINISHED");
                });
    }

    @Test
    void shouldThrowWhenStoryNotFoundForGetVotes() {
        UUID missingId = UUID.randomUUID();
        when(storyPersistencePort.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> votingService.getVotes(missingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
