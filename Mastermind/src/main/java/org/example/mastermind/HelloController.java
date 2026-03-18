package org.example.mastermind;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class HelloController {

    @FXML
    private TextField inputField;

    @FXML
    private Label remainingLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private VBox historyBox;

    private MastermindModel model;

    private final Circle[][] guessCircles = new Circle[10][4];
    private final Circle[][] feedbackCircles = new Circle[10][4];
    private int currentRow = 0;

    @FXML
    public void initialize() {
        startNewGame();
    }

    @FXML
    protected void onSubmitButtonClick() {
        if (model.isGameOver()) {
            return;
        }

        String input = inputField.getText();

        if (!model.isValidGuess(input)) {
            setErrorMessage("Ungültige Eingabe! Bitte genau 4 Zeichen aus R, G, B, Y, O, P eingeben.");
            inputField.clear();
            inputField.requestFocus();
            return;
        }

        String guess = input.toUpperCase().replaceAll("\\s+", "");
        String result = model.checkGuess(guess);

        fillBoardRow(currentRow, guess, result);
        currentRow++;

        remainingLabel.setText("Verbleibende Versuche: " + model.getRemainingAttempts());

        if (model.isWon()) {
            setSuccessMessage("Gewonnen! Du hast den geheimen Code erraten.");
            inputField.setDisable(true);
            return;
        }

        if (model.isGameOver()) {
            setErrorMessage("Verloren! Der geheime Code war: " + model.getSecretCode());
            inputField.setDisable(true);
            return;
        }

        inputField.clear();
        inputField.requestFocus();
    }

    @FXML
    protected void onRestartButtonClick() {
        startNewGame();
    }

    private void startNewGame() {
        model = new MastermindModel();
        currentRow = 0;

        buildEmptyBoard();

        inputField.clear();
        inputField.setDisable(false);
        inputField.requestFocus();

        remainingLabel.setText("Verbleibende Versuche: " + model.getRemainingAttempts());
        setInfoMessage("Neues Spiel gestartet! Gib 4 Farben ein: R, G, B, Y, O, P");
    }

    private void buildEmptyBoard() {
        historyBox.getChildren().clear();

        for (int row = 0; row < 10; row++) {
            HBox rowBox = new HBox(18);
            rowBox.setAlignment(Pos.CENTER_LEFT);
            rowBox.setPadding(new Insets(6, 10, 6, 10));
            rowBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 14;");

            Label rowLabel = new Label(String.valueOf(row + 1));
            rowLabel.setMinWidth(30);
            rowLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #334155;");

            HBox guessBox = new HBox(12);
            guessBox.setAlignment(Pos.CENTER_LEFT);

            for (int col = 0; col < 4; col++) {
                Circle circle = createEmptyGuessCircle();
                guessCircles[row][col] = circle;

                StackPane peg = new StackPane(circle);
                peg.setMinSize(44, 44);
                guessBox.getChildren().add(peg);
            }

            GridPane feedbackGrid = new GridPane();
            feedbackGrid.setHgap(6);
            feedbackGrid.setVgap(6);
            feedbackGrid.setAlignment(Pos.CENTER);

            for (int i = 0; i < 4; i++) {
                Circle smallCircle = createEmptyFeedbackCircle();
                feedbackCircles[row][i] = smallCircle;
                feedbackGrid.add(smallCircle, i % 2, i / 2);
            }

            rowBox.getChildren().addAll(rowLabel, guessBox, feedbackGrid);
            historyBox.getChildren().add(rowBox);
        }
    }

    private void fillBoardRow(int row, String guess, String result) {
        for (int i = 0; i < 4; i++) {
            guessCircles[row][i].setFill(colorForGuess(guess.charAt(i)));
            guessCircles[row][i].setStroke(Color.web("#334155"));
        }

        int exact = countSymbol(result, '●');
        int partial = countSymbol(result, '○');

        for (int i = 0; i < 4; i++) {
            if (i < exact) {
                feedbackCircles[row][i].setFill(Color.BLACK);
                feedbackCircles[row][i].setStroke(Color.BLACK);
            } else if (i < exact + partial) {
                feedbackCircles[row][i].setFill(Color.WHITE);
                feedbackCircles[row][i].setStroke(Color.BLACK);
            } else {
                feedbackCircles[row][i].setFill(Color.web("#e2e8f0"));
                feedbackCircles[row][i].setStroke(Color.web("#cbd5e1"));
            }
        }
    }

    private int countSymbol(String text, char symbol) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == symbol) {
                count++;
            }
        }
        return count;
    }

    private Circle createEmptyGuessCircle() {
        Circle circle = new Circle(17);
        circle.setFill(Color.web("#e2e8f0"));
        circle.setStroke(Color.web("#94a3b8"));
        circle.setStrokeWidth(2);
        return circle;
    }

    private Circle createEmptyFeedbackCircle() {
        Circle circle = new Circle(6);
        circle.setFill(Color.web("#e2e8f0"));
        circle.setStroke(Color.web("#cbd5e1"));
        circle.setStrokeWidth(1.2);
        return circle;
    }

    private Color colorForGuess(char c) {
        return switch (c) {
            case 'R' -> Color.web("#ef4444");
            case 'G' -> Color.web("#22c55e");
            case 'B' -> Color.web("#3b82f6");
            case 'Y' -> Color.web("#facc15");
            case 'O' -> Color.web("#f97316");
            case 'P' -> Color.web("#ec4899");
            default -> Color.web("#cbd5e1");
        };
    }

    private void setInfoMessage(String text) {
        messageLabel.setText(text);
        messageLabel.setStyle(
                "-fx-font-size: 17px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #1d4ed8;"
        );
    }

    private void setSuccessMessage(String text) {
        messageLabel.setText(text);
        messageLabel.setStyle(
                "-fx-font-size: 17px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #16a34a;"
        );
    }

    private void setErrorMessage(String text) {
        messageLabel.setText(text);
        messageLabel.setStyle(
                "-fx-font-size: 17px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #dc2626;"
        );
    }

    private void setWarningMessage(String text) {
        messageLabel.setText(text);
        messageLabel.setStyle(
                "-fx-font-size: 17px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #b45309;"
        );
    }
}