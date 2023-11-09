import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class StrategicBot implements RoShamBot {

    private Map<Action, Integer> opponentMoveCount;
    private Random random;
    private int totalMoves;
    private final double randomnessFactor = 0.1; // Increase for more randomness
    private Action[] lastThreeMoves = new Action[3];
    private Map<String, Integer> patternMap = new HashMap<>();
    private final double[] nashEquilibrium = { 0.2, 0.2, 0.2, 0.2, 0.2 }; // Equal probability for each move
    private final int patternThreshold = 3; // Threshold to consider a behavior a pattern

    public StrategicBot() {
        this.opponentMoveCount = new HashMap<>();
        for (Action action : Action.values()) {
            this.opponentMoveCount.put(action, 0);
        }
        this.random = new Random();
        this.totalMoves = 0;
    }

    private void updatePatternTracking(Action lastOpponentMove) {
        if (totalMoves >= 3) {
            String pattern = "" + lastThreeMoves[0] + lastThreeMoves[1] + lastThreeMoves[2];
            patternMap.put(pattern, patternMap.getOrDefault(pattern, 0) + 1);
        }

        // Update the last moves array
        lastThreeMoves[0] = lastThreeMoves[1];
        lastThreeMoves[1] = lastThreeMoves[2];
        lastThreeMoves[2] = lastOpponentMove;
    }

    private Action getMoveIfOpponentIsPredictable() {
        // Check if the opponent has played the same move every time
        for (Map.Entry<Action, Integer> entry : opponentMoveCount.entrySet()) {
            if (entry.getValue() == totalMoves) {
                return counterMove(entry.getKey()); // Always counter the repeated move
            }
        }
        return null; // Opponent is not predictable
    }

    public Action getNextMove(Action lastOpponentMove) {
        totalMoves++;
        opponentMoveCount.put(lastOpponentMove, opponentMoveCount.getOrDefault(lastOpponentMove, 0) + 1);

        Action predictableCounter = getMoveIfOpponentIsPredictable();
        if (predictableCounter != null) {
            return predictableCounter; // Exploit the opponent's predictability
        }

        Action likelyAction = detectPattern();
        if (likelyAction != null) {
            return counterMove(likelyAction); // Exploit detected pattern
        } else {
            return weightedNashEquilibriumMove(); // Use weighted Nash equilibrium
        }
    }

    private Action weightedNashEquilibriumMove() {
        double[] weightedChances = new double[Action.values().length];
        int totalOpponentMoves = opponentMoveCount.values().stream().mapToInt(Integer::intValue).sum();

        // Adjust weights based on opponent move frequency
        for (int i = 0; i < weightedChances.length; i++) {
            Action action = Action.values()[i];
            weightedChances[i] = nashEquilibrium[i] +
                    randomnessFactor * (opponentMoveCount.getOrDefault(action, 0) / (double) totalOpponentMoves);
        }

        // Normalize the weights
        double sum = 0;
        for (double weight : weightedChances) {
            sum += weight;
        }
        for (int i = 0; i < weightedChances.length; i++) {
            weightedChances[i] /= sum;
        }

        // Select a move based on the weighted chances
        double p = random.nextDouble();
        double cumulativeProbability = 0.0;
        for (int i = 0; i < weightedChances.length; i++) {
            cumulativeProbability += weightedChances[i];
            if (p <= cumulativeProbability) {
                return Action.values()[i];
            }
        }

        return Action.ROCK; // Fallback if something goes wrong
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
                return Action.PAPER; // Paper covers rock
            case PAPER:
                return Action.SCISSORS; // Scissors cut paper
            case SCISSORS:
                return Action.ROCK; // Rock crushes scissors
            case LIZARD:
                return Action.SCISSORS; // Scissors decapitate lizard
            case SPOCK:
                return Action.LIZARD; // Lizard poisons Spock
            default:
                return Action.ROCK;
        }
    }

    private Action nashEquilibriumMove() {
        double p = random.nextDouble();
        double cumulativeProbability = 0.0;
        for (int i = 0; i < nashEquilibrium.length; i++) {
            cumulativeProbability += nashEquilibrium[i];
            if (p <= cumulativeProbability) {
                return Action.values()[i];
            }
        }
        return Action.ROCK;
    }
}
