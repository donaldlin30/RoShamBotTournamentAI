import java.util.Random;

/**
 * Randomly choose a move.
 * 
 */
public class SimpleBot implements RoShamBot {

    private Random random;

    /** Constructor initializes the random number generator. */
    public SimpleBot() {
        this.random = new Random();
    }

    /**
     * Returns a random action for each move.
     * 
     * @param lastOpponentMove the action that was played by the opponent on the
     *                         last round (ignored by this bot).
     * @return the next action to play chosen randomly.
     */
    public Action getNextMove(Action lastOpponentMove) {
        // Get a random integer between 0 (inclusive) and the number of actions
        int pick = random.nextInt(Action.values().length);
        return Action.values()[pick];
    }
}
