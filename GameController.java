package com.blackjackgame;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.List;

/**
 * Builds the entire game UI and wires it to BlackjackGameMain (the engine).
 *
 * Layout (top → bottom):
 *   ┌─────────────────────────────────────────┐
 *   │  DEALER  (score label)                  │
 *   │  [card]  [card_back] ...                │
 *   │─────────────────────────────────────────│
 *   │           result message                │
 *   │─────────────────────────────────────────│
 *   │  YOUR HAND  (score label)               │
 *   │  [card]  [card] ...                     │
 *   │─────────────────────────────────────────│
 *   │  Wins: 0      [HIT]  [STAND]   [DEAL]   │
 *   └─────────────────────────────────────────┘
 */
public class GameController {

    // ── Friend's game engine ──────────────────────────────────────────────────
    private final BlackjackGameMain game = new BlackjackGameMain();

    // ── Root layout ───────────────────────────────────────────────────────────
    private final VBox root = new VBox(10);

    // ── UI nodes we need to update at runtime ────────────────────────────────
    private final HBox  dealerCardsBox    = new HBox(8);
    private final HBox  playerCardsBox    = new HBox(8);
    private final Label dealerScoreLabel  = makeLabel("DEALER", 17, true);
    private final Label playerScoreLabel  = makeLabel("YOUR HAND", 17, true);
    private final Label resultLabel       = makeLabel("", 30, true);
    private final Label winsLabel         = makeLabel("Wins: 0", 15, false);

    private final Button hitBtn      = makeButton("HIT",   "#e63946");
    private final Button standBtn    = makeButton("STAND", "#457b9d");
    private final Button dealBtn     = makeButton("DEAL",  "#2a9d8f");

    // ── Image cache so we only load each PNG once ─────────────────────────────
    private java.util.Map<String, Image> imageCache = new java.util.HashMap<>();

    // ─────────────────────────────────────────────────────────────────────────

    public GameController() {
        buildLayout();
        wireButtons();
        setButtonState(false, false, true);   // only DEAL enabled at startup
    }

    // ── Layout construction ──────────────────────────────────────────────────

    private void buildLayout() {
        root.setPadding(new Insets(20, 30, 20, 30));
        root.setAlignment(Pos.TOP_CENTER);

        // Try to load the felt background image; fall back to solid green
        var bgStream = getClass().getResourceAsStream("/com/blackjackgame/cards/table_bg.png");
        if (bgStream != null) {
            Image bgImg = new Image(bgStream);
            BackgroundImage bg = new BackgroundImage(
                bgImg,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
            );
            root.setBackground(new Background(bg));
        } else {
            root.setStyle("-fx-background-color: #1a6b3c;");
        }

        // Dealer section
        dealerCardsBox.setAlignment(Pos.CENTER);
        dealerCardsBox.setMinHeight(120);
        VBox dealerSection = new VBox(6, dealerScoreLabel, dealerCardsBox);
        dealerSection.setAlignment(Pos.CENTER);

        // Result banner (hidden until a round ends)
        resultLabel.setTextFill(Color.GOLD);
        resultLabel.setMinHeight(50);
        resultLabel.setStyle("-fx-effect: dropshadow(gaussian, black, 4, 0.6, 0, 1);");

        // Player section
        playerCardsBox.setAlignment(Pos.CENTER);
        playerCardsBox.setMinHeight(120);
        VBox playerSection = new VBox(6, playerScoreLabel, playerCardsBox);
        playerSection.setAlignment(Pos.CENTER);

        // Button bar
        winsLabel.setTextFill(Color.LIGHTGREEN);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox buttonBar = new HBox(14, winsLabel, spacer, hitBtn, standBtn, dealBtn);
        buttonBar.setAlignment(Pos.CENTER_LEFT);
        buttonBar.setPadding(new Insets(10, 0, 0, 0));

        // Dividers
        Region gap1 = new Region(); VBox.setVgrow(gap1, Priority.ALWAYS);
        Region gap2 = new Region(); VBox.setVgrow(gap2, Priority.ALWAYS);
        Region gap3 = new Region(); VBox.setVgrow(gap3, Priority.SOMETIMES);

        root.getChildren().addAll(
            dealerSection,
            gap1,
            resultLabel,
            gap2,
            playerSection,
            gap3,
            buttonBar
        );
    }

    // ── Button wiring ─────────────────────────────────────────────────────────

    private void wireButtons() {
        dealBtn.setOnAction(e  -> onDeal());
        hitBtn.setOnAction(e   -> onHit());
        standBtn.setOnAction(e -> onStand());
    }

    // ── Button click handlers ─────────────────────────────────────────────────

    /** Start a new round: clear hands, deal 2 cards each, hide dealer's second card. */
    private void onDeal() {
        resultLabel.setText("");
        game.startRound();

        renderDealerCards(/*hideSecond=*/ true);
        renderPlayerCards();
        updateScoreLabels(/*hideDealer=*/ true);

        setButtonState(true, true, false);
    }

    /** Player takes another card. End the round immediately if they bust. */
    private void onHit() {
        game.playerHit();
        renderPlayerCards();
        updateScoreLabels(true);

        if (game.getPlayerHand().isBust()) {
            finishRound();
        }
    }

