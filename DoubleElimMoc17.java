package cs21120.assignment2017.solution;
import cs21120.assignment2017.IManager;
import cs21120.assignment2017.Match;
import cs21120.assignment2017.NoNextMatchException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.LinkedList;
import java.util.Queue;

public class DoubleElimMoc17 implements IManager {
    private Queue<String> winnersQueue;
    private Queue<String> losersQueue;
    private Match currentPlayers;
    private String winner;
    private Boolean whatQueue;
    private Stack<StateOfMatch> undoStack;

    public class StateOfMatch{
        private Match currentMatch;
        private int whereYouCameFrom;

        private StateOfMatch(Match currentMatch, int whereYouCameFrom){
            this.currentMatch = currentMatch;
            this.whereYouCameFrom = whereYouCameFrom;
        }

        Match getCurrentMatch(){
            return currentMatch;
        }

        int getWhereYouCameFrom(){
            return whereYouCameFrom;
        }

    }



    @Override
    public void setPlayers(ArrayList<String> players) {
        this.undoStack = new Stack<>();
        this.winnersQueue = new LinkedList<>();
        this.losersQueue = new LinkedList<>();
        for (String player: players){
            winnersQueue.add(player);
        }

    }

    @Override
    public boolean hasNextMatch() {
        return (winnersQueue.size() > 1 ||
                losersQueue.size() > 1 ||
                (winnersQueue.size() == 1 && losersQueue.size() == 1)
                || currentPlayers != null);
    }

    @Override
    public Match nextMatch() throws NoNextMatchException {
        if(!hasNextMatch()) {
            throw new NoNextMatchException("No next match");
        }

        if (winnersQueue.size() > losersQueue.size()){
            currentPlayers = new Match(winnersQueue.poll(), winnersQueue.poll());
            whatQueue = true;
            return currentPlayers;
        } else if (winnersQueue.size() == 1 && losersQueue.size() == 1){
            currentPlayers = new Match(winnersQueue.poll(), losersQueue.poll());
            whatQueue = false;
            return currentPlayers;
        }
        else {
            currentPlayers = new Match(losersQueue.poll(), losersQueue.poll());
            whatQueue = false;
            return currentPlayers;
        }
    }

    @Override
    public void setMatchWinner(boolean player1) {
        if (winnersQueue.size() == 0 && losersQueue.size() == 0 && !whatQueue){
            if(player1){
                winner = currentPlayers.getPlayer1();
            } else {
                winner = currentPlayers.getPlayer2();
            }
            undoStack.push(new StateOfMatch(currentPlayers, 3));
            currentPlayers = null;
        }
        else {
            if(whatQueue) {
                if (player1) {
                    winnersQueue.add(currentPlayers.getPlayer1());
                    losersQueue.add(currentPlayers.getPlayer2());
                } else {
                    winnersQueue.add(currentPlayers.getPlayer2());
                    losersQueue.add(currentPlayers.getPlayer1());
                }
                undoStack.push(new StateOfMatch(currentPlayers, 1));
                currentPlayers = null;
            } else {
                if (player1) {
                    losersQueue.add(currentPlayers.getPlayer1());

                } else {
                    losersQueue.add(currentPlayers.getPlayer2());
                }
                undoStack.push(new StateOfMatch(currentPlayers, 2));
                currentPlayers = null;
            }
        }
    }

    @Override
    public String getWinner() {
        return winner;
    }


    @Override
    public void undo() {
        if (canUndo()){
            Queue<String> tempWinnersQueue = new LinkedList<>();
            Queue<String> tempLosersQueue = new LinkedList<>();
            StateOfMatch currentUndo = undoStack.pop();
            if (currentUndo.getWhereYouCameFrom() == 1){
                tempWinnersQueue.add(currentUndo.getCurrentMatch().getPlayer1());
                tempWinnersQueue.add(currentUndo.getCurrentMatch().getPlayer2());
                while(winnersQueue.size() > 1){
                    tempWinnersQueue.add(winnersQueue.poll());
                }
                while(losersQueue.size() > 1){
                    tempLosersQueue.add(losersQueue.poll());
                }
                winnersQueue = tempWinnersQueue;
                losersQueue = tempLosersQueue;
            } else if (currentUndo.getWhereYouCameFrom() == 2){
                tempLosersQueue.add(currentUndo.getCurrentMatch().getPlayer1());
                tempLosersQueue.add(currentUndo.getCurrentMatch().getPlayer2());
                while(losersQueue.size() > 1){
                    tempLosersQueue.add(losersQueue.poll());
                }
                losersQueue = tempLosersQueue;
            } else {
                winnersQueue.add(currentUndo.getCurrentMatch().getPlayer1());
                losersQueue.add(currentUndo.getCurrentMatch().getPlayer2());
            }
        }
    }

    @Override
    public void redo() {

    }

    @Override
    public boolean canUndo() {
        return !undoStack.empty();
    }

    @Override
    public boolean canRedo() {
        return false;
    }
}



