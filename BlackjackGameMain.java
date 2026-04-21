/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.blackjackgame;

/**
 *
 * @author toosi
 */


// Manages round flow, dealer logic, win/loss evaluation, and win tracking
public class BlackjackGameMain {
    private Deck deck;
    private Hand playerHand;
    private Hand dealerHand;
    private int wins; // tracks total wins for the session

    public enum Result { PLAYER_WIN, DEALER_WIN, PUSH }

    public BlackjackGameMain() {
        deck = new Deck();
        playerHand = new Hand();
        dealerHand = new Hand();
        wins = 0;
    }

    // Clears both hands and deals two cards to each side to start a round
    public void startRound() {
        playerHand.clear();
        dealerHand.clear();
        playerHand.addCard(deck.deal());
        playerHand.addCard(deck.deal());
        dealerHand.addCard(deck.deal());
        dealerHand.addCard(deck.deal());
    }

    // Deals one card to the player when they choose to hit
    public void playerHit() {
        playerHand.addCard(deck.deal());
    }

    // Dealer draws cards until reaching a hand value of 17 or higher
    public void dealerPlay() {
        while (dealerHand.getValue() < 17) {
            dealerHand.addCard(deck.deal());
        }
    }

    // Compares hand values to determine the outcome and updates win count
    public Result evaluateResult() {
        int playerScore = playerHand.getValue();
        int dealerScore = dealerHand.getValue();

        Result result;

        if (playerHand.isBust()) {
            result = Result.DEALER_WIN;         // player went over 21
        } else if (dealerHand.isBust()) {
            result = Result.PLAYER_WIN;         // dealer went over 21
        } else if (playerScore > dealerScore) {
            result = Result.PLAYER_WIN;         // player has higher score
        } else if (dealerScore > playerScore) {
            result = Result.DEALER_WIN;         // dealer has higher score
        } else {
            result = Result.PUSH;               // tied
        }

        if (result == Result.PLAYER_WIN) wins++;
        return result;
    }

    public Hand getPlayerHand() { return playerHand; }
    public Hand getDealerHand() { return dealerHand; }
    public int getWins() { return wins; }
}