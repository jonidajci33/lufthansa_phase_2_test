import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useRoomStore } from '../stores/useRoomStore';
import { fetchDeckTypes, createDeckType } from '../api/deckTypes';
import { PageTransition } from '../components/animations/PageTransition';
import type { DeckType } from '../types/api';

export function CreateRoomPage() {
  const navigate = useNavigate();
  const createRoom = useRoomStore((s) => s.createRoom);

  const [deckTypes, setDeckTypes] = useState<DeckType[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Form state
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [deckTypeId, setDeckTypeId] = useState('');
  const [maxParticipants, setMaxParticipants] = useState(20);
  const [autoReveal, setAutoReveal] = useState(false);
  const [timerEnabled, setTimerEnabled] = useState(false);
  const [timerSeconds, setTimerSeconds] = useState(60);

  // Custom deck creation
  const [showCustomDeck, setShowCustomDeck] = useState(false);
  const [customDeckName, setCustomDeckName] = useState('');
  const [customValues, setCustomValues] = useState<{ label: string; numericValue: string }[]>([
    { label: '', numericValue: '' },
  ]);
  const [isCreatingDeck, setIsCreatingDeck] = useState(false);
  const [deckError, setDeckError] = useState<string | null>(null);

  useEffect(() => {
    fetchDeckTypes().then((types) => {
      setDeckTypes(types);
      if (types.length > 0 && types[0]) {
        setDeckTypeId(types[0].id);
      }
    });
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !deckTypeId) return;

    setIsSubmitting(true);
    setError(null);

    try {
      const room = await createRoom({
        name: name.trim(),
        description: description.trim() || undefined,
        deckTypeId,
        maxParticipants,
        autoReveal,
        timerSeconds: timerEnabled ? timerSeconds : null,
      });
      navigate(`/rooms/${room.id}`);
    } catch {
      setError('Failed to create room. Please try again.');
      setIsSubmitting(false);
    }
  };

  const addCustomValue = () => {
    setCustomValues([...customValues, { label: '', numericValue: '' }]);
  };

  const removeCustomValue = (index: number) => {
    if (customValues.length <= 1) return;
    setCustomValues(customValues.filter((_, i) => i !== index));
  };

  const updateCustomValue = (index: number, field: 'label' | 'numericValue', value: string) => {
    setCustomValues(customValues.map((v, i) => (i === index ? { ...v, [field]: value } : v)));
  };

  const handleCreateCustomDeck = async () => {
    if (!customDeckName.trim()) {
      setDeckError('Deck name is required');
      return;
    }
    const validValues = customValues.filter((v) => v.label.trim());
    if (validValues.length < 2) {
      setDeckError('Add at least 2 values');
      return;
    }

    setIsCreatingDeck(true);
    setDeckError(null);

    try {
      const newDeck = await createDeckType({
        name: customDeckName.trim(),
        values: validValues.map((v) => ({
          label: v.label.trim(),
          numericValue: v.numericValue.trim() ? Number(v.numericValue) : null,
        })),
      });
      setDeckTypes([...deckTypes, newDeck]);
      setDeckTypeId(newDeck.id);
      setShowCustomDeck(false);
      setCustomDeckName('');
      setCustomValues([{ label: '', numericValue: '' }]);
    } catch {
      setDeckError('Failed to create custom deck');
    } finally {
      setIsCreatingDeck(false);
    }
  };

  const selectedDeck = deckTypes.find((d) => d.id === deckTypeId);

  return (
    <PageTransition>
      <div className="max-w-lg mx-auto px-6 py-8">
        <h1 className="text-2xl font-bold text-white mb-2">Create Room</h1>
        <p className="text-sm text-text-secondary mb-8">
          Set up a new estimation session
        </p>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Room name */}
          <div>
            <label className="block text-sm text-text-secondary mb-1.5">
              Room Name *
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Sprint 42 Planning"
              required
              className="w-full px-3 py-2 bg-surface-secondary border border-border rounded-md text-white text-sm placeholder:text-text-muted focus:outline-none focus:border-border-hover"
            />
          </div>

          {/* Description */}
          <div>
            <label className="block text-sm text-text-secondary mb-1.5">
              Description
            </label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Optional description..."
              rows={3}
              className="w-full px-3 py-2 bg-surface-secondary border border-border rounded-md text-white text-sm placeholder:text-text-muted focus:outline-none focus:border-border-hover resize-none"
            />
          </div>

          {/* Deck type */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="text-sm text-text-secondary">Deck Type *</label>
              <button
                type="button"
                onClick={() => setShowCustomDeck(!showCustomDeck)}
                className="text-xs text-text-muted hover:text-white transition-colors"
              >
                {showCustomDeck ? 'Cancel' : '+ Custom Deck'}
              </button>
            </div>

            {!showCustomDeck ? (
              <>
                <select
                  value={deckTypeId}
                  onChange={(e) => setDeckTypeId(e.target.value)}
                  className="w-full px-3 py-2 bg-surface-secondary border border-border rounded-md text-white text-sm focus:outline-none focus:border-border-hover"
                >
                  {deckTypes.map((dt) => (
                    <option key={dt.id} value={dt.id}>
                      {dt.name} {dt.isSystem ? '' : '(custom)'}
                    </option>
                  ))}
                </select>
                {selectedDeck && (
                  <p className="mt-1.5 text-xs text-text-muted font-mono">
                    Values: {selectedDeck.values.map((v) => v.label).join(', ')}
                  </p>
                )}
              </>
            ) : (
              <div className="space-y-3 p-4 rounded-lg border border-border bg-surface-secondary">
                <div>
                  <label className="block text-xs text-text-muted mb-1">Deck Name</label>
                  <input
                    type="text"
                    value={customDeckName}
                    onChange={(e) => setCustomDeckName(e.target.value)}
                    placeholder="My Custom Deck"
                    className="w-full px-3 py-2 bg-surface-tertiary border border-border rounded-md text-white text-sm placeholder:text-text-muted focus:outline-none focus:border-border-hover"
                  />
                </div>

                <div>
                  <label className="block text-xs text-text-muted mb-1">
                    Values (label + optional numeric)
                  </label>
                  <div className="space-y-2">
                    {customValues.map((v, i) => (
                      <div key={i} className="flex gap-2 items-center">
                        <input
                          type="text"
                          value={v.label}
                          onChange={(e) => updateCustomValue(i, 'label', e.target.value)}
                          placeholder="Label (e.g. S, 5, ?)"
                          className="flex-1 px-2 py-1.5 bg-surface-tertiary border border-border rounded text-white text-xs placeholder:text-text-muted focus:outline-none focus:border-border-hover"
                        />
                        <input
                          type="number"
                          value={v.numericValue}
                          onChange={(e) => updateCustomValue(i, 'numericValue', e.target.value)}
                          placeholder="Numeric"
                          className="w-20 px-2 py-1.5 bg-surface-tertiary border border-border rounded text-white text-xs placeholder:text-text-muted focus:outline-none focus:border-border-hover"
                        />
                        <button
                          type="button"
                          onClick={() => removeCustomValue(i)}
                          disabled={customValues.length <= 1}
                          className="text-text-muted hover:text-white text-xs disabled:opacity-30 px-1"
                        >
                          x
                        </button>
                      </div>
                    ))}
                  </div>
                  <button
                    type="button"
                    onClick={addCustomValue}
                    className="mt-2 text-xs text-text-muted hover:text-white transition-colors"
                  >
                    + Add Value
                  </button>
                </div>

                {deckError && (
                  <p className="text-xs text-red-400">{deckError}</p>
                )}

                <button
                  type="button"
                  onClick={handleCreateCustomDeck}
                  disabled={isCreatingDeck}
                  className="w-full py-2 bg-surface-tertiary border border-border hover:border-border-hover text-white rounded-md text-xs font-medium transition-colors disabled:opacity-50"
                >
                  {isCreatingDeck ? 'Creating...' : 'Create Deck'}
                </button>
              </div>
            )}
          </div>

          {/* Max participants */}
          <div>
            <label className="block text-sm text-text-secondary mb-1.5">
              Max Participants
            </label>
            <input
              type="number"
              value={maxParticipants}
              onChange={(e) => setMaxParticipants(Number(e.target.value))}
              min={2}
              max={100}
              className="w-24 px-3 py-2 bg-surface-secondary border border-border rounded-md text-white text-sm focus:outline-none focus:border-border-hover"
            />
          </div>

          {/* Auto reveal */}
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={() => setAutoReveal(!autoReveal)}
              className={`w-10 h-5 rounded-full transition-colors relative ${
                autoReveal ? 'bg-white' : 'bg-surface-tertiary border border-border'
              }`}
            >
              <span
                className={`absolute top-0.5 w-4 h-4 rounded-full transition-all ${
                  autoReveal
                    ? 'left-5 bg-black'
                    : 'left-0.5 bg-text-muted'
                }`}
              />
            </button>
            <label className="text-sm text-text">
              Auto-reveal when all participants voted
            </label>
          </div>

          {/* Timer */}
          <div className="space-y-2">
            <div className="flex items-center gap-3">
              <button
                type="button"
                onClick={() => setTimerEnabled(!timerEnabled)}
                className={`w-10 h-5 rounded-full transition-colors relative ${
                  timerEnabled ? 'bg-white' : 'bg-surface-tertiary border border-border'
                }`}
              >
                <span
                  className={`absolute top-0.5 w-4 h-4 rounded-full transition-all ${
                    timerEnabled
                      ? 'left-5 bg-black'
                      : 'left-0.5 bg-text-muted'
                  }`}
                />
              </button>
              <label className="text-sm text-text">
                Enable voting timer
              </label>
            </div>
            {timerEnabled && (
              <div className="pl-13">
                <input
                  type="number"
                  value={timerSeconds}
                  onChange={(e) => setTimerSeconds(Number(e.target.value))}
                  min={10}
                  max={600}
                  className="w-24 px-3 py-2 bg-surface-secondary border border-border rounded-md text-white text-sm focus:outline-none focus:border-border-hover"
                />
                <span className="text-xs text-text-muted ml-2">seconds</span>
              </div>
            )}
          </div>

          {/* Error */}
          {error && (
            <p className="text-sm text-text-muted">{error}</p>
          )}

          {/* Actions */}
          <div className="flex gap-3 pt-2">
            <button
              type="submit"
              disabled={isSubmitting || !name.trim() || !deckTypeId}
              className="px-6 py-2.5 bg-white text-black rounded-lg font-medium text-sm hover:bg-gray-200 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isSubmitting ? 'Creating...' : 'Create Room'}
            </button>
            <button
              type="button"
              onClick={() => navigate('/rooms')}
              className="px-6 py-2.5 border border-border text-text-secondary rounded-lg text-sm hover:border-border-hover hover:text-white transition-colors"
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </PageTransition>
  );
}
