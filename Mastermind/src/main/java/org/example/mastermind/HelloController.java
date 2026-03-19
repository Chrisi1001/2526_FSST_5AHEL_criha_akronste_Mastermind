package org.example.mastermind;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
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

    private final Circle[][] guessCircles    = new Circle[10][4];
    private final Circle[][] feedbackCircles = new Circle[10][4];
    private int currentRow = 0;

    @FXML
    public void initialize() {
        startNewGame();
    }

    @FXML
    protected void onSubmitButtonClick() {
        if (model.isGameOver()) return;

        String input = inputField.getText();

        if (!model.isValidGuess(input)) {
            setErrorMessage("⚠  Ungültige Eingabe! Genau 4 Zeichen aus R, G, B, Y, O, P eingeben.");
            inputField.clear();
            inputField.requestFocus();
            return;
        }

        String guess  = input.toUpperCase().replaceAll("\\s+", "");
        String result = model.checkGuess(guess);

        fillBoardRow(currentRow, guess, result);
        currentRow++;

        remainingLabel.setText("Verbleibende Versuche: " + model.getRemainingAttempts());

        if (model.isWon()) {
            setSuccessMessage("🎉  Gewonnen! Du hast den geheimen Code erraten!");
            inputField.setDisable(true);
            return;
        }

        if (model.isGameOver()) {
            setErrorMessage("💀  Verloren! Der Code war: " + model.getSecretCode());
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
        model      = new MastermindModel();
        currentRow = 0;

        buildEmptyBoard();

        inputField.clear();
        inputField.setDisable(false);
        inputField.requestFocus();

        remainingLabel.setText("Verbleibende Versuche: " + model.getRemainingAttempts());
        setInfoMessage("🎮  Neues Spiel! Gib 4 Farben ein: R G B Y O P");
    }

    private void buildEmptyBoard() {
        historyBox.getChildren().clear();

        for (int row = 0; row < 10; row++) {
            HBox rowBox = new HBox(16);
            rowBox.setAlignment(Pos.CENTER_LEFT);
            rowBox.setPadding(new Insets(8, 14, 8, 14));

            String bg = (row % 2 == 0) ? "#1e293b" : "#0f172a";
            rowBox.setStyle(
                    "-fx-background-color: " + bg + ";" +
                            "-fx-background-radius: 12;"
            );

            Label rowLabel = new Label(String.format("%2d", row + 1));
            rowLabel.setMinWidth(28);
            rowLabel.setStyle(
                    "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #64748b;" +
                            "-fx-font-family: 'Courier New';"
            );

            HBox guessBox = new HBox(10);
            guessBox.setAlignment(Pos.CENTER_LEFT);

            for (int col = 0; col < 4; col++) {
                Circle circle = createEmptyGuessCircle();
                guessCircles[row][col] = circle;

                StackPane peg = new StackPane(circle);
                peg.setMinSize(42, 42);
                guessBox.getChildren().add(peg);
            }

            GridPane feedbackGrid = new GridPane();
            feedbackGrid.setHgap(5);
            feedbackGrid.setVgap(5);
            feedbackGrid.setAlignment(Pos.CENTER);

            for (int i = 0; i < 4; i++) {
                Circle small = createEmptyFeedbackCircle();
                feedbackCircles[row][i] = small;
                feedbackGrid.add(small, i % 2, i / 2);
            }

            rowBox.getChildren().addAll(rowLabel, guessBox, feedbackGrid);
            historyBox.getChildren().add(rowBox);
        }
    }

    private void fillBoardRow(int row, String guess, String result) {
        for (int i = 0; i < 4; i++) {
            Color base = colorForGuess(guess.charAt(i));
            guessCircles[row][i].setFill(radialGradientFor(base));

            DropShadow glow = new DropShadow();
            glow.setColor(base.deriveColor(0, 1.0, 1.0, 0.6));
            glow.setRadius(10);
            glow.setSpread(0.2);
            guessCircles[row][i].setEffect(glow);
            guessCircles[row][i].setStroke(base.brighter());
            guessCircles[row][i].setStrokeWidth(1.5);
        }

        int exact   = countSymbol(result, '●');
        int partial = countSymbol(result, '○');

        for (int i = 0; i < 4; i++) {
            if (i < exact) {
                feedbackCircles[row][i].setFill(
                        radialGradientFor(Color.web("#f8fafc"))
                );
                feedbackCircles[row][i].setStroke(Color.web("#ffffff"));
                feedbackCircles[row][i].setStrokeWidth(1.2);

                DropShadow ds = new DropShadow();
                ds.setColor(Color.web("#ffffff", 0.5));
                ds.setRadius(5);
                feedbackCircles[row][i].setEffect(ds);

            } else if (i < exact + partial) {
                feedbackCircles[row][i].setFill(Color.web("#475569"));
                feedbackCircles[row][i].setStroke(Color.web("#94a3b8"));
                feedbackCircles[row][i].setStrokeWidth(1.2);

            } else {
                feedbackCircles[row][i].setFill(Color.web("#1e293b"));
                feedbackCircles[row][i].setStroke(Color.web("#334155"));
                feedbackCircles[row][i].setStrokeWidth(1.0);
            }
        }
    }

    private RadialGradient radialGradientFor(Color base) {
        return new RadialGradient(
                0, 0,
                0.35, 0.3,
                0.65,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, base.brighter().brighter()),
                new Stop(0.5, base),
                new Stop(1.0, base.darker().darker())
        );
    }

    private int countSymbol(String text, char symbol) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == symbol) count++;
        }
        return count;
    }

    private Circle createEmptyGuessCircle() {
        Circle c = new Circle(18);
        c.setFill(Color.web("#1e293b"));
        c.setStroke(Color.web("#334155"));
        c.setStrokeWidth(1.5);

        InnerShadow inner = new InnerShadow();
        inner.setColor(Color.web("#000000", 0.4));
        inner.setRadius(6);
        c.setEffect(inner);

        return c;
    }

    private Circle createEmptyFeedbackCircle() {
        Circle c = new Circle(6);
        c.setFill(Color.web("#0f172a"));
        c.setStroke(Color.web("#1e293b"));
        c.setStrokeWidth(1.0);
        return c;
    }

    private Color colorForGuess(char ch) {
        return switch (ch) {
            case 'R' -> Color.web("#ef4444");
            case 'G' -> Color.web("#22c55e");
            case 'B' -> Color.web("#3b82f6");
            case 'Y' -> Color.web("#facc15");
            case 'O' -> Color.web("#f97316");
            case 'P' -> Color.web("#a855f7");
            default  -> Color.web("#334155");
        };
    }

    private void setInfoMessage(String text) {
        messageLabel.setText(text);
        messageLabel.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #93c5fd;"
        );
    }

    private void setSuccessMessage(String text) {
        messageLabel.setText(text);
        messageLabel.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #4ade80;"
        );
    }

    private void setErrorMessage(String text) {
        messageLabel.setText(text);
        messageLabel.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #f87171;"
        );
    }
}