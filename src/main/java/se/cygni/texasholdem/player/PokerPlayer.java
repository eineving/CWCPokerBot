package se.cygni.texasholdem.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.cygni.texasholdem.client.CurrentPlayState;
import se.cygni.texasholdem.client.PlayerClient;
import se.cygni.texasholdem.communication.message.event.*;
import se.cygni.texasholdem.communication.message.request.ActionRequest;
import se.cygni.texasholdem.game.*;
import se.cygni.texasholdem.game.definitions.PlayState;
import se.cygni.texasholdem.game.definitions.PokerHand;
import se.cygni.texasholdem.game.util.PokerHandUtil;

import java.io.FileNotFoundException;
import java.util.Formatter;

/**
 * This is an example Poker bot player, you can use it as
 * a starting point when creating your own.
 * <p/>
 * If you choose to create your own class don't forget that
 * it must implement the interface Player
 *
 * @see Player
 * <p/>
 * Javadocs for common utilities and classes used may be
 * found here:
 * http://poker.cygni.se/mavensite/texas-holdem-common/apidocs/index.html
 * <p/>
 * You can inspect the games you bot has played here:
 * http://poker.cygni.se/showgame
 */
public class PokerPlayer implements Player {

    private static Logger log = LoggerFactory
            .getLogger(PokerPlayer.class);

    private final String serverHost;
    private final int serverPort;
    private final PlayerClient playerClient;
    private PercentageCalculator pCalculator;

    /**
     * Default constructor for a Java Poker Bot.
     *
     * @param serverHost IP or hostname to the poker server
     * @param serverPort port at which the poker server listens
     */
    public PokerPlayer(String serverHost, int serverPort) throws FileNotFoundException {
        this.serverHost = serverHost;
        this.serverPort = serverPort;

        // Initialize the player client
        playerClient = new PlayerClient(this, serverHost, serverPort);
        pCalculator = new PercentageCalculator();
    }

    public void playATrainingGame() throws Exception {
        playerClient.connect();
        playerClient.registerForPlay(Room.TRAINING);
    }

