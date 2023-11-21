import java.lang.Math;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;

public class CocainePowder implements RoShamBot {

    // Instance variables to keep track of
    public int consecutiveLosses;
    public List<Action> botMoveHistory;
    public List<Action> opponentMoveHistory;
    private Action lastOpponentMove = null;
    public String usedStrategy = null;
    public Map<String, Map<String, Action>> recommendedMoves;

    private Map<Action, Map<Action, Integer>> markovChain;
    private Map<List<Action>, Map<Action, Integer>> advancedMarkovChain;
    public Map<String, Map<String, Integer>> scoreTable; // TODO: Change to private later

    // Class variables
    private static final List<String> strategies = Arrays.asList("random", "repeat", "ape", "rotation",
            "reverseRotation", "frequency", "history", "pairHistory", "markov", "advancedMarkov"); // match with
                                                                                                   // implemented
                                                                                                   // strategy methods
    private static final List<String> metastrategies = Arrays.asList("m0", "m1", "m2", "m3", "m4");

    private static final Map<Action, List<Action>> beats = new HashMap<>();
    private static final Map<Action, List<Action>> beatenBy = new HashMap<>();

    static {
        beats.put(Action.ROCK, Arrays.asList(Action.SCISSORS, Action.LIZARD));
        beats.put(Action.PAPER, Arrays.asList(Action.ROCK, Action.SPOCK));
        beats.put(Action.SCISSORS, Arrays.asList(Action.PAPER, Action.LIZARD));
        beats.put(Action.LIZARD, Arrays.asList(Action.SPOCK, Action.PAPER));
        beats.put(Action.SPOCK, Arrays.asList(Action.SCISSORS, Action.ROCK));

        beatenBy.put(Action.ROCK, Arrays.asList(Action.PAPER, Action.SPOCK));
        beatenBy.put(Action.PAPER, Arrays.asList(Action.SCISSORS, Action.LIZARD));
        beatenBy.put(Action.SCISSORS, Arrays.asList(Action.ROCK, Action.SPOCK));
        beatenBy.put(Action.LIZARD, Arrays.asList(Action.ROCK, Action.SCISSORS));
        beatenBy.put(Action.SPOCK, Arrays.asList(Action.PAPER, Action.LIZARD));
    }

    public CocainePowder() {
        consecutiveLosses = 0;

        botMoveHistory = new ArrayList<>();
        opponentMoveHistory = new ArrayList<>();

        recommendedMoves = new HashMap<>();

        initializeMarkovChain();
        initializeAdvancedMarkovChain();
        initializeScoreTable();
    }

    // General bot methods begin from here
    private int getRoundsPlayed() {
        return (botMoveHistory != null) ? botMoveHistory.size() : 0;
    }

    private void rememberRecommendedMove(String strategy, String metastrategy, Action recommendedMove) {
        recommendedMoves.computeIfAbsent(strategy, k -> new HashMap<>()).put(metastrategy, recommendedMove);
    }

    public Action getRecommendedMove(String strategy, String metastrategy) {
        return recommendedMoves.getOrDefault(strategy, Collections.emptyMap()).get(metastrategy);
    }

    private void updateBotHistory(Action move) {
        botMoveHistory.add(move);
    }

    private void updateOpponentHistory(Action move) {
        opponentMoveHistory.add(move);
    }

    private Action getPrediction(String strategy) {
        Action oppMove;
        switch (strategy) {
            case "random":
                oppMove = randomAction();
                break;
            case "repeat":
                oppMove = repeat();
                break;
            case "ape":
                oppMove = apePattern();
                break;
            case "rotation":
                oppMove = rotationPattern();
                break;
            case "reverseRotation":
                oppMove = reverseRotationPattern();
                break;
            case "pi":
                oppMove = pi();
                break;
            case "e":
                oppMove = e();
                break;
            case "frequency":
                oppMove = frequencyCounter();
                break;
            case "history":
                oppMove = historyMatching();
                break;
            case "pairHistory":
                oppMove = pairHistory();
                break;
            case "markov":
                oppMove = markovChain();
                break;
            case "advancedMarkov":
                oppMove = advancedMarkovChain();
                break;
            case "iocaine":
                oppMove = iocainePowder();
                break;
            default:
                // Handle other metastrategies if needed
                oppMove = randomAction();
        }

        return oppMove;
    }

