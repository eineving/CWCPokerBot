package se.cygni.texasholdem.player;

import se.cygni.texasholdem.game.Card;
import se.cygni.texasholdem.game.Hand;
import se.cygni.texasholdem.game.util.PokerHandUtil;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

/**
 * Created by Test on 2015-04-16.
 */
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

    public double ratioOfBetterHands() {
        //TODO Implement
        return 1;
    }

    private boolean betterThanMyHand(List<Card> possibleHand) {
        PokerHandUtil my = new PokerHandUtil(communityCards, privateCards);
        PokerHandUtil opponent = new PokerHandUtil(communityCards, possibleHand);

        //If the hands have the same typ and needs to be decided by value of most significant card/cards in the hand
        if (my.getBestHand().getPokerHand().getOrderValue() == opponent.getBestHand().getPokerHand().getOrderValue()) {

            //TODO Refactor
            switch (my.getBestHand().getPokerHand().getOrderValue()) {
                case 1:
                    //High hand
                    return getHighestCard(my.getBestHand()) < getHighestCard(opponent.getBestHand());
                case 2:
                    //Pair
                    return getHighestPair(my.getBestHand()) < getHighestPair(opponent.getBestHand());
                case 3:
                    //Two Pair
                    if (getHighestPair(my.getBestHand()) == getHighestPair(opponent.getBestHand())) {
                        return getLowestPair(my.getBestHand()) < getLowestPair(opponent.getBestHand());
                    }
                    return getHighestPair(my.getBestHand()) < getHighestPair(opponent.getBestHand());
                case 4:
                    //TODO Implement
            }


        } else {
            return my.getBestHand().getPokerHand().getOrderValue() < opponent.getBestHand().getPokerHand().getOrderValue();
        }

        //Failsafe
        return true;
    }

    private int getLowestPair(Hand hand) {
        List<Card> sortedHand = sortHand(hand.getCards());
        for (int i = 4; i > 0; i--) {
            if (sortedHand.get(i).getRank().getOrderValue() == sortedHand.get(i - 1).getRank().getOrderValue()) {
                //check if there is three of a kind
                if (i > 1 && sortedHand.get(i).getRank().getOrderValue() != sortedHand.get(i - 2).getRank().getOrderValue()) {
                    return sortedHand.get(i).getRank().getOrderValue();
                } else if (i == 1) {
                    return sortedHand.get(i).getRank().getOrderValue();
                }
            }
        }
        //Failsafe
        return 0;
    }

    private int getHighestPair(Hand hand) {
        List<Card> sortedHand = sortHand(hand.getCards());
        for (int i = 0; i < 4; i++) {
            if (sortedHand.get(i).getRank().getOrderValue() == sortedHand.get(i + 1).getRank().getOrderValue()) {
                //check if there is three of a kind
                if (i < 3 && sortedHand.get(i).getRank().getOrderValue() != sortedHand.get(i + 2).getRank().getOrderValue()) {
                    return sortedHand.get(i).getRank().getOrderValue();
                } else if (i == 3) {
                    return sortedHand.get(i).getRank().getOrderValue();
                }
            }
        }
        //Failsafe
        return 0;
    }

    private int getHighestCard(Hand hand) {
        int temp = 0;
        for (Card card : hand.getCards()) {
            if (card.getRank().getOrderValue() > temp) {
                temp = card.getRank().getOrderValue();
            }
        }
        return temp;
    }

    /**
     * Returns the list of cards sorted with the highest first
     *
     * @param cards
     * @return
     */
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
