package org.example.mastermind;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HelloController {

    @FXML private Label   remainingLabel;
    @FXML private Label   messageLabel;
    @FXML private VBox    historyBox;
    @FXML private HBox    paletteBox;
    @FXML private VBox    hintPopup;
    @FXML private WebView instructionWebView;

    private MastermindModel model;
    private final char[][]   placedColors    = new char[10][4];
    private final Circle[][] guessCircles    = new Circle[10][4];
    private final Circle[][] feedbackCircles = new Circle[10][4];
    private int     currentRow = 0;
    private boolean hintUsed   = false;

    private static final char[]   COLOR_KEYS = {'R','G','B','Y','O','P'};
    private static final String[] COLOR_HEX  = {
            "#ef4444","#22c55e","#3b82f6","#facc15","#f97316","#a855f7"
    };

    private final boolean[] paletteVisible = new boolean[6];

    // ── Einstiegspunkt ─────────────────────────────────────────────
    @FXML
    public void initialize() {
        loadInstructionHtml();
        buildPalette();
        startNewGame();
    }

    // =========================================================================
    //  Anleitung (WebView)
    // =========================================================================

    private void loadInstructionHtml() {
        if (instructionWebView == null) return;

        String html = """
            <!DOCTYPE html>
            <html lang="de">
            <head>
            <meta charset="UTF-8"/>
            <style>
              * { box-sizing: border-box; margin: 0; padding: 0; }
              body {
                background: #0f172a;
                color: #cbd5e1;
                font-family: 'Segoe UI', Arial, sans-serif;
                font-size: 13px;
                padding: 14px;
              }
              h2 {
                color: #93c5fd;
                font-size: 15px;
                margin-bottom: 14px;
                padding-bottom: 6px;
                border-bottom: 1px solid #1e3a5f;
                letter-spacing: 1px;
              }
              .section {
                background: #1e293b;
                border-radius: 10px;
                padding: 10px 12px;
                margin-bottom: 10px;
                border-left: 3px solid #3b82f6;
              }
              .section-title {
                color: #7dd3fc;
                font-weight: bold;
                font-size: 13px;
                margin-bottom: 5px;
              }
              .section p { color: #94a3b8; line-height: 1.5; }
              .chips { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 6px; }
              .chip {
                display: inline-block;
                width: 30px; height: 30px;
                border-radius: 50%;
                color: white; font-weight: bold; font-size: 13px;
                text-align: center; line-height: 30px;
                box-shadow: 0 0 6px rgba(0,0,0,0.5);
              }
              .feedback-row { display: flex; align-items: center; gap: 10px; margin-top: 6px; }
              .dot { width: 14px; height: 14px; border-radius: 50%; display: inline-block; flex-shrink: 0; }
              .dot-white { background: #f8fafc; box-shadow: 0 0 5px #fff; }
              .dot-gray  { background: #475569; border: 1px solid #94a3b8; }
              .tip {
                background: #0c1a2e; border: 1px solid #1e3a5f;
                border-radius: 8px; padding: 8px 10px; margin-top: 6px;
                color: #64748b; font-style: italic; font-size: 12px;
              }
              .tag {
                display: inline-block; background: #1e3a5f; color: #7dd3fc;
                border-radius: 4px; padding: 1px 7px;
                font-size: 11px; font-weight: bold; margin-right: 4px;
              }
            </style>
            </head>
            <body>
              <h2>Spielanleitung</h2>
              <div class="section">
                <div class="section-title"><span class="tag">ZIEL</span> Ziel des Spiels</div>
                <p>Errate den geheimen <strong style="color:#e2e8f0">4-Farben-Code</strong>
                   in maximal <strong style="color:#38bdf8">10 Versuchen</strong>.</p>
              </div>
              <div class="section">
                <div class="section-title"><span class="tag">FARBEN</span> Verfügbare Farben</div>
                <div class="chips">
                  <div class="chip" style="background:#ef4444;">R</div>
                  <div class="chip" style="background:#22c55e;">G</div>
                  <div class="chip" style="background:#3b82f6;">B</div>
                  <div class="chip" style="background:#facc15; color:#1e293b;">Y</div>
                  <div class="chip" style="background:#f97316;">O</div>
                  <div class="chip" style="background:#a855f7;">P</div>
                </div>
              </div>
              <div class="section">
                <div class="section-title"><span class="tag">INPUT</span> Eingabe</div>
                <p>Ziehe die Farbkreise per Drag &amp; Drop in die Felder der aktuellen Reihe.
                   Klicke auf ein befülltes Feld, um es wieder zu leeren.</p>
              </div>
              <div class="section">
                <div class="section-title"><span class="tag">INFO</span> Feedback-Punkte</div>
                <div class="feedback-row">
                  <div class="dot dot-white"></div>
                  <span><strong style="color:#f8fafc">Weisser Punkt</strong> –
                    richtige Farbe, richtige Position</span>
                </div>
                <div class="feedback-row" style="margin-top:8px;">
                  <div class="dot dot-gray"></div>
                  <span><strong style="color:#94a3b8">Grauer Punkt</strong> –
                    richtige Farbe, falsche Position</span>
                </div>
                <div class="tip" style="margin-top:10px;">
                  Kein Punkt = diese Farbe kommt im Code nicht vor.
                </div>
              </div>
              <div class="section">
                <div class="section-title"><span class="tag">TIPP</span> Tipp-Button</div>
                <p>Du hast <strong style="color:#38bdf8">einen Tipp</strong> pro Spiel.
                   Wähle zwischen einem leichten Tipp (1 Farbe ausblenden)
                   oder einem starken Tipp (2 Farben ausblenden).</p>
              </div>
              <div class="section">
                <div class="section-title"><span class="tag">NEU</span> Neues Spiel</div>
                <p>Klicke auf <strong style="color:#4ade80">Neustart</strong>,
                   um eine neue Runde zu starten.</p>
              </div>
              <div class="section" style="border-left-color:#a855f7;">
                <div class="section-title"><span class="tag" style="background:#3b1a5f;color:#c084fc;">STRATEGIE</span> Tipp</div>
                <p>Beginne mit möglichst <em>verschiedenen</em> Farben,
                   um schnell Informationen über den Code zu gewinnen.</p>
              </div>
            </body>
            </html>
        """;

        instructionWebView.getEngine().loadContent(html, "text/html");
    }

    // =========================================================================
    //  Palette aufbauen
    // =========================================================================

    private void buildPalette() {
        paletteBox.getChildren().clear();
        Arrays.fill(paletteVisible, true);

        for (int i = 0; i < COLOR_KEYS.length; i++) {
            final char   key = COLOR_KEYS[i];
            final String hex = COLOR_HEX[i];
            final Color  col = Color.web(hex);
            final int    idx = i;

            Circle circle = new Circle(22);
            circle.setFill(radialGradientFor(col));
            circle.setStroke(col.brighter());
            circle.setStrokeWidth(2);
            circle.setCursor(Cursor.OPEN_HAND);

            DropShadow glow = new DropShadow();
            glow.setColor(Color.web(hex, 0.75));
            glow.setRadius(14);
            glow.setSpread(0.3);
            circle.setEffect(glow);

            circle.setOnMouseEntered(e -> { circle.setScaleX(1.2); circle.setScaleY(1.2); });
            circle.setOnMouseExited (e -> { circle.setScaleX(1.0); circle.setScaleY(1.0); });

            circle.setOnDragDetected(e -> {
                if (!paletteVisible[idx]) return;
                Dragboard db = circle.startDragAndDrop(TransferMode.COPY);
                ClipboardContent cc = new ClipboardContent();
                cc.putString(String.valueOf(key));
                db.setContent(cc);
                e.consume();
            });

            Label lbl = new Label(String.valueOf(key));
            lbl.setStyle(
                    "-fx-font-size:11px;-fx-font-weight:bold;" +
                            "-fx-text-fill:#64748b;-fx-font-family:'Courier New';"
            );

            VBox cell = new VBox(6, circle, lbl);
            cell.setAlignment(Pos.CENTER);
            paletteBox.getChildren().add(cell);
        }
    }

    // =========================================================================
    //  Spielstart
    // =========================================================================

    private void startNewGame() {
        model      = new MastermindModel();
        currentRow = 0;
        hintUsed   = false;
        for (char[] row : placedColors) Arrays.fill(row, (char) 0);

        Arrays.fill(paletteVisible, true);
        for (int i = 0; i < paletteBox.getChildren().size(); i++) {
            VBox cell = (VBox) paletteBox.getChildren().get(i);
            cell.setOpacity(1.0);
            cell.setDisable(false);
        }

        buildEmptyBoard();
        hideHintPopup();

        remainingLabel.setText("Verbleibende Versuche: " + model.getRemainingAttempts());
        setInfoMessage("Ziehe Farben in die Felder — Reihe 1");
    }

    @FXML
    protected void onRestartButtonClick() {
        startNewGame();
    }

    // =========================================================================
    //  Reihe prüfen
    // =========================================================================

    @FXML
    protected void onSubmitButtonClick() {
        if (model.isGameOver()) return;
        hideHintPopup();

        StringBuilder sb = new StringBuilder();
        for (int col = 0; col < 4; col++) {
            char c = placedColors[currentRow][col];
            if (c == 0) { setErrorMessage("Bitte alle 4 Felder befüllen!"); return; }
            sb.append(c);
        }

        String result = model.checkGuess(sb.toString());
        fillFeedback(currentRow, result);
        currentRow++;

        remainingLabel.setText("Verbleibende Versuche: " + model.getRemainingAttempts());

        if (model.isWon())      { setSuccessMessage("Gewonnen! Du hast den Code erraten!"); return; }
        if (model.isGameOver()) { setErrorMessage("Verloren! Der Code war: " + model.getSecretCode()); return; }

        setInfoMessage("Ziehe Farben in die Felder — Reihe " + (currentRow + 1));
    }

    // =========================================================================
    //  Tipp-System
    // =========================================================================

    @FXML
    protected void onHintButtonClick() {
        if (model.isGameOver()) return;

        if (hintUsed) {
            setInfoMessage("Du hast bereits einen Tipp für dieses Spiel verwendet!");
            hideHintPopup();
            return;
        }

        if (hintPopup.isVisible()) {
            hideHintPopup();
        } else {
            showHintPopup();
        }
    }

    @FXML
    protected void onEasyHintClick() {
        hideHintPopup();
        applyHint(1);
    }

    @FXML
    protected void onStrongHintClick() {
        hideHintPopup();
        applyHint(2);
    }

    private void showHintPopup() {
        hintPopup.setVisible(true);
        hintPopup.setManaged(true);
    }

    private void hideHintPopup() {
        hintPopup.setVisible(false);
        hintPopup.setManaged(false);
    }

    private void applyHint(int count) {
        hintUsed = true;
        String secret = model.getSecretCode();

        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < COLOR_KEYS.length; i++) {
            if (paletteVisible[i] && secret.indexOf(COLOR_KEYS[i]) == -1) {
                candidates.add(i);
            }
        }

        if (candidates.isEmpty()) {
            setInfoMessage("Alle verbleibenden Farben kommen im Code vor!");
            return;
        }

        Collections.shuffle(candidates);
        int removed = 0;

        for (int idx : candidates) {
            if (removed >= count) break;

            paletteVisible[idx] = false;
            VBox cell = (VBox) paletteBox.getChildren().get(idx);
            cell.setOpacity(0.15);
            cell.setDisable(true);

            for (int col = 0; col < 4; col++) {
                if (placedColors[currentRow][col] == COLOR_KEYS[idx]) {
                    placedColors[currentRow][col] = 0;
                    resetGuessCircle(guessCircles[currentRow][col]);
                }
            }
            removed++;
        }

        if (removed == 1) {
            setInfoMessage("Eine Farbe, die nicht im Code vorkommt, wurde entfernt.");
        } else {
            setInfoMessage(removed + " Farben, die nicht im Code vorkommen, wurden entfernt.");
        }
    }

    // =========================================================================
    //  Board aufbauen
    // =========================================================================

    private void buildEmptyBoard() {
        historyBox.getChildren().clear();

        for (int row = 0; row < 10; row++) {
            final int r = row;

            HBox rowBox = new HBox(14);
            rowBox.setAlignment(Pos.CENTER_LEFT);
            rowBox.setPadding(new Insets(8, 14, 8, 14));
            rowBox.setStyle(
                    "-fx-background-color:" + (row % 2 == 0 ? "#1e293b" : "#0f172a") + ";" +
                            "-fx-background-radius:12;"
            );

            Label num = new Label(String.format("%2d", row + 1));
            num.setMinWidth(26);
            num.setStyle(
                    "-fx-font-size:13px;-fx-font-weight:bold;" +
                            "-fx-text-fill:#475569;-fx-font-family:'Courier New';"
            );

            HBox guessBox = new HBox(10);
            guessBox.setAlignment(Pos.CENTER_LEFT);

            for (int col = 0; col < 4; col++) {
                final int c = col;

                Circle circle = createEmptyGuessCircle();
                guessCircles[row][col] = circle;

                StackPane slot = new StackPane(circle);
                slot.setMinSize(44, 44);
                slot.setMaxSize(44, 44);
                slot.setAlignment(Pos.CENTER);

                slot.setOnDragOver(e -> {
                    if (e.getDragboard().hasString() && !model.isGameOver() && r == currentRow)
                        e.acceptTransferModes(TransferMode.COPY);
                    e.consume();
                });
                slot.setOnDragEntered(e -> {
                    if (!model.isGameOver() && r == currentRow && e.getDragboard().hasString()) {
                        circle.setStroke(Color.web("#60a5fa"));
                        circle.setStrokeWidth(3);
                    }
                });
                slot.setOnDragExited(e -> {
                    if (placedColors[r][c] == 0) {
                        circle.setStroke(Color.web("#334155"));
                        circle.setStrokeWidth(1.5);
                    }
                });
                slot.setOnDragDropped(e -> {
                    Dragboard db = e.getDragboard();
                    if (db.hasString() && r == currentRow && !model.isGameOver()) {
                        char color = db.getString().charAt(0);
                        int colorIdx = indexForChar(color);
                        if (colorIdx >= 0 && paletteVisible[colorIdx]) {
                            placedColors[r][c] = color;
                            applyColorToCircle(guessCircles[r][c], color);
                            e.setDropCompleted(true);
                        } else {
                            e.setDropCompleted(false);
                        }
                    } else {
                        e.setDropCompleted(false);
                    }
                    e.consume();
                });
                slot.setOnMouseClicked(e -> {
                    if (placedColors[r][c] != 0 && r == currentRow && !model.isGameOver()) {
                        placedColors[r][c] = 0;
                        resetGuessCircle(guessCircles[r][c]);
                    }
                });
                slot.setCursor(Cursor.HAND);
                guessBox.getChildren().add(slot);
            }

            GridPane feedbackGrid = new GridPane();
            feedbackGrid.setHgap(5);
            feedbackGrid.setVgap(5);
            feedbackGrid.setAlignment(Pos.CENTER);
            feedbackGrid.setMinWidth(36);

            for (int i = 0; i < 4; i++) {
                Circle small = createEmptyFeedbackCircle();
                feedbackCircles[row][i] = small;
                feedbackGrid.add(small, i % 2, i / 2);
            }

            rowBox.getChildren().addAll(num, guessBox, feedbackGrid);
            historyBox.getChildren().add(rowBox);
        }
    }

    // =========================================================================
    //  Hilfsmethoden
    // =========================================================================

    private void applyColorToCircle(Circle c, char color) {
        Color base = Color.web(hexForChar(color));
        c.setFill(radialGradientFor(base));
        c.setStroke(base.brighter());
        c.setStrokeWidth(2);
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web(hexForChar(color), 0.65));
        glow.setRadius(10);
        glow.setSpread(0.2);
        c.setEffect(glow);
    }

    private void resetGuessCircle(Circle c) {
        c.setFill(Color.web("#1e293b"));
        c.setStroke(Color.web("#334155"));
        c.setStrokeWidth(1.5);
        InnerShadow inner = new InnerShadow();
        inner.setColor(Color.web("#000000", 0.4));
        inner.setRadius(6);
        c.setEffect(inner);
    }

    private void fillFeedback(int row, String result) {
        int exact   = countSymbol(result, '●');
        int partial = countSymbol(result, '○');
        for (int i = 0; i < 4; i++) {
            Circle fb = feedbackCircles[row][i];
            if (i < exact) {
                fb.setFill(radialGradientFor(Color.web("#f8fafc")));
                fb.setStroke(Color.web("#ffffff"));
                fb.setStrokeWidth(1.2);
                DropShadow ds = new DropShadow();
                ds.setColor(Color.web("#ffffff", 0.5));
                ds.setRadius(5);
                fb.setEffect(ds);
            } else if (i < exact + partial) {
                fb.setFill(Color.web("#475569"));
                fb.setStroke(Color.web("#94a3b8"));
                fb.setStrokeWidth(1.2);
            } else {
                fb.setFill(Color.web("#0f172a"));
                fb.setStroke(Color.web("#334155"));
                fb.setStrokeWidth(1.0);
            }
        }
    }

    private int countSymbol(String text, char symbol) {
        int n = 0;
        for (char c : text.toCharArray()) if (c == symbol) n++;
        return n;
    }

    private int indexForChar(char c) {
        for (int i = 0; i < COLOR_KEYS.length; i++)
            if (COLOR_KEYS[i] == c) return i;
        return -1;
    }

    private String hexForChar(char c) {
        for (int i = 0; i < COLOR_KEYS.length; i++)
            if (COLOR_KEYS[i] == c) return COLOR_HEX[i];
        return "#334155";
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

    private RadialGradient radialGradientFor(Color base) {
        return new RadialGradient(
                0, 0, 0.35, 0.3, 0.65, true,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, base.brighter().brighter()),
                new Stop(0.5, base),
                new Stop(1.0, base.darker().darker())
        );
    }

    private void setInfoMessage(String t) {
        messageLabel.setText(t);
        messageLabel.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#93c5fd;");
    }

    private void setSuccessMessage(String t) {
        messageLabel.setText(t);
        messageLabel.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#4ade80;");
    }

    private void setErrorMessage(String t) {
        messageLabel.setText(t);
        messageLabel.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#f87171;");
    }
}