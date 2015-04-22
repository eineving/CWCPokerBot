package se.cygni.texasholdem.player;

import se.cygni.texasholdem.game.Card;
import se.cygni.texasholdem.game.Hand;
import se.cygni.texasholdem.game.util.PokerHandUtil;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class PercentageCalculator {


    private List<Card> privateCards;
    private List<Card> communityCards;
    private Deck deck = new Deck();
    private String preFlopMapSearcher;
    private HashMap<String, Double> preFlopHeadsUp = new HashMap<String, Double>();
    private HashMap<String, Double> preFlopMulti = new HashMap<String, Double>();

    public PercentageCalculator() throws FileNotFoundException {
        communityCards = new LinkedList<Card>();
        Scanner scannerHeadsUp = new Scanner(new FileReader("percentages\\preFlopHeadsUp.txt"));
        Scanner scannerMulti = new Scanner(new FileReader("percentages\\preFlopMulti.txt"));

        while (scannerHeadsUp.hasNext()) {
            String[] columns = scannerHeadsUp.nextLine().split("\t");
            preFlopHeadsUp.put(columns[0], Double.valueOf(columns[1]) / 100);
        }

        while (scannerMulti.hasNext()) {
            String[] columns = scannerMulti.nextLine().split("\t");
            preFlopMulti.put(columns[0], Double.valueOf(columns[1]) / 100);
        }
    }

    public void newCards(List<Card> hand) {
        this.privateCards = hand;
        communityCards.clear();
        deck.fillDeck();

        //Removing my cards from the calculation deck
        for (Card card : privateCards) {
            deck.removeCard(card);
        }


        //Needs to have the biggest value first
        if (hand.get(0).getRank().getOrderValue() > hand.get(1).getRank().getOrderValue()) {
            preFlopMapSearcher = hand.get(0).getRank().getName() + hand.get(1).getRank().getName();
        } else {
            preFlopMapSearcher = hand.get(1).getRank().getName() + hand.get(0).getRank().getName();
        }
        //Is it the same suit then add "s" to the end of the string, otherwise "o"
        if (hand.get(0).getSuit().equals(hand.get(1).getSuit())) {
            preFlopMapSearcher = preFlopMapSearcher + "s";
        } else {
            preFlopMapSearcher = preFlopMapSearcher + "o";
        }
    }

    /**
     * Returns the precentage of winning with a given privateCards and given amount of players.
     *
     * @param nbrOfPlayers players still having a privateCards
     * @return percentage of winning if all players would not fold
     */
    public double getPreFlopPercentage(int nbrOfPlayers) {
        if (nbrOfPlayers > 2) {
            return preFlopMulti.get(preFlopMapSearcher);
        } else {
            return preFlopHeadsUp.get(preFlopMapSearcher);
        }
    }

    public void newCommunityCard(Card card) {
        communityCards.add(card);
        deck.removeCard(card);
    }

    private List<Card> sortHand(List<Card> cards) {
        List<Card> sorted = new ArrayList<Card>();
        for (int i = 0; i < 5; i++) {
            int indexOfBiggest = 0;
            int runner = 0;
            for (Card card : cards) {
                if (card.getRank().getOrderValue() > cards.get(indexOfBiggest).getRank().getOrderValue()) {
                    indexOfBiggest = runner;
                }
                runner++;
            }
            sorted.add(cards.get(indexOfBiggest));
            cards.remove(indexOfBiggest);
        }
        return sorted;
    }

}
