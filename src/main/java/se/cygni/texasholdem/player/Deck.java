package se.cygni.texasholdem.player;

import se.cygni.texasholdem.game.Card;
import se.cygni.texasholdem.game.definitions.Rank;
import se.cygni.texasholdem.game.definitions.Suit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Test on 2015-04-21.
 */
public class Deck {
    List<Card> cards = new ArrayList<Card>();

    public Deck() {
        fillDeck();
    }

    public void fillDeck() {
        cards.clear();
        Suit[] suits = new Suit[4];
        suits[0] = Suit.CLUBS;
        suits[1] = Suit.DIAMONDS;
        suits[2] = Suit.HEARTS;
        suits[3] = Suit.SPADES;
        for (Suit suit : suits) {
            cards.add(new Card(Rank.ACE, suit));
            cards.add(new Card(Rank.DEUCE, suit));
            cards.add(new Card(Rank.THREE, suit));
            cards.add(new Card(Rank.FOUR, suit));
            cards.add(new Card(Rank.FIVE, suit));
            cards.add(new Card(Rank.SIX, suit));
            cards.add(new Card(Rank.SEVEN, suit));
            cards.add(new Card(Rank.EIGHT, suit));
            cards.add(new Card(Rank.NINE, suit));
            cards.add(new Card(Rank.TEN, suit));
            cards.add(new Card(Rank.JACK, suit));
            cards.add(new Card(Rank.QUEEN, suit));
            cards.add(new Card(Rank.KING, suit));
        }
    }

    public boolean removeCard(Card rCard) {
        return cards.remove(rCard);
    }

    public int getDeckSize() {
        return cards.size();
    }

    public List<List<Card>> getPossibleTuples() {
        List<List<Card>> tuples = new ArrayList<List<Card>>();
        for (int i = 0; i < cards.size(); i++) {
            for (int j = i+1; j < cards.size(); j++) {
                List<Card> temp = new ArrayList<Card>();
                temp.add(cards.get(i));
                temp.add(cards.get(j));
                tuples.add(temp);
            }
        }
        System.out.println("Amount of tuples:" + tuples.size());
        return tuples;
    }
}
