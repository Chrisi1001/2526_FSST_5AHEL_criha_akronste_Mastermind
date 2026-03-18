package org.example.mastermind;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class HelloController {

    @FXML
    private TextField inputField;

    @FXML
    private TextArea historyArea;

    @FXML
    private Label remainingLabel;

    @FXML
    private Label messageLabel;

    private MastermindModel model;

    @FXML
    public void initialize() {
        model = new MastermindModel();
        remainingLabel.setText("Verbleibende Versuche: " + model.getRemainingAttempts());
        messageLabel.setText("Gib 4 Farben ein: R, G, B, Y, O, P");
    }

    @FXML
    protected void onSubmitButtonClick() {
        String input = inputField.getText();

        if (!model.isValidGuess(input)) {
            messageLabel.setText("Ungültige Eingabe! Bitte genau 4 Zeichen aus R, G, B, Y, O, P eingeben.");
            inputField.clear();
            return;
        }

        String result = model.checkGuess(input);

        String guess = input.toUpperCase().replaceAll("\\s+", "");
        historyArea.appendText(guess + "   ->   " + result + "\n");

        remainingLabel.setText("Verbleibende Versuche: " + model.getRemainingAttempts());

        if (model.isWon()) {
            messageLabel.setText("Gewonnen! Der Code war richtig.");
            inputField.setDisable(true);
            return;
        }

        if (model.isGameOver()) {
            messageLabel.setText("Verloren! Der geheime Code war: " + model.getSecretCode());
            inputField.setDisable(true);
            return;
        }

        messageLabel.setText("Auswertung: " + result);
        inputField.clear();
    }
}