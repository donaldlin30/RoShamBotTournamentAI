import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class StrategicBot implements RoShamBot {

    private Map<Action, Integer> opponentMoveCount;
    private Random random;
    private int totalMoves;
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

    public Action getNextMove(Action lastOpponentMove) {
        totalMoves++;
        opponentMoveCount.put(lastOpponentMove, opponentMoveCount.getOrDefault(lastOpponentMove, 0) + 1);

        // Check for patterns in opponent's moves
        Action likelyAction = detectPattern();
        if (likelyAction != null) {
            // Exploit the detected pattern
            return counterMove(likelyAction);
        } else {
            // No pattern detected, use Nash equilibrium
            return nashEquilibriumMove();
        }
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
                return Action.ROCK; // Default case, should not be reached
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
