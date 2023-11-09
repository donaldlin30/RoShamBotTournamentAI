import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class StrategicBot implements RoShamBot {

    private Map<Action, Integer> opponentMoveCount;
    private Random random;
    private int totalMoves;
    private final double[] nashEquilibrium = { 0.2, 0.2, 0.2, 0.2, 0.2 }; // Equal probability for each move
    private final int patternThreshold = 3; // Threshold to consider a behavior a pattern

    /** Constructor initializes the move counters and random number generator. */
    public StrategicBot() {
        this.opponentMoveCount = new HashMap<>();
        for (Action action : Action.values()) {
            this.opponentMoveCount.put(action, 0);
        }
        this.random = new Random();
        this.totalMoves = 0;
    }

    /**
     * Returns the next action that this bot will take.
     * 
     * @param lastOpponentMove the action that was played by the opponent on the
     *                         last round.
     * @return the next action to play.
     */
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

    /**
     * Detects if there is a pattern in the opponent's moves.
     * 
     * @return the action that the opponent is most likely to take next if a pattern
     *         is detected, otherwise null.
     */
    private Action detectPattern() {
        for (Map.Entry<Action, Integer> entry : opponentMoveCount.entrySet()) {
            if (entry.getValue() > (totalMoves / nashEquilibrium.length) + patternThreshold) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Returns a move that counters the opponent's most likely next move.
     * 
     * @param action the action that the opponent is most likely to take next.
     * @return the action to play that counters the opponent's most likely next
     *         move.
     */
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

    /**
     * Returns a move based on the Nash equilibrium strategy.
     * 
     * @return the action to play based on Nash equilibrium.
     */
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