    private Action getMeta(String metastrategy, Action oppMove) {
        Action recommendedMove;

        switch (metastrategy) {
            case "m0":
                recommendedMove = m0(oppMove);
                break;
            case "m1":
                recommendedMove = m1(oppMove);
                break;
            case "m2":
                recommendedMove = m2(oppMove);
                break;
            case "m3":
                recommendedMove = m3(oppMove);
                break;
            case "m4":
                recommendedMove = m4(oppMove);
                break;
            default:
                recommendedMove = oppMove;
                break;
        }
        return recommendedMove;
    }

    private void initializeMarkovChain() {
        markovChain = new HashMap<>();

        for (Action currentMove : Action.values()) {
            markovChain.put(currentMove, new HashMap<>());
            for (Action nextMove : Action.values()) {
                markovChain.get(currentMove).put(nextMove, 0);
            }
        }
    }

    private void initializeAdvancedMarkovChain() {
        advancedMarkovChain = new HashMap<>();

        for (Action opponentMove : Action.values()) {
            for (Action botMove : Action.values()) {
                List<Action> state = Arrays.asList(opponentMove, botMove);
                advancedMarkovChain.put(state, new HashMap<>());
                for (Action nextMove : Action.values()) {
                    advancedMarkovChain.get(state).put(nextMove, 0);
                }
            }
        }
    }

    private void initializeScoreTable() {
        scoreTable = new HashMap<>();
        for (String strategy : strategies) {
            Map<String, Integer> scores = new HashMap<>();
            for (String metastrategy : metastrategies) {
                scores.put(metastrategy, 0);
            }
            scoreTable.put(strategy, scores);
        }
    }

    private void addScore(String strategy, String metastrategy, int points) {
        Map<String, Integer> scores = scoreTable.get(strategy);
        if (scores != null) {
            int currentScore = scores.getOrDefault(metastrategy, 0);
            scores.put(metastrategy, currentScore + points);
        }
    }

    private void updateScoreTable() { // TODO: change scoring system to introduce locality
        for (String strategy : strategies) {
            for (String metastrategy : metastrategies) {
                Action botMove = getRecommendedMove(strategy, metastrategy);

                if (beats.get(botMove).contains(lastOpponentMove)) {
                    // Bot wins against opponent's last move
                    addScore(strategy, metastrategy, 2);
                } else if (beatenBy.get(botMove).contains(lastOpponentMove)) {
                    // Bot loses against opponent's last move
                    addScore(strategy, metastrategy, -2);
                } else {
                    // It's a draw
                    addScore(strategy, metastrategy, -1);
                }
            }
        }
    }

    private int getScore(String strategy, String metastrategy) {
        Map<String, Integer> scores = scoreTable.get(strategy);
        if (scores != null) {
            return scores.getOrDefault(metastrategy, 0);
        }
        return 0;
    }

    private void checkForReset() {
        int resetThreshold = 3; // Adjust me

        if (getRoundsPlayed() < resetThreshold) {
            // Not enough rounds played yet, no need to check for reset
            return;
        }
        if (consecutiveLosses >= resetThreshold) {
            // Reset relevant data structures (What about markov chains?)
            initializeScoreTable();
            consecutiveLosses = 0;
        } else {
            Action lastBotMove = botMoveHistory.get(getRoundsPlayed() - 1);

            if (beatenBy.get(lastBotMove).contains(lastOpponentMove)) {
                // Loss
                consecutiveLosses++; // Increment consecutive losses
            } else {
                // Draw
                consecutiveLosses = 0;
            }
        }
    }

    // Metastrategy methods begin from here
    private Action findCommonMove(List<Action> list1, List<Action> list2) {
        for (Action commonAction : list1) {
            if (list2.contains(commonAction)) {
                return commonAction;
            }
        }
        return null; // No common move found
    }

    private Action determineInfrequentMove(Action move1, Action move2) {
        int countMove1 = Collections.frequency(botMoveHistory, move1);
        int countMove2 = Collections.frequency(botMoveHistory, move2);

        if (countMove1 < countMove2) {
            return move1;
        } else {
            return move2;
        }
    }

