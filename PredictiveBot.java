import java.util.*;

public class PredictiveBot implements RoShamBot {

    private List<Action> challengerMoves;
    private List<Action> humanMoves;
    private Random random;

    // Constructor
    public PredictiveBot() {
        this.challengerMoves = new ArrayList<>();
        this.humanMoves = new ArrayList<>();
        this.random = new Random();
    }

    @Override
    public Action getNextMove(Action lastOpponentMove) {
        humanMoves.add(lastOpponentMove);
        Action predictedMove = predictNextMove();
        challengerMoves.add(predictedMove);
        return predictedMove;
    }
    private Action predictNextMove() {
        if (humanMoves.isEmpty()) {
            return getRandomAction();
        }
    
        Action prediction = bestNextMove(humanMoves);
        int predictionIndex = prediction.ordinal();
    
        // Initialize scores array
        int[] scores = new int[5];
    
        for (int i = 1; i < humanMoves.size(); i++) {
            int actualIndex = humanMoves.get(i).ordinal();
    
            // Calculate score index using positive modulo to avoid negative indices
            int winIndex = Math.floorMod(actualIndex - predictionIndex + 1, 5);
            int tieIndex = Math.floorMod(actualIndex - predictionIndex + 3, 5);
            int loseIndex1 = Math.floorMod(actualIndex - predictionIndex + 2, 5);
            int loseIndex2 = Math.floorMod(actualIndex - predictionIndex + 4, 5);
    
            // Update the scores based on the game rules
            scores[winIndex]++;
            scores[tieIndex]++;
            scores[loseIndex1]--;
            scores[loseIndex2]--;
        }
    
        // Select the best strategy
        int bestStrategyScore = Integer.MIN_VALUE;
        int bestStrategyIndex = predictionIndex; // Default to the predicted move's index
    
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > bestStrategyScore) {
                bestStrategyScore = scores[i];
                bestStrategyIndex = i;
            }
        }
    
        return Action.values()[bestStrategyIndex];
    }
    

    

    private Action bestNextMove(List<Action> history) {
        // The strategy consists of identifying patterns within the move history.
        // For each length l, we look back at the sequence of the last l moves and find the
        // most recent occurrence of that same sequence in the past, then predict based on
        // what move followed that sequence previously.
    
        int historySize = history.size();
        int maxPatternLength = Math.min(historySize, 20); // Limit the pattern length for efficiency
        Action bestNextMove = getRandomAction(); // Default to random move
    
        // Initialize move frequencies
        Map<Action, Integer> moveFrequencies = new EnumMap<>(Action.class);
        for (Action move : Action.values()) {
            moveFrequencies.put(move, 0);
        }
        for (Action move : history) {
            moveFrequencies.put(move, moveFrequencies.get(move) + 1);
        }
    
        // For each pattern length from 1 to maxPatternLength
        for (int l = 1; l <= maxPatternLength; l++) {
            // Get the last l moves as the current pattern to search for
            List<Action> pattern = history.subList(historySize - l, historySize);
    
            // Search for the pattern in the history, excluding the last l moves
            for (int i = historySize - 2 * l; i >= 0; i--) {
                List<Action> testPattern = history.subList(i, i + l);
                if (pattern.equals(testPattern) && i + l < historySize) {
                    // Found a matching pattern, predict the move that followed it last time
                    bestNextMove = history.get(i + l);
                    break;
                }
            }
        }
    
        // If no pattern is found, use the most frequent move
        if (bestNextMove == null) {
            int maxFrequency = Collections.max(moveFrequencies.values());
            for (Map.Entry<Action, Integer> entry : moveFrequencies.entrySet()) {
                if (entry.getValue() == maxFrequency) {
                    bestNextMove = entry.getKey();
                    break;
                }
            }
        }
    
        return bestNextMove;
    }
    
    

    private Action getRandomAction() {
        Action[] actions = Action.values();
        return actions[random.nextInt(actions.length)];
    }
}
