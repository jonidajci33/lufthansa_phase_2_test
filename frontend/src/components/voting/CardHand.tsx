import { VotingCard } from './VotingCard';
import { useVotingStore } from '../../stores/useVotingStore';

interface CardHandProps {
  deckValues: string[];
  storyId: string;
  disabled?: boolean;
}

/**
 * Grid of voting cards from the selected deck type.
 * Click to select/vote, click a different card to change vote.
 */
export function CardHand({ deckValues, storyId, disabled = false }: CardHandProps) {
  const { selectedCard, hasVoted, submitVote, changeVote } = useVotingStore();

  const handleCardClick = async (value: string) => {
    if (disabled) return;

    if (value === selectedCard) return; // Already selected

    if (hasVoted) {
      await changeVote(storyId, value);
    } else {
      await submitVote(storyId, value);
    }
  };

  return (
    <div className="flex flex-wrap justify-center gap-3 py-4">
      {deckValues.map((value) => (
        <VotingCard
          key={value}
          value={value}
          isSelected={selectedCard === value}
          onClick={() => handleCardClick(value)}
          disabled={disabled}
        />
      ))}
    </div>
  );
}