    private Action m0(Action move) {
        List<Action> moveChoices = beatenBy.get(move);

        Action option1 = moveChoices.get(0);
        Action option2 = moveChoices.get(1);

        Action betterMove = determineInfrequentMove(option1, option2);

        return betterMove;
    }

    private Action m1(Action move) {
        // Beat m0's counter-strategy
        Action strategy = m0(move);
        Action oppResponse1 = beatenBy.get(strategy).get(0);
        Action oppResponse2 = beatenBy.get(strategy).get(1);

        List<Action> beatOppResponse1 = beatenBy.get(oppResponse1);
        List<Action> beatOppResponse2 = beatenBy.get(oppResponse2);

        Action commonMove = findCommonMove(beatOppResponse1, beatOppResponse2);

        // If a common move is found, return it; otherwise, return the original move
        return (commonMove != null) ? commonMove : move;
    }

    private Action m2(Action move) {
        Action strategy = m1(move);
        Action oppResponse1 = beatenBy.get(strategy).get(0);
        Action oppResponse2 = beatenBy.get(strategy).get(1);

        List<Action> beatOppResponse1 = beatenBy.get(oppResponse1);
        List<Action> beatOppResponse2 = beatenBy.get(oppResponse2);

        Action commonMove = findCommonMove(beatOppResponse1, beatOppResponse2);

        return (commonMove != null) ? commonMove : move;
    }

    private Action m3(Action move) {
        Action strategy = m2(move);
        Action oppResponse1 = beatenBy.get(strategy).get(0);
        Action oppResponse2 = beatenBy.get(strategy).get(1);

        List<Action> beatOppResponse1 = beatenBy.get(oppResponse1);
        List<Action> beatOppResponse2 = beatenBy.get(oppResponse2);

        Action commonMove = findCommonMove(beatOppResponse1, beatOppResponse2);

        return (commonMove != null) ? commonMove : move;
    }

    private Action m4(Action move) {
        Action strategy = m3(move);
        Action oppResponse1 = beatenBy.get(strategy).get(0);
        Action oppResponse2 = beatenBy.get(strategy).get(1);

        List<Action> beatOppResponse1 = beatenBy.get(oppResponse1);
        List<Action> beatOppResponse2 = beatenBy.get(oppResponse2);

        Action commonMove = findCommonMove(beatOppResponse1, beatOppResponse2);

        return (commonMove != null) ? commonMove : move;
    }

    // Strategy methods begin from here
    private Action randomAction() {
        Random random = new Random();
        int index = random.nextInt(Action.values().length);
        return Action.values()[index];
    }

    private Action repeat() {
        return lastOpponentMove;
    }

    private Action apePattern() {
        if (!botMoveHistory.isEmpty()) {
            Action nextMove = botMoveHistory.get(botMoveHistory.size() - 1);
            return nextMove;
        } else {
            // Handle the case when move history is empty
            return randomAction();
        }
    }

    private Action rotationPattern() {
        Action[] allActions = Action.values();
        int currentIndex = Arrays.asList(allActions).indexOf(this.lastOpponentMove);
        int nextIndex = (currentIndex + 1) % allActions.length;
        Action nextMove = allActions[nextIndex];

        return nextMove;
    }

    private Action reverseRotationPattern() {
        Action[] allActions = Action.values();
        int currentIndex = Arrays.asList(allActions).indexOf(this.lastOpponentMove);

        // Ensure the index is non-negative
        int nextIndex = (currentIndex - 1 + allActions.length) % allActions.length;

        Action nextMove = allActions[nextIndex];
        return nextMove;
    }

    private Action pi() {
        BigDecimal pi = new BigDecimal(Math.PI).setScale(this.getRoundsPlayed() + 1, RoundingMode.DOWN);
        int piDigit = pi.remainder(BigDecimal.ONE).movePointRight(pi.scale()).intValue();

        // Use the modulus of the pi digit to choose from actions
        Action[] allActions = Action.values();
        int actionIndex = piDigit % allActions.length;

        return allActions[actionIndex];
    }

