package se.cygni.texasholdem.player;

import se.cygni.texasholdem.game.Hand;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by Test on 2015-04-16.
 */
public class PercentageCalculator {


    private Hand hand;
    private String mapSearcher;
    private HashMap<String, Double> preFlopHeadsUp = new HashMap<String, Double>();
    private HashMap<String, Double> preFlopMulti = new HashMap<String, Double>();

    public PercentageCalculator() throws FileNotFoundException {

        Scanner scannerHeadsUp = new Scanner(new FileReader("percentages\\preFlopHeadsUp.txt"));
        Scanner scannerMulti = new Scanner(new FileReader("percentages\\preFlopMulti.txt"));

        while (scannerHeadsUp.hasNext()) {
            String[] columns = scannerHeadsUp.nextLine().split("\t");
            preFlopHeadsUp.put(columns[0], Double.valueOf(columns[1]));
        }

        while (scannerMulti.hasNext()) {
            String[] columns = scannerMulti.nextLine().split("\t");
            preFlopMulti.put(columns[0], Double.valueOf(columns[1]));
        }
    }

    public double newHand(Hand hand, int nbrOfPlayers) {
        this.hand = hand;

        HashMap<String, Double> preFlopPercentages;

        if (nbrOfPlayers > 2) {
            preFlopPercentages = preFlopMulti;
        } else {
            preFlopPercentages = preFlopHeadsUp;
        }


        //Needs to have the biggest value first
        if (hand.getCards().get(0).getRank().getOrderValue() > hand.getCards().get(1).getRank().getOrderValue()) {
            mapSearcher = hand.getCards().get(0).getRank().getName() + hand.getCards().get(1).getRank().getName();
        } else {
            mapSearcher = hand.getCards().get(1).getRank().getName() + hand.getCards().get(0).getRank().getName();
        }
        //Is it the same suit then add "s" to the end of the string, otherwise "o"
        if (hand.getCards().get(0).getSuit().equals(hand.getCards().get(1).getSuit())) {
            mapSearcher = mapSearcher + "s";
        } else {
            mapSearcher = mapSearcher + "o";
        }

        return preFlopPercentages.get(mapSearcher);
    }
}
