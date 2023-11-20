import java.util.*;

public class QLBot implements RoShamBot {
    private final double learningRate;
    private final double discountFactor;
    private double explorationRate;
    private final double explorationDecayRate;
    private final Map<List<Action>, double[]> qTable;
    private final Random random;
    private Action lastMove;
    
    public QLBot() {
        this.learningRate = 0.9;
        this.discountFactor = 0.95;
        this.explorationRate = 1.0;
        this.explorationDecayRate = 0.995;
        this.qTable = new HashMap<>();
        this.random = new Random();
        this.lastMove = Action.ROCK; 
    }

    @Override
    public Action getNextMove(Action lastOpponentMove) {
        // Define the current state as the last move of the bot and the last move of the opponent
        List<Action> state = Arrays.asList(lastMove, lastOpponentMove);

        // Select an action based on the Q-table or exploration
        Action action = selectAction(state);

        // Assume this is a training scenario where we simulate the opponent's move
        // In a real game, the opponent's move would be determined by the game
        Action nextOpponentMove = Action.values()[random.nextInt(Action.values().length)];

        // Update the Q-table based on the reward received from the move
        updateQTable(state, action, getReward(action, nextOpponentMove), Arrays.asList(action, nextOpponentMove));

        // Decay the exploration rate
        explorationRate *= explorationDecayRate;

        // Update the last move
        lastMove = action;

        return action;
    }

    private Action selectAction(List<Action> state) {
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
        // Initialize Q-values for the next state if it's new
        qTable.putIfAbsent(nextState, new double[Action.values().length]);

        // Get current Q-value
        double[] currentQValues = qTable.get(state);
        double[] nextQValues = qTable.get(nextState);

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
