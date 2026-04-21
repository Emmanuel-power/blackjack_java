/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.blackjackgame;

/**
 *
 * @author toosi
 */


import java.util.ArrayList;
import java.util.List;

// Represents a hand of cards held by the player or dealer
public class Hand {
    private List<Card> cards;

    public Hand() {
        cards = new ArrayList<>();
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    // Calculates total hand value, counting aces as 1 if needed to avoid busting
    public int getValue() {
        int total = 0;
        int aces = 0;

        for (Card card : cards) {
            total += card.getValue();
            if (card.getRank().equals("Ace")) aces++;
        }

        // Drop each ace from 11 to 1 as long as the hand is over 21
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }

        return total;
    }

    public List<Card> getCards() { return cards; }

    // Clears all cards from the hand at the start of a new round
    public void clear() { cards.clear(); }

    public boolean isBust() { return getValue() > 21; }
}