    public Action e() {
        // Calculate the digit of 'e' corresponding to the current move number
        BigDecimal e = new BigDecimal(Math.E, MathContext.DECIMAL128).setScale(this.getRoundsPlayed() + 1,
                RoundingMode.DOWN);
        int eDigit = e.remainder(BigDecimal.ONE).movePointRight(e.scale()).intValue();

        // Use the modulus of the 'e' digit to choose from actions
        Action[] allActions = Action.values();
        int actionIndex = eDigit % allActions.length;

        return allActions[actionIndex];
    }

    private Action frequencyCounter() {
        // Check if there are previous moves in the opponent's history
        if (!opponentMoveHistory.isEmpty()) {
            Map<Action, Integer> moveCounts = new HashMap<>();

            // Count the frequency of each move in the opponent's history
            for (Action opponentMove : opponentMoveHistory) {
                moveCounts.put(opponentMove, moveCounts.getOrDefault(opponentMove, 0) + 1);
            }

            // Find the moves with the highest frequency
            List<Action> mostFrequentMoves = new ArrayList<>();
            int maxCount = 0;
            for (Map.Entry<Action, Integer> entry : moveCounts.entrySet()) {
                int count = entry.getValue();
                if (count > maxCount) {
                    mostFrequentMoves.clear();
                    mostFrequentMoves.add(entry.getKey());
                    maxCount = count;
                } else if (count == maxCount) {
                    mostFrequentMoves.add(entry.getKey());
                }
            }

            // Break ties randomly
            if (!mostFrequentMoves.isEmpty()) {
                Random random = new Random();
                return mostFrequentMoves.get(random.nextInt(mostFrequentMoves.size()));
            }
        }
        // If there's no history, return a random move
        return randomAction();
    }

    private Action historyMatching() {
        int[] sequenceLengths = { 10, 9, 8, 7, 6, 5 };
        Map<List<Action>, Map<Action, Integer>> sequenceCounts = new HashMap<>();

        int windowSize = 100; // Change me
        // Determine the starting index based on the opponent's move history size
        int startIndex = Math.max(0, opponentMoveHistory.size() - windowSize);

        for (int length : sequenceLengths) {
            if (opponentMoveHistory.size() >= length) {
                List<Action> lastSequence = opponentMoveHistory.subList(opponentMoveHistory.size() - length,
                        opponentMoveHistory.size());

                // Iterate over all possible sequences of the specified length in the opponent's
                // move history
                for (int i = startIndex; i < opponentMoveHistory.size() - length; i++) {
                    List<Action> sequence = opponentMoveHistory.subList(i, i + length);

                    if (sequence.equals(lastSequence)) {
                        Action nextMove = opponentMoveHistory.get(i + length);

                        // Update the count for the next move following the current sequence
                        sequenceCounts.computeIfAbsent(sequence, k -> new HashMap<>());
                        Map<Action, Integer> nextMoveCounts = sequenceCounts.get(sequence);
                        nextMoveCounts.put(nextMove, nextMoveCounts.getOrDefault(nextMove, 0) + 1);
                    }
                }
            }
        }

        // Find the most likely next move for each sequence
        Map<List<Action>, Action> mostLikelyNextMove = new HashMap<>();
        for (Map.Entry<List<Action>, Map<Action, Integer>> entry : sequenceCounts.entrySet()) {
            List<Action> sequence = entry.getKey();
            Map<Action, Integer> counts = entry.getValue();

            // Find the most common next move for this specific sequence
            Action mostCommonNextMove = counts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            // Store the most common next move for this sequence
            mostLikelyNextMove.put(sequence, mostCommonNextMove);
        }

        // Find the most common next move in general over all sequences
        Map<Action, Integer> overallNextMoveCounts = new HashMap<>();
        for (Action nextMove : mostLikelyNextMove.values()) {
            overallNextMoveCounts.put(nextMove, overallNextMoveCounts.getOrDefault(nextMove, 0) + 1);
        }

        // Weight the counts based on the length of each sequence
        Map<Action, Double> weightedNextMoveCounts = new HashMap<>();
        for (Map.Entry<List<Action>, Action> entry : mostLikelyNextMove.entrySet()) {
            Action nextMove = entry.getValue();
            int count = overallNextMoveCounts.get(nextMove);

            // Weight the count based on the length of the action list
            double weightedCount = count * entry.getKey().size() / (double) mostLikelyNextMove.size();

            weightedNextMoveCounts.put(nextMove, weightedCount);
        }

        // Find the most common next move based on weighted counts
        Action mostCommonNextMove = weightedNextMoveCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // Return the most common next move
        return (mostCommonNextMove != null) ? mostCommonNextMove : randomAction();
    }

