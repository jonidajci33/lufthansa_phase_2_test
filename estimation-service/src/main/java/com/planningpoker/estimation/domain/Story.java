package com.planningpoker.estimation.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing a story to be estimated in a planning poker session.
 */
public class Story {

    private UUID id;
    private UUID roomId;
    private String title;
    private String description;
    private StoryStatus status;
    private int sortOrder;
    private BigDecimal finalScore;
    private boolean consensusReached;
    private List<Vote> votes;
    private Instant createdAt;
    private Instant updatedAt;

    public Story() {
        this.votes = new ArrayList<>();
    }

    public Story(UUID id, UUID roomId, String title, String description, StoryStatus status,
                 int sortOrder, BigDecimal finalScore, boolean consensusReached,
                 List<Vote> votes, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.roomId = roomId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.sortOrder = sortOrder;
        this.finalScore = finalScore;
        this.consensusReached = consensusReached;
        this.votes = votes != null ? new ArrayList<>(votes) : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ── Factory ───────────────────────────────────────────────────────

    public static Story create(UUID roomId, String title, String description, int sortOrder) {
        Instant now = Instant.now();
        return new Story(UUID.randomUUID(), roomId, title, description,
                StoryStatus.PENDING, sortOrder, null, false,
                new ArrayList<>(), now, now);
    }

    // ── Business methods ─────────────────────────────────────────────

    /**
     * Transitions this story to VOTING status. Clears any previous votes.
     *
     * @throws IllegalStateException if current status is not PENDING
     */
    public void startVoting() {
        if (this.status != StoryStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot start voting on story with status " + this.status + "; expected PENDING");
        }
        this.status = StoryStatus.VOTING;
        this.votes.clear();
        this.finalScore = null;
        this.consensusReached = false;
        this.updatedAt = Instant.now();
    }

    /**
     * Finishes voting: transitions from VOTING to VOTED, calculates
     * the average of numeric votes, and determines consensus.
     *
     * @throws IllegalStateException if current status is not VOTING
     */
    public void finishVoting() {
        if (this.status != StoryStatus.VOTING) {
            throw new IllegalStateException(
                    "Cannot finish voting on story with status " + this.status + "; expected VOTING");
        }
        this.status = StoryStatus.VOTED;
        this.finalScore = calculateAverage();
        this.consensusReached = determineConsensus();
        this.updatedAt = Instant.now();
    }

    /**
     * Returns {@code true} if the story is currently accepting votes.
     */
    public boolean isVotingOpen() {
        return this.status == StoryStatus.VOTING;
    }

    /**
     * Updates the editable fields of this story. Only allowed while PENDING.
     *
     * @param title       new title (may be null to keep current)
     * @param description new description (may be null to keep current)
     * @throws IllegalStateException if current status is not PENDING
     */
    public void update(String title, String description) {
        if (this.status != StoryStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot update story with status " + this.status + "; expected PENDING");
        }
        if (title != null) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Adds a vote to this story. If the user has already voted, the
     * existing vote is replaced. Only allowed while VOTING.
     *
     * @param vote the vote to add
     * @throws IllegalStateException if current status is not VOTING
     */
    public void addVote(Vote vote) {
        Objects.requireNonNull(vote, "vote must not be null");
        if (this.status != StoryStatus.VOTING) {
            throw new IllegalStateException(
                    "Cannot add vote to story with status " + this.status + "; expected VOTING");
        }
        // Replace existing vote from the same user
        this.votes.removeIf(v -> Objects.equals(v.getUserId(), vote.getUserId()));
        this.votes.add(vote);
    }

    /**
     * Returns the number of votes cast on this story.
     */
    public int getVoteCount() {
        return this.votes.size();
    }

    /**
     * Calculates the average of all numeric votes, ignoring non-numeric
     * values (e.g. "?", "coffee"). Returns {@code null} if no numeric
     * votes exist.
     */
    public BigDecimal calculateAverage() {
        List<BigDecimal> numericVotes = this.votes.stream()
                .map(Vote::getNumericValue)
                .filter(Objects::nonNull)
                .toList();

        if (numericVotes.isEmpty()) {
            return null;
        }

        BigDecimal sum = numericVotes.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(numericVotes.size()), 2, java.math.RoundingMode.HALF_UP);
    }

    // ── Private helpers ──────────────────────────────────────────────

    /**
     * Consensus is reached when all numeric votes have the same value.
     */
    private boolean determineConsensus() {
        List<BigDecimal> numericVotes = this.votes.stream()
                .map(Vote::getNumericValue)
                .filter(Objects::nonNull)
                .toList();

        if (numericVotes.size() < 2) {
            return numericVotes.size() == 1;
        }

        BigDecimal first = numericVotes.get(0);
        return numericVotes.stream().allMatch(v -> v.compareTo(first) == 0);
    }

    // ── Getters & setters ────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public StoryStatus getStatus() {
        return status;
    }

    public void setStatus(StoryStatus status) {
        this.status = status;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public BigDecimal getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(BigDecimal finalScore) {
        this.finalScore = finalScore;
    }

    public boolean isConsensusReached() {
        return consensusReached;
    }

    public void setConsensusReached(boolean consensusReached) {
        this.consensusReached = consensusReached;
    }

    public List<Vote> getVotes() {
        return Collections.unmodifiableList(votes);
    }

    public void setVotes(List<Vote> votes) {
        this.votes = votes != null ? new ArrayList<>(votes) : new ArrayList<>();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ── Object contract ──────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Story that = (Story) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Story{id=" + id + ", roomId=" + roomId + ", title='" + title + "', status=" + status + "}";
    }
}