    /**
     * The main method to start your bot.
     *
     * @param args strings
     */
    public static void main(String... args) {
        try {
            PokerPlayer bot = new PokerPlayer("poker.cygni.se", 4711);

            bot.playATrainingGame();


        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * The name you choose must be unique, if another connected bot has
     * the same name your bot will be denied connection.
     *
     * @return The name under which this bot will be known
     */
    @Override
    public String getName() {
        return "DanielEineving";
    }

    /**
     * This is where you supply your bot with your special mojo!
     * <p/>
     * The ActionRequest contains a list of all the possible actions
     * your bot can perform.
     *
     * @param request The list of Actions that the bot may perform.
     * @return The action the bot wants to perform.
     * @see ActionRequest
     * <p/>
     * Given the current situation you need to choose the best
     * action. It is not allowed to change any values in the
     * ActionRequest. The amount you may RAISE or CALL is already
     * predermined by the poker server.
     * <p/>
     * If an invalid Action is returned the server will ask two
     * more times. Failure to comply (i.e. returning an incorrect
     * or non valid Action) will result in a forced FOLD for the
     * current Game Round.
     * @see Action
     */
    @Override
    public Action actionRequired(ActionRequest request) {
        Action response = getBestAction(request);
        log.info("I'm going to {} {}",
                response.getActionType(),
                response.getAmount() > 0 ? "with " + response.getAmount() : "");

        return response;
    }

    /**
     * A helper method that returns this bots idea of the best action.
     * Note! This is just an example, you need to add your own smartness
     * to win.
     *
     * @param request what actions can be done
     * @return the action to do
     */
    private Action getBestAction(ActionRequest request) {


        // The current play state is accessible through this class. It
        // keeps track of basic events and other players.
        CurrentPlayState playState = playerClient.getCurrentPlayState();

        PossibleActions possibleActions = new PossibleActions(request.getPossibleActions());
        //PreFlop
        if (playState.getCommunityCards().isEmpty()) {
            return preFlopAction(request, possibleActions);

        } else if (playState.getCommunityCards().size() == 3) {
            //Flop
            return flopAction(request, possibleActions);

        } else if (playState.getCommunityCards().size() == 4) {
            //Turn
            //TODO Implement

        } else if (playState.getCommunityCards().size() == 5) {
            //River
            //TODO Implement

        } else {

            //failsafe
            log.debug("Not right amount of community cards");
            return new Action(ActionType.FOLD, 0);
        }





        /* ==============================Cygni's method below =====================*/

        Action callAction = null;
        Action checkAction = null;
        Action raiseAction = null;
        Action foldAction = null;
        Action allInAction = null;

        for (final Action action : request.getPossibleActions()) {
            switch (action.getActionType()) {
                case CALL:
                    callAction = action;
                    break;
                case CHECK:
                    checkAction = action;
                    break;
                case FOLD:
                    foldAction = action;
                    break;
                case RAISE:
                    raiseAction = action;
                    break;
                case ALL_IN:
                    allInAction = action;
                default:
                    break;
            }
        }

        // The current BigBlind
        long currentBB = playState.getBigBlind();

        // PokerHandUtil is a hand classifier that returns the best hand given
        // the current community cards and your cards.
        PokerHandUtil pokerHandUtil = new PokerHandUtil(playState.getCommunityCards(), playState.getMyCards());
        Hand myBestHand = pokerHandUtil.getBestHand();
        PokerHand myBestPokerHand = myBestHand.getPokerHand();

        // Let's go ALL IN if hand is better than or equal to THREE_OF_A_KIND
        if (allInAction != null && isHandBetterThan(myBestPokerHand, PokerHand.TWO_PAIRS)) {
            return allInAction;
        }

        // Otherwise, be more careful CHECK if possible.
        if (checkAction != null) {
            return checkAction;
        }

        // Okay, we have either CALL or RAISE left
        long callAmount = callAction == null ? -1 : callAction.getAmount();
        long raiseAmount = raiseAction == null ? -1 : raiseAction.getAmount();

        // Only call if ONE_PAIR or better
        if (isHandBetterThan(myBestPokerHand, PokerHand.ONE_PAIR) && callAction != null) {
            return callAction;
        }

        // Do I have something better than TWO_PAIR and can RAISE?
        if (isHandBetterThan(myBestPokerHand, PokerHand.TWO_PAIRS) && raiseAction != null) {
            return raiseAction;
        }

        // I'm small blind and we're in PRE_FLOP, might just as well call
        if (playState.amISmallBlindPlayer() &&
                playState.getCurrentPlayState() == PlayState.PRE_FLOP &&
                callAction != null) {
            return callAction;
        }

        // failsafe
        return foldAction;
    }

    private Action flopAction(ActionRequest request, PossibleActions possibleActions) {
        //TODO Implement
        return null;
    }

    private Action preFlopAction(ActionRequest request, PossibleActions possibleActions) {
        log.debug("I am now doing preFlopCalculations");

        CurrentPlayState playState = playerClient.getCurrentPlayState();

        if (possibleActions.canICheck()) {
            //Maybe raise to see what the others will do
            if (1 - pCalculator.getPreFlopPercentage(playState.getNumberOfPlayers()) / 2 < Math.random() && possibleActions.canIRaise()) {
                return new Action(ActionType.RAISE, possibleActions.getRaiseAmount());
            } else {
                return new Action(ActionType.CHECK, 0);
            }
        }
        //Can only all in
        if (!possibleActions.canICall() && possibleActions.canIGoAllIn()) {
            if (goAllInPreFlop()) {
                return new Action(ActionType.ALL_IN, possibleActions.getAllInAmount());
            } else {
                return fold();
            }
        }
        if (possibleActions.canICall()) {
            log.debug("CALL calculations: " + possibleActions.getCallAmount() + "/" + playState.getMyCurrentChipAmount());
            if (playState.getNumberOfPlayers() > 2) {

                //p(hand)>0,2*(bet/stack)^(1/4)
                if (pCalculator.getPreFlopPercentage(playState.getNumberOfPlayers()) > 0.2 *
                        Math.pow(possibleActions.getCallAmount() / playState.getMyCurrentChipAmount(), 1 / 8)) {
                    if (Math.random() > 0.80 && possibleActions.canIRaise()) {
                        return new Action(ActionType.RAISE, possibleActions.getRaiseAmount());
                    } else {
                        return new Action(ActionType.CALL, possibleActions.getCallAmount());
                    }
                } else {
                    return fold();
                }
            } else {
                if (pCalculator.getPreFlopPercentage(playState.getNumberOfPlayers()) > 0.6 *
                        Math.pow(possibleActions.getCallAmount() / playState.getMyCurrentChipAmount(), 1 / 8)) {
                    if (Math.random() > 0.75 && possibleActions.canIRaise()) {
                        return new Action(ActionType.RAISE, possibleActions.getRaiseAmount());
                    } else {
                        return new Action(ActionType.CALL, possibleActions.getCallAmount());
                    }
                } else {
                    return fold();
                }
            }
        }
        //Failsafe
        return fold();

    }

    private Action fold() {
        return new Action(ActionType.FOLD, 0);
    }

    private boolean goAllInPreFlop() {
        //TODO Implement
        //Is stack smaller than big blind?
        /*
        CurrentPlayState playState = playerClient.getCurrentPlayState();
        if(playState.getBigBlind()>playState.getMyCurrentChipAmount()){
            return pCalculator.getPreFlopPercentage(2) > .50;
        } else {
            return pCalculator.getPreFlopPercentage(2) > (1-playState.getBigBlind()/playState.getMyCurrentChipAmount());
        }*/
        return false;
    }


    /**
     * Compares two pokerhands.
     *
     * @param myPokerHand
     * @param otherPokerHand
     * @return TRUE if myPokerHand is valued higher than otherPokerHand
     */
    private boolean isHandBetterThan(PokerHand myPokerHand, PokerHand otherPokerHand) {
        //TODO Remove
        return myPokerHand.getOrderValue() > otherPokerHand.getOrderValue();
    }

    /**
     * **********************************************************************
     * <p/>
     * Event methods
     * <p/>
     * These methods tells the bot what is happening around the Poker Table.
     * The methods must be implemented but it is not mandatory to act on the
     * information provided.
     * <p/>
     * The helper class CurrentPlayState provides most of the book keeping
     * needed to keep track of the total picture around the table.
     *
     * @see CurrentPlayState
     * <p/>
     * ***********************************************************************
     */

    @Override
    public void onPlayIsStarted(final PlayIsStartedEvent event) {
        log.debug("Play is started");
    }

    @Override
    public void onTableChangedStateEvent(TableChangedStateEvent event) {

        log.debug("Table changed state: {}", event.getState());
    }

    @Override
    public void onYouHaveBeenDealtACard(final YouHaveBeenDealtACardEvent event) {

        log.debug("I, {}, got a card: {}", getName(), event.getCard());
        if (playerClient.getCurrentPlayState().getMyCards().size() == 2) {
            pCalculator.newCards(playerClient.getCurrentPlayState().getMyCards());
            log.debug("I have now received two cards");
        }

    }

    @Override
    public void onCommunityHasBeenDealtACard(
            final CommunityHasBeenDealtACardEvent event) {
        pCalculator.newCommunityCard(event.getCard());
        log.debug("Community got a card: {}", event.getCard());
    }

    @Override
    public void onPlayerBetBigBlind(PlayerBetBigBlindEvent event) {

        log.debug("{} placed big blind with amount {}", event.getPlayer().getName(), event.getBigBlind());
    }

    @Override
    public void onPlayerBetSmallBlind(PlayerBetSmallBlindEvent event) {

        log.debug("{} placed small blind with amount {}", event.getPlayer().getName(), event.getSmallBlind());
    }

    @Override
    public void onPlayerFolded(final PlayerFoldedEvent event) {

        log.debug("{} folded after putting {} in the pot", event.getPlayer().getName(), event.getInvestmentInPot());
    }

    @Override
    public void onPlayerForcedFolded(PlayerForcedFoldedEvent event) {

        log.debug("NOT GOOD! {} was forced to fold after putting {} in the pot because exceeding the time limit", event.getPlayer().getName(), event.getInvestmentInPot());
    }

    @Override
    public void onPlayerCalled(final PlayerCalledEvent event) {

        log.debug("{} called with amount {}", event.getPlayer().getName(), event.getCallBet());
    }

    @Override
    public void onPlayerRaised(final PlayerRaisedEvent event) {

        log.debug("{} raised with bet {}", event.getPlayer().getName(), event.getRaiseBet());
    }

    @Override
    public void onTableIsDone(TableIsDoneEvent event) {

        log.debug("Table is done, I'm leaving the table with ${}", playerClient.getCurrentPlayState().getMyCurrentChipAmount());
        log.info("Ending poker session, the last game may be viewed at: http://{}/showgame/table/{}", serverHost, playerClient.getCurrentPlayState().getTableId());
    }

    @Override
    public void onPlayerWentAllIn(final PlayerWentAllInEvent event) {

        log.debug("{} went all in with amount {}", event.getPlayer().getName(), event.getAllInAmount());
    }

    @Override
    public void onPlayerChecked(final PlayerCheckedEvent event) {

        log.debug("{} checked", event.getPlayer().getName());
    }

    @Override
    public void onYouWonAmount(final YouWonAmountEvent event) {

        log.debug("I, {}, won: {}", getName(), event.getWonAmount());
    }

    @Override
    public void onShowDown(final ShowDownEvent event) {

        if (!log.isInfoEnabled()) {
            return;
        }

        final StringBuilder sb = new StringBuilder();
        final Formatter formatter = new Formatter(sb);

        sb.append("ShowDown:\n");

        for (final PlayerShowDown psd : event.getPlayersShowDown()) {
            formatter.format("%-13s won: %6s  hand: %-15s ",
                    psd.getPlayer().getName(),
                    psd.getHand().isFolded() ? "Fold" : psd.getWonAmount(),
                    psd.getHand().getPokerHand().getName());

            sb.append(" cards: | ");
            for (final Card card : psd.getHand().getCards()) {
                formatter.format("%-13s | ", card);
            }
            sb.append("\n");
        }

        log.info(sb.toString());
    }

    @Override
    public void onPlayerQuit(final PlayerQuitEvent event) {

        log.debug("Player {} has quit", event.getPlayer());
    }

    @Override
    public void connectionToGameServerLost() {

        log.debug("Lost connection to game server, exiting");
        System.exit(0);
    }

    @Override
    public void connectionToGameServerEstablished() {

        log.debug("Connection to game server established");
    }

    @Override
    public void serverIsShuttingDown(final ServerIsShuttingDownEvent event) {
        log.debug("Server is shutting down");
    }


}
