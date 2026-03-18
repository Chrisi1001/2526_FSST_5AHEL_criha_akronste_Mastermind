package org.example.mastermind;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MastermindModel {

    private final char[] allowedColors = {'R', 'G', 'B', 'Y', 'O', 'P'};
    private final char[] secretCode = new char[4];
    private final List<String> guesses = new ArrayList<>();
    private final List<String> evaluations = new ArrayList<>();

    private int remainingAttempts = 10;
    private boolean won = false;

    public MastermindModel() {
        generateSecretCode();
    }

    private void generateSecretCode() {
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            secretCode[i] = allowedColors[random.nextInt(allowedColors.length)];
        }
    }

    public boolean isValidGuess(String input) {
        if (input == null) return false;

        input = input.toUpperCase().replaceAll("\\s+", "");

        if (input.length() != 4) return false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            boolean valid = false;
            for (char allowed : allowedColors) {
                if (c == allowed) {
                    valid = true;
                    break;
                }
            }
            if (!valid) return false;
        }

        return true;
    }

    public String checkGuess(String input) {
        input = input.toUpperCase().replaceAll("\\s+", "");

        int exact = 0;
        int wrongPlace = 0;

        boolean[] secretUsed = new boolean[4];
        boolean[] guessUsed = new boolean[4];

        for (int i = 0; i < 4; i++) {
            if (input.charAt(i) == secretCode[i]) {
                exact++;
                secretUsed[i] = true;
                guessUsed[i] = true;
            }
        }

        for (int i = 0; i < 4; i++) {
            if (guessUsed[i]) continue;

            for (int j = 0; j < 4; j++) {
                if (!secretUsed[j] && input.charAt(i) == secretCode[j]) {
                    wrongPlace++;
                    secretUsed[j] = true;
                    guessUsed[i] = true;
                    break;
                }
            }
        }

        remainingAttempts--;

        String result = "●".repeat(exact) + "○".repeat(wrongPlace);

        guesses.add(input);
        evaluations.add(result);

        if (exact == 4) {
            won = true;
        }

        return result;
    }

    public List<String> getGuesses() {
        return guesses;
    }

    public List<String> getEvaluations() {
        return evaluations;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public boolean isWon() {
        return won;
    }

    public boolean isGameOver() {
        return won || remainingAttempts <= 0;
    }

    public String getSecretCode() {
        return new String(secretCode);
    }
}