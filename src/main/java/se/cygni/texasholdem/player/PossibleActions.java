package se.cygni.texasholdem.player;

import se.cygni.texasholdem.game.Action;

import java.util.List;

/**
 * Created by Daniel on 2015-04-17.
 */
public class PossibleActions {

    private boolean call = false;
    private boolean check = false;
    private boolean raise = false;
    private boolean fold = false;
    private boolean allIn = false;

    private long callAmount = -1;
    private long raiseAmount = -1;
    private long allInAmount = -1;

    public PossibleActions(List<Action> possibleActions) {
        for (Action action : possibleActions) {
            switch (action.getActionType()) {
                case CALL:
                    call = true;
                    callAmount = action.getAmount();
                    break;
                case CHECK:
                    check = true;
                    break;
                case FOLD:
                    fold = true;
                    break;
                case RAISE:
                    raise = true;
                    raiseAmount = action.getAmount();
                    break;
                case ALL_IN:
                    allIn = true;
                    allInAmount = action.getAmount();
                default:
                    break;
            }
        }
    }

    public boolean canICall() {
        return call;
    }

    public boolean canICheck() {
        return check;
    }

    public boolean canIRaise() {
        return raise;
    }

    public boolean canIFold() {
        return fold;
    }

    public boolean canIGoAllIn() {
        return allIn;
    }

    public long getCallAmount() {
        return callAmount;
    }

    public long getRaiseAmount() {
        return raiseAmount;
    }

    public long getAllInAmount() {
        return allInAmount;
    }
}
