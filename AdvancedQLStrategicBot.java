import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import java.util.Arrays;
import java.util.List;

public class AdvancedQLStrategicBot implements RoShamBot {
    private final double learningRate;
    private final double discountFactor;
    private double explorationRate;
    private final double explorationDecayRate;
    private final Map<List<Action>, double[]> qTable;
    private final Random random;
    private Action lastMove;
    private Map<Action, Integer> opponentMoveCount;
    private final double randomnessFactor = 0.1;
    private Action[] lastThreeMoves = new Action[3];
    private Map<String, Integer> patternMap = new HashMap<>();
    private final double[] nashEquilibrium = { 0.2, 0.2, 0.2, 0.2, 0.2 };
    private final int patternThreshold = 3;
    private boolean isOpponentLimited = false;
    private int totalMoves;

    public AdvancedQLStrategicBot() {
        this.learningRate = 0.9;
        this.discountFactor = 0.95;
        this.explorationRate = 1.0;
        this.explorationDecayRate = 0.995;
        this.qTable = new HashMap<>();
        this.random = new Random();
        this.lastMove = Action.ROCK; // Start with any move
        this.opponentMoveCount = new HashMap<>();
        for (Action action : Action.values()) {
            this.opponentMoveCount.put(action, 0);
        }
        this.totalMoves = 0;
    }

    @Override
    public Action getNextMove(Action lastOpponentMove) {
        totalMoves++;
        updateOpponentMoveCount(lastOpponentMove);
        updatePatternTracking(lastOpponentMove);

        // Define the current state
        List<Action> state = Arrays.asList(lastMove, lastOpponentMove);

        // Use Strategic Pattern Detection
        Action strategyAction = getStrategicAction();

        // Use Q-Learning for move selection if no pattern is detected
        Action action = strategyAction != null ? strategyAction : selectActionUsingQLearning(state);

        // Assume a training scenario for Q-learning update
        Action nextOpponentMove = Action.values()[random.nextInt(Action.values().length)];
        updateQTable(state, action, getReward(action, nextOpponentMove), Arrays.asList(action, nextOpponentMove));

        // Decay the exploration rate
        explorationRate *= explorationDecayRate;

        // Update the last move
        lastMove = action;

        return action;
    }

