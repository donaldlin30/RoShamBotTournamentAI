import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
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
    private int gamesWon = 0;
    private int totalGames = 0;
    private Deque<Action> lastFiveBotMoves;
    private Deque<Action> lastFiveOpponentMoves;
    private List<Action> prevState;

    public AdvancedQLStrategicBot() {
        this.learningRate = 0.9;
        this.discountFactor = 0.4;
        this.explorationRate = 1.3;
        this.explorationDecayRate = 0.995;
        this.qTable = new HashMap<>();
        this.random = new Random();
        this.lastMove = Action.ROCK; // Start with any move
        this.opponentMoveCount = new HashMap<>();
        for (Action action : Action.values()) {
            this.opponentMoveCount.put(action, 0);
        }
        this.totalMoves = 0;
        this.lastFiveBotMoves = new LinkedList<>(Collections.nCopies(3, Action.ROCK));
        this.lastFiveOpponentMoves = new LinkedList<>(Collections.nCopies(3, Action.ROCK));
        this.prevState = null;
    }

    public Action getNextMove(Action lastOpponentMove) {
        updateLastFiveMoves(lastMove, lastOpponentMove);

        List<Action> currentState = new ArrayList<>(lastFiveBotMoves);
        currentState.addAll(lastFiveOpponentMoves);
        if (prevState != null) {
            // Update the Q-table based on the previous state, action, and the reward received from the move
            int reward = getReward(lastMove, lastOpponentMove);
            if (reward == 1) {
                gamesWon++;
            }
            updateQTable(prevState, lastMove, reward, currentState);
        }

        totalGames++;
        totalMoves++;
        updateOpponentMoveCount(lastOpponentMove);
        updatePatternTracking(lastOpponentMove);


        // Calculate win rate
        double winRate = (double) gamesWon / totalGames;

        // Check win rate and apply Nash equilibrium directly if conditions are met
        if (totalGames > 1000 && winRate < 0.3) {
            return weightedNashEquilibriumMove();
        }


        // Use Strategic Pattern Detection
        Action strategyAction = getStrategicAction();

        // Use Q-Learning for move selection if no pattern is detected
        Action action = strategyAction != null ? strategyAction : selectActionUsingQLearning(currentState);

        prevState = new ArrayList<>(currentState);
        // Update the last move
        lastMove = action;

        // Decay the exploration rate
        explorationRate *= explorationDecayRate;


        return action;
    }

    private void updateLastFiveMoves(Action botMove, Action opponentMove) {

        lastFiveBotMoves.removeFirst();
        lastFiveOpponentMoves.removeFirst();
        
        lastFiveBotMoves.addLast(botMove);
        lastFiveOpponentMoves.addLast(opponentMove);
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
            // System.out.println("Using Counter Limited Move Set Strategy");
            return counterLimitedMoveSet();
        }
        double randomChance = random.nextDouble();
        if(randomChance < 0.4){
            return weightedNashEquilibriumMove();
        }

        // Introduce a chance to skip Nash equilibrium strategy
        return null;
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
            // Random selection for exploration
            Action randomAction = Action.values()[random.nextInt(Action.values().length)];
            // System.out.println("QL Strategy (Exploration): Chosen Action = " +
            // randomAction);
            return randomAction;
        } else {
            // Choose the action with the highest Q-value for exploitation
            double[] qValues = qTable.get(state);
            int bestActionIndex = 0;
            for (int i = 1; i < qValues.length; i++) {
                if (qValues[i] > qValues[bestActionIndex]) {
                    bestActionIndex = i;
                }
            }
            Action chosenAction = Action.values()[bestActionIndex];
            // System.out.println("QL Strategy (Exploitation): Chosen Action = " +
            // chosenAction);
            return chosenAction;
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