import java.util.*;

public class QLBot implements RoShamBot {

    private final double learningRate;
    private final double discountFactor;
    private double explorationRate;
    private final double explorationDecayRate;
    private final Map<List<Action>, double[]> qTable;
    private final Random random;
    private Action lastMove;
    private Deque<Action> lastFiveMoves; // Last five moves of the bot
    private Deque<Action> lastFiveOpponentMoves; // Last five moves of the opponent
    private List<Action> prevState; // Stores the previous state
    private Action prevAction; // Stores the previous action taken

    public QLBot() {
        this.learningRate = 0.9;
        this.discountFactor = 0.95;
        this.explorationRate = 1.0;
        this.explorationDecayRate = 0.995;
        this.qTable = new HashMap<>();
        this.random = new Random();
        this.lastMove = Action.ROCK;
        this.lastFiveMoves = new LinkedList<>(Collections.nCopies(5, Action.ROCK));
        this.lastFiveOpponentMoves = new LinkedList<>(Collections.nCopies(5, Action.ROCK));
        this.prevState = null;
        this.prevAction = null;
    }

    @Override
    public Action getNextMove(Action lastOpponentMove) {
        // Update the last five moves for the bot and the opponent
        updateLastFiveMoves(lastMove, lastOpponentMove);

        // Define the current state as the combination of last five moves from both the bot and the opponent
        List<Action> currentState = new ArrayList<>(lastFiveMoves);
        currentState.addAll(lastFiveOpponentMoves);

        if (prevState != null) {
            // Update the Q-table based on the previous state, action, and the reward received from the move
            int reward = getReward(prevAction, lastOpponentMove);
            updateQTable(prevState, prevAction, reward, currentState);
        }

        // Select an action based on the Q-table or exploration
        Action action = selectAction(currentState);

        // Decay the exploration rate
        explorationRate *= explorationDecayRate;

        // Store the current state and action for the next round
        prevState = new ArrayList<>(currentState);
        prevAction = action;

        // Update the last move
        lastMove = action;

        return action;
    }

    private void updateLastFiveMoves(Action botMove, Action opponentMove) {
        if (lastFiveMoves.size() >= 5) {
            lastFiveMoves.removeFirst();
            lastFiveOpponentMoves.removeFirst();
        }
        lastFiveMoves.addLast(botMove);
        lastFiveOpponentMoves.addLast(opponentMove);
    }

    private Action selectAction(List<Action> state) {
        qTable.putIfAbsent(state, new double[Action.values().length]);

        if (random.nextDouble() < explorationRate) {
            return Action.values()[random.nextInt(Action.values().length)];
        } else {
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
        qTable.putIfAbsent(nextState, new double[Action.values().length]);

        double[] currentQValues = qTable.get(state);
        double[] nextQValues = qTable.get(nextState);

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