    private Action pairHistory() {
        // Hardcoded list of sequence lengths to check against
        List<Integer> sequenceLengths = List.of(10, 7, 5, 3); // Add sequence lengths as needed

        int windowSize = 100; // Change me
        // Determine the starting index based on the opponent's move history size
        int startIndex = Math.max(0, opponentMoveHistory.size() - windowSize);

        Map<Action, Integer> nextMoveCounts = new HashMap<>();
        int maxCount = 0;
        Action mostCommonNextMove = null;
        boolean tieDetected = false;

        for (Integer sequenceLength : sequenceLengths) {
            if (getRoundsPlayed() >= sequenceLength) {
                List<Action> lastOpponentSequence = opponentMoveHistory
                        .subList(opponentMoveHistory.size() - sequenceLength, opponentMoveHistory.size());
                List<Action> lastBotSequence = botMoveHistory.subList(botMoveHistory.size() - sequenceLength,
                        botMoveHistory.size());

                for (int i = startIndex; i < getRoundsPlayed() - sequenceLength; i++) {
                    List<Action> opponentSequence = opponentMoveHistory.subList(i, i + sequenceLength);
                    List<Action> mySequence = botMoveHistory.subList(i, i + sequenceLength);

                    // if a match between both sequences is found
                    if (opponentSequence.equals(lastOpponentSequence) && mySequence.equals(lastBotSequence)) {
                        int nextMoveIndex = i + sequenceLength;

                        Action nextMove = opponentMoveHistory.get(nextMoveIndex);
                        nextMoveCounts.put(nextMove, nextMoveCounts.getOrDefault(nextMove, 0) + 1);

                        // Update the most common next move if this one is more common
                        int count = nextMoveCounts.get(nextMove);
                        if (count > maxCount) {
                            maxCount = count;
                            mostCommonNextMove = nextMove;
                        }
                    }
                }
            }

            // if a match is already found, this executes
            if (mostCommonNextMove != null) {
                // Check for ties after the loop
                for (Action move : nextMoveCounts.keySet()) {
                    if (nextMoveCounts.get(move) == maxCount && !move.equals(mostCommonNextMove)) {
                        tieDetected = true;
                    }
                }
                if (!tieDetected) {
                    return mostCommonNextMove;
                } else {
                    tieDetected = false;
                }
            }
        }
        if (mostCommonNextMove != null) {
            return mostCommonNextMove;
        }
        // No matches found
        return randomAction();
    }

    private Action markovChain() {
        int historyWindow = 100; // Adjust the history window to track only the last 100 moves
        double decayFactor = 0.9; // Introduce a decay factor (adjust as needed)

        if (getRoundsPlayed() >= 2) {
            int startIndex = Math.max(0, opponentMoveHistory.size() - historyWindow);

            for (int i = startIndex; i < opponentMoveHistory.size() - 1; i++) {
                Action currentMove = opponentMoveHistory.get(i);
                Action nextMove = opponentMoveHistory.get(i + 1);

                // Update the count with a decay factor
                int count = markovChain.get(currentMove).get(nextMove);
                count = (int) (decayFactor * count);
                markovChain.get(currentMove).put(nextMove, count);

                // Optionally, introduce rewards for winning or losing states
                if (beats.get(currentMove).contains(botMoveHistory.get(i))) {
                    markovChain.get(currentMove).put(nextMove, count + 1);
                } else if (beatenBy.get(currentMove).contains(botMoveHistory.get(i))) {
                    markovChain.get(currentMove).put(nextMove, count - 1);
                }
            }

            Action lastMove = opponentMoveHistory.get(opponentMoveHistory.size() - 1);
            Map<Action, Integer> transitions = markovChain.get(lastMove);

            if (!transitions.isEmpty()) {
                // Choose the next move based on the most likely transition
                Action predictedMove = Collections.max(transitions.entrySet(), Map.Entry.comparingByValue()).getKey();
                return predictedMove;
            }
        }
        // Default to a random move if no history or transitions are available
        return randomAction();
    }