    private Action counterLimitedMoveSet() {
        Action mostCommon = Action.ROCK; // Default
        int maxCount = -1;
        for (Map.Entry<Action, Integer> entry : opponentMoveCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostCommon = entry.getKey();
            }
        }
        // Return the counter to the most common move
        return counterMove(mostCommon);
    }

    private Action getStrategicAction() {
        // Check if the opponent is predictable
        Action predictableCounter = getMoveIfOpponentIsPredictable();
        if (predictableCounter != null) {
            return predictableCounter;
        }

        // Detect pattern
        Action likelyAction = detectPattern();
        if (likelyAction != null) {
            return counterMove(likelyAction);
        } else if (isOpponentLimited) {
            return counterLimitedMoveSet();
        }

        // Otherwise, use Nash equilibrium move
        return weightedNashEquilibriumMove();
    }

    private void updateOpponentMoveCount(Action lastOpponentMove) {
        opponentMoveCount.put(lastOpponentMove, opponentMoveCount.getOrDefault(lastOpponentMove, 0) + 1);
    }

    private void updatePatternTracking(Action lastOpponentMove) {
        if (totalMoves >= 3) {
            String pattern = "" + lastThreeMoves[0] + lastThreeMoves[1] + lastThreeMoves[2];
            patternMap.put(pattern, patternMap.getOrDefault(pattern, 0) + 1);
        }
        lastThreeMoves[0] = lastThreeMoves[1];
        lastThreeMoves[1] = lastThreeMoves[2];
        lastThreeMoves[2] = lastOpponentMove;
    }

    private Action getMoveIfOpponentIsPredictable() {
        for (Map.Entry<Action, Integer> entry : opponentMoveCount.entrySet()) {
            if (entry.getValue() == totalMoves) {
                return counterMove(entry.getKey());
            }
        }
        return null;
    }

    private Action detectPattern() {
        if (totalMoves < 3) {
            return null;
        }
        String recentPattern = "" + lastThreeMoves[1] + lastThreeMoves[2];
        Action mostLikelyMove = null;
        int maxCount = -1;
        for (Action action : Action.values()) {
            String potentialPattern = recentPattern + action;
            int count = patternMap.getOrDefault(potentialPattern, 0);
            if (count > maxCount) {
                maxCount = count;
                mostLikelyMove = action;
            }
        }
        if (maxCount > patternThreshold) {
            return mostLikelyMove;
        }
        return null;
    }

    private Action counterMove(Action action) {
        switch (action) {
            case ROCK:
                return Action.PAPER;
            case PAPER:
                return Action.SCISSORS;
            case SCISSORS:
                return Action.ROCK;
            case LIZARD:
                return Action.SCISSORS;
            case SPOCK:
                return Action.LIZARD;
            default:
                return Action.ROCK;
        }
    }

    private Action weightedNashEquilibriumMove() {
        double[] weightedChances = new double[Action.values().length];
        int totalOpponentMoves = opponentMoveCount.values().stream().mapToInt(Integer::intValue).sum();
        for (int i = 0; i < weightedChances.length; i++) {
            Action action = Action.values()[i];
            weightedChances[i] = nashEquilibrium[i] +
                    randomnessFactor * (opponentMoveCount.getOrDefault(action, 0) / (double) totalOpponentMoves);
        }
        double sum = 0;
        for (double weight : weightedChances) {
            sum += weight;
        }
        for (int i = 0; i < weightedChances.length; i++) {
            weightedChances[i] /= sum;
        }
        double p = random.nextDouble();
        double cumulativeProbability = 0.0;
        for (int i = 0; i < weightedChances.length; i++) {
            cumulativeProbability += weightedChances[i];
            if (p <= cumulativeProbability) {
                return Action.values()[i];
            }
        }
        return Action.ROCK;
    }

    private Action selectActionUsingQLearning(List<Action> state) {
        // Initialize Q-values for the state if it's new
        qTable.putIfAbsent(state, new double[Action.values().length]);

        // Exploration vs exploitation decision
        if (random.nextDouble() < explorationRate) {
            return Action.values()[random.nextInt(Action.values().length)];
        } else {
            // Choose the action with the highest Q-value
            double[] qValues = qTable.get(state);
            int bestActionIndex = 0;
            for (int i = 1; i < qValues.length; i++) {
                if (qValues[i] > qValues[bestActionIndex]) {
                    bestActionIndex = i;
                }
            }
            return Action.values()[bestActionIndex];
        }
    }

    private void updateQTable(List<Action> state, Action action, int reward, List<Action> nextState) {
        // Initialize Q-values for the current state if it's new
        qTable.putIfAbsent(state, new double[Action.values().length]);
        // Initialize Q-values for the next state if it's new
        qTable.putIfAbsent(nextState, new double[Action.values().length]);

        // Get current Q-values and next Q-values
        double[] currentQValues = qTable.get(state);
        double[] nextQValues = qTable.get(nextState);

        // Ensure arrays are not null
        if (currentQValues == null || nextQValues == null) {
            throw new IllegalStateException("Q-Values array not initialized properly.");
        }

        // Q-learning formula
        int actionIndex = action.ordinal();
        double oldQValue = currentQValues[actionIndex];
        double nextMaxQ = Arrays.stream(nextQValues).max().orElse(Double.NEGATIVE_INFINITY);
        currentQValues[actionIndex] = oldQValue + learningRate * (reward + discountFactor * nextMaxQ - oldQValue);
    }

    private int getReward(Action action, Action opponentAction) {
        // Winning cases for RPSLS
        if ((action == Action.ROCK && (opponentAction == Action.SCISSORS || opponentAction == Action.LIZARD)) ||
                (action == Action.PAPER && (opponentAction == Action.ROCK || opponentAction == Action.SPOCK)) ||
                (action == Action.SCISSORS && (opponentAction == Action.PAPER || opponentAction == Action.LIZARD)) ||
                (action == Action.LIZARD && (opponentAction == Action.SPOCK || opponentAction == Action.PAPER)) ||
                (action == Action.SPOCK && (opponentAction == Action.SCISSORS || opponentAction == Action.ROCK))) {
            return 1; // Win
        } else if (action == opponentAction) {
            return 0; // Tie
        } else {
            return -1; // Lose
        }
    }
}
