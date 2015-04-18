package se.cygni.texasholdem.player;

import se.cygni.texasholdem.game.Card;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Test on 2015-04-16.
 */
public class PercentageCalculator {


    private List<Card> privateCards;
    private String preFlopMapSearcher;
    private HashMap<String, Double> preFlopHeadsUp = new HashMap<String, Double>();
    private HashMap<String, Double> preFlopMulti = new HashMap<String, Double>();

    public PercentageCalculator() throws FileNotFoundException {

        Scanner scannerHeadsUp = new Scanner(new FileReader("percentages\\preFlopHeadsUp.txt"));
        Scanner scannerMulti = new Scanner(new FileReader("percentages\\preFlopMulti.txt"));

        while (scannerHeadsUp.hasNext()) {
            String[] columns = scannerHeadsUp.nextLine().split("\t");
            preFlopHeadsUp.put(columns[0], Double.valueOf(columns[1])/100);
        }

        while (scannerMulti.hasNext()) {
            String[] columns = scannerMulti.nextLine().split("\t");
            preFlopMulti.put(columns[0], Double.valueOf(columns[1])/100);
        }
    }

    public void newCards(List<Card> hand) {
        this.privateCards = hand;

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
     * @param nbrOfPlayers players still having a privateCards
     * @return percentage of winning if all players would not fold
     */
    public double getPreFlopPercentage(int nbrOfPlayers){
        if (nbrOfPlayers > 2) {
            return preFlopMulti.get(preFlopMapSearcher);
        } else {
            return preFlopHeadsUp.get(preFlopMapSearcher);
        }
    }
}