    private Action advancedMarkovChain() {
        int historyWindow = 100; // Adjust the history window to track only the last 100 moves
        double decayFactor = 0.9; // Introduce a decay factor (adjust as needed)

        if (getRoundsPlayed() >= 2) {
            int startIndex = Math.max(0, opponentMoveHistory.size() - historyWindow);

            for (int i = startIndex; i < opponentMoveHistory.size() - 1 && i < botMoveHistory.size() - 1; i++) {
                Action currentOpponentMove = opponentMoveHistory.get(i);
                Action currentBotMove = botMoveHistory.get(i);
                Action nextOpponentMove = opponentMoveHistory.get(i + 1);

                List<Action> currentState = Arrays.asList(currentOpponentMove, currentBotMove);

                // Update the count with a decay factor
                int count = advancedMarkovChain.get(currentState).get(nextOpponentMove);
                count = (int) (decayFactor * count);
                advancedMarkovChain.get(currentState).put(nextOpponentMove, count);

                // Optionally, introduce rewards for winning or losing states
                if (beats.get(currentOpponentMove).contains(currentBotMove)) {
                    advancedMarkovChain.get(currentState).put(nextOpponentMove, count + 1);
                } else if (beatenBy.get(currentOpponentMove).contains(currentBotMove)) {
                    advancedMarkovChain.get(currentState).put(nextOpponentMove, count - 1);
                }
            }

            Action lastBotMove = botMoveHistory.get(botMoveHistory.size() - 1);
            List<Action> lastState = Arrays.asList(lastOpponentMove, lastBotMove);

            Map<Action, Integer> opponentTransitions = advancedMarkovChain.get(lastState);
            if (!opponentTransitions.isEmpty()) {
                // Choose the next opponent move based on the most likely transition
                Action predictedOpponentMove = Collections
                        .max(opponentTransitions.entrySet(), Map.Entry.comparingByValue()).getKey();
                return predictedOpponentMove;
            }
        }
        // Default to a random move if no history or transitions are available
        return randomAction();
    }

    private Action iocainePowder() {
        return randomAction();
    }

    // main function methods begin from here
    private Action findBestMove() {
        // Begin by playing random moves
        if (getRoundsPlayed() < 5) {
            return randomAction();
        }

        String bestStrategy = null;
        String bestMeta = null;
        int maxScore = Integer.MIN_VALUE;

        for (String strategy : strategies) {
            for (String metastrategy : metastrategies) {
                int score = getScore(strategy, metastrategy);

                if (score > maxScore) {
                    // Update if a higher score is found
                    maxScore = score;
                    bestStrategy = strategy;
                    bestMeta = metastrategy;
                } else if (score == maxScore) {
                    // Break ties pseudo-randomly
                    Random random = new Random();
                    if (random.nextBoolean()) {
                        bestStrategy = strategy;
                        bestMeta = metastrategy;
                    }
                }
            }
        }
        // Choose the move based on the best strategy and metastrategy
        if (bestStrategy != null && bestMeta != null) {
            usedStrategy = bestStrategy;
            return getRecommendedMove(bestStrategy, bestMeta);
        } else {
            // If no strategy or metastrategy is found, return a random move
            return randomAction();
        }
    }

    @Override
    public Action getNextMove(Action lastOpponentMove) {

        this.lastOpponentMove = lastOpponentMove;
        if (getRoundsPlayed() != 0) {
            updateOpponentHistory(lastOpponentMove);
            updateScoreTable();
        }

        for (String strategy : strategies) {
            Action oppMove = getPrediction(strategy);
            for (String metastrategy : metastrategies) {
                // Store the moves recommended by each strategy-meta pair this round
                Action recommendedMove = getMeta(metastrategy, oppMove);
                rememberRecommendedMove(strategy, metastrategy, recommendedMove);
            }
        }
        checkForReset();

        Action nextMove = findBestMove();
        updateBotHistory(nextMove);

        return nextMove;
    }

}