    /**
     * Player stands: dealer draws to 17+, flip the hidden card,
     * evaluate result, show outcome.
     */
    private void onStand() {
        game.dealerPlay();
        renderDealerCards(/*hideSecond=*/ false);
        updateScoreLabels(/*hideDealer=*/ false);
        finishRound();
    }

    // ── Round finalization ───────────────────────────────────────────────────

    private void finishRound() {
        // Always reveal dealer's cards when round ends
        renderDealerCards(false);
        updateScoreLabels(false);

        BlackjackGameMain.Result result = game.evaluateResult();

        String msg = switch (result) {
            case PLAYER_WIN -> "🏆  YOU WIN!";
            case DEALER_WIN -> "💀  DEALER WINS";
            case PUSH       -> "🤝  PUSH — TIE";
        };

        resultLabel.setText(msg);
        winsLabel.setText("Wins: " + game.getWins());

        // Fade the result label in
        FadeTransition ft = new FadeTransition(Duration.millis(400), resultLabel);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        setButtonState(false, false, true);   // only DEAL active
    }

    // ── Render helpers ───────────────────────────────────────────────────────

    /**
     * Redraws the dealer's card row.
     * @param hideSecond  true during player's turn (second card stays face-down)
     */
    private void renderDealerCards(boolean hideSecond) {
        dealerCardsBox.getChildren().clear();
        List<Card> cards = game.getDealerHand().getCards();

        for (int i = 0; i < cards.size(); i++) {
            String key = (i == 1 && hideSecond) ? "card_back" : cardKey(cards.get(i));
            dealerCardsBox.getChildren().add(cardView(key));
        }
    }

    /** Redraws the player's card row. */
    private void renderPlayerCards() {
        playerCardsBox.getChildren().clear();
        for (Card card : game.getPlayerHand().getCards()) {
            playerCardsBox.getChildren().add(cardView(cardKey(card)));
        }
    }

    /**
     * Updates the score labels above each hand.
     * @param hideDealer  true during player's turn (show only first dealer card value)
     */
    private void updateScoreLabels(boolean hideDealer) {
        int pv = game.getPlayerHand().getValue();
        playerScoreLabel.setText("YOUR HAND   (" + pv + ")");

        if (hideDealer) {
            int visible = game.getDealerHand().getCards().get(0).getValue();
            dealerScoreLabel.setText("DEALER   (" + visible + " + ?)");
        } else {
            int dv = game.getDealerHand().getValue();
            dealerScoreLabel.setText("DEALER   (" + dv + ")");
        }
    }

    // ── Image loading ────────────────────────────────────────────────────────

    /**
     * Builds the resource-path key for a Card.
     * Card.getRank() returns e.g. "Ace", "10", "King"
     * Card.getSuit() returns e.g. "Spades"
     * → key becomes "ace_of_spades" → file is "ace_of_spades.png"
     */
    private String cardKey(Card card) {
        return card.getRank().toLowerCase() + "_of_" + card.getSuit().toLowerCase();
    }

    /**
     * Returns an ImageView for a card image key, using a cache to avoid
     * reloading the same PNG more than once per session.
     */
    private ImageView cardView(String key) {
        Image img = imageCache.computeIfAbsent(key, k -> {
            String path = "/com/blackjackgame/cards/" + k + ".png";
            var stream = getClass().getResourceAsStream(path);
            return (stream != null) ? new Image(stream) : null;
        });

        ImageView iv = new ImageView();
        if (img != null) iv.setImage(img);
        iv.setFitWidth(90);
        iv.setFitHeight(126);
        iv.setPreserveRatio(false);
        iv.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 6, 0.3, 2, 2);");
        return iv;
    }

    // ── Utility constructors ─────────────────────────────────────────────────

    private Label makeLabel(String text, int size, boolean bold) {
        Label l = new Label(text);
        l.setFont(bold
            ? Font.font("Arial", FontWeight.BOLD, size)
            : Font.font("Arial", size));
        l.setTextFill(Color.WHITE);
        return l;
    }

    private Button makeButton(String text, String hex) {
        Button b = new Button(text);
        b.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        b.setMinWidth(90);
        b.setStyle(
            "-fx-background-color: " + hex + ";"  +
            "-fx-text-fill: white;"               +
            "-fx-background-radius: 8;"           +
            "-fx-padding: 10 20 10 20;"           +
            "-fx-cursor: hand;"
        );
        // Hover darken effect via mouse events
        String darken = "-fx-background-color: derive(" + hex + ", -15%);"
                      + "-fx-text-fill: white;"
                      + "-fx-background-radius: 8;"
                      + "-fx-padding: 10 20 10 20;"
                      + "-fx-cursor: hand;";
        b.setOnMouseEntered(e -> { if (!b.isDisabled()) b.setStyle(darken); });
        b.setOnMouseExited(e  -> b.setStyle(
            "-fx-background-color: " + hex + ";"  +
            "-fx-text-fill: white;"               +
            "-fx-background-radius: 8;"           +
            "-fx-padding: 10 20 10 20;"           +
            "-fx-cursor: hand;"
        ));
        return b;
    }

    /** Enables/disables the three action buttons. */
    private void setButtonState(boolean hit, boolean stand, boolean deal) {
        hitBtn.setDisable(!hit);
        standBtn.setDisable(!stand);
        dealBtn.setDisable(!deal);
    }

    /** Returns the root VBox so BlackjackApp can attach it to a Scene. */
    public VBox getRoot() { return root; }
}
