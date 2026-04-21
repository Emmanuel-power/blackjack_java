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
import java.util.Collections;
import java.util.List;

// Represents a full 52-card deck that is shuffled on creation
public class Deck {
    private List<Card> cards;

    private static final String[] SUITS = {"Hearts", "Diamonds", "Clubs", "Spades"};
    private static final String[] RANKS = {"2","3","4","5","6","7","8","9","10","Jack","Queen","King","Ace"};

    public Deck() {
        cards = new ArrayList<>();

        // Build all 52 cards by combining each suit with each rank
        for (String suit : SUITS) {
            for (String rank : RANKS) {
                int value;
                // Face cards are worth 10, Aces start at 11 (adjusted in Hand if needed)
                switch (rank) {
                    case "Jack": case "Queen": case "King": value = 10; break;
                    case "Ace": value = 11; break;
                    default: value = Integer.parseInt(rank); break;
                }
                cards.add(new Card(suit, rank, value));
            }
        }
        Collections.shuffle(cards);
    }

    // Removes and returns the top card; reshuffles a fresh deck if empty
    public Card deal() {
        if (cards.isEmpty()) {
            Deck fresh = new Deck();
            cards = fresh.cards;
        }
        return cards.remove(0);
    }

    public int size() { return cards.size(); }
}
