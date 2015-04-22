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

    public double ratioOfBetterHands() {
        List<List<Card>> possibleTuples = deck.getPossibleTuples();

        int numberOfBetterHands=0;
        double numberOfHands = possibleTuples.size();

        for(List<Card> hand : possibleTuples){
            if(betterThanMyHand(hand)){
                numberOfBetterHands++;
            }
        }

        return numberOfBetterHands/numberOfHands;
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
                    //TODO same pair?
                    return getHighestPair(my.getBestHand()) < getHighestPair(opponent.getBestHand());
                case 3:
                    //Two Pair
                    //TODO same two pair?
                    if (getHighestPair(my.getBestHand()) == getHighestPair(opponent.getBestHand())) {
                        return getLowestPair(my.getBestHand()) < getLowestPair(opponent.getBestHand());
                    }
                    return getHighestPair(my.getBestHand()) < getHighestPair(opponent.getBestHand());
                case 4:
                    //Three of a kind
                    return getThreeOfAKindValue(my.getBestHand()) < getThreeOfAKindValue(opponent.getBestHand());
                case 5:
                    //Straight
                    //if both have the same straight the highest card counts unless ace is a one
                    //TODO implement if ace is at bottom
                    return getHighestCard(my.getBestHand()) < getHighestCard(opponent.getBestHand());

                case 6:
                    //Flush
                    return getHighestCard(my.getBestHand()) < getHighestCard(opponent.getBestHand());

                case 7:
                    //Full house
                    if (getThreeOfAKindValue(my.getBestHand()) == getThreeOfAKindValue(opponent.getBestHand())) {
                        return getHighestPair(my.getBestHand()) < getHighestPair(opponent.getBestHand());
                    }
                    return getThreeOfAKindValue(my.getBestHand()) < getThreeOfAKindValue(opponent.getBestHand());

                case 8:
                    //Four of a kind
                    //TODO Implement that both players could have the same four of a kind
                    return getFourOfAKindValue(my.getBestHand()) < getFourOfAKindValue(opponent.getBestHand());

                case 9:
                    //Straight flush
                    return getHighestCard(my.getBestHand()) < getHighestCard(opponent.getBestHand());

                case 10:
                    //Royal flush
                    return false;
            }


        } else {
            return my.getBestHand().getPokerHand().getOrderValue() < opponent.getBestHand().getPokerHand().getOrderValue();
        }

        //Failsafe
        return true;

    }

    private int getFourOfAKindValue(Hand hand) {
        List<Card> sortedHand = sortHand(hand.getCards());
        return sortedHand.get(1).getRank().getOrderValue();
    }

    private int getThreeOfAKindValue(Hand hand) {
        List<Card> sortedHand = sortHand(hand.getCards());
        for (int i = 0; i < 3; i++) {
            if (sortedHand.get(i).getRank().getOrderValue() == sortedHand.get(i + 1).getRank().getOrderValue() &&
                    sortedHand.get(i).getRank().getOrderValue() == sortedHand.get(i + 2).getRank().getOrderValue()) {
                //check if there is three of a kind
                if (i < 2 && sortedHand.get(i).getRank().getOrderValue() != sortedHand.get(i + 3).getRank().getOrderValue()) {
                    return sortedHand.get(i).getRank().getOrderValue();
                } else if (i == 2) {
                    return sortedHand.get(i).getRank().getOrderValue();
                }
            }
        }
        //Failsafe
        return 0;
    }

    /**
     * Returns the lowest pair in the hand and 0 if no pair is found. Three of a kind returns 0.
     *
     * @param hand a five-card hand
     * @return rank of the lowest pair
     */
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

    /**
     * Returns the highest pair in the hand and 0 if no pair is found. Three of a kind returns 0.
     *
     * @param hand a five-card hand
     * @return rank of the highest pair
     */
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
     * @param cards a five card hand
     * @return a new list sorted with the card with the highest rank first
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
