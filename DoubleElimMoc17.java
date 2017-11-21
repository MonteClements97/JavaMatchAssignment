package cs21120.assignment2017.solution;
import cs21120.assignment2017.IManager;
import cs21120.assignment2017.Match;
import cs21120.assignment2017.NoNextMatchException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.LinkedList;
import java.util.Queue;


/**
 * Generates queues and stacks to keep track of the current state of the competition.
 *
 *
 *
 */
public class DoubleElimMoc17 implements IManager {
    private Queue<String> stateOfWinnersQueue; //stores the state the queues are in after poll
    private Queue<String> stateOfLosersQueue;  //^^
    private Queue<String> winnersQueue; //The queue to remove from
    private Queue<String> losersQueue;  //^^
    private Match currentPlayers;
    private String winner;
    private Boolean whatQueue;
    private Boolean lastMatch;
    private Stack<StateOfQueues> undoStack;
    private Stack<StateOfQueues> redoStack;

    private class StateOfQueues{
        private Queue<String> currentWinnersQueue;
        private Queue<String> currentLosersQueue;

        private StateOfQueues(Queue<String> currentWinnersQueue, Queue<String> currentLosersQueue){
            this.currentLosersQueue = currentLosersQueue;
            this.currentWinnersQueue = currentWinnersQueue;
        }

        private Queue<String> getWinnersQueue(){return currentWinnersQueue;
        }

        private Queue<String> getLosersQueue(){
            return currentLosersQueue;
        }

    }



    @Override
    public void setPlayers(ArrayList<String> players) {
        lastMatch = false;
        undoStack = new Stack<>();
        redoStack = new Stack<>();
        winnersQueue = new LinkedList<>();
        losersQueue = new LinkedList<>();

        for (String player: players){
            winnersQueue.add(player);
        }

    }

    @Override
    public boolean hasNextMatch() {
        return (winnersQueue.size() > 1 ||
                losersQueue.size() > 1 ||
                (winnersQueue.size() == 1 && losersQueue.size() == 1)
                || !lastMatch);
    }

    @Override
    public Match nextMatch() throws NoNextMatchException {
        if(!hasNextMatch()) {
            throw new NoNextMatchException("No next match");
        }
        stateOfWinnersQueue = copyQueue(winnersQueue);
        stateOfLosersQueue = copyQueue(losersQueue);
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
        redoStack.clear();
        undoStack.push(new StateOfQueues(copyQueue(stateOfWinnersQueue), copyQueue(stateOfLosersQueue)));
        if (winnersQueue.size() == 0 && losersQueue.size() == 0 && !whatQueue){
            lastMatch = true;
            if(player1){
                winner = currentPlayers.getPlayer1();
                stateOfWinnersQueue.clear();
                stateOfLosersQueue.clear();
            } else {
                winner = currentPlayers.getPlayer2();
                stateOfWinnersQueue.clear();
                stateOfLosersQueue.clear();
            }
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
                currentPlayers = null;
            } else {
                if (player1) {
                    losersQueue.add(currentPlayers.getPlayer1());

                } else {
                    losersQueue.add(currentPlayers.getPlayer2());
                }
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
        if (canUndo()) {
            lastMatch = false;
            redoOrUndoAlgorithm(redoStack, undoStack);
        }
    }

    private void redoOrUndoAlgorithm(Stack<StateOfQueues> pushStack, Stack<StateOfQueues> popStack){
        pushStack.push(new StateOfQueues(copyQueue(stateOfWinnersQueue), copyQueue(stateOfLosersQueue)));
        StateOfQueues currentQueues = popStack.pop();
        winnersQueue = currentQueues.getWinnersQueue();
        losersQueue = currentQueues.getLosersQueue();
        stateOfLosersQueue = copyQueue(losersQueue);
        stateOfWinnersQueue = copyQueue(winnersQueue);

    }

    @Override
    public void redo() {
        if (canRedo()){
            redoOrUndoAlgorithm(undoStack, redoStack);
            if (redoStack.empty()) lastMatch = true;
        }
    }

    @Override
    public boolean canUndo() {
        return !undoStack.empty();
    }

    @Override
    public boolean canRedo() {
        if(redoStack.empty()) return false;
        return true;
    }

    private Queue<String> copyQueue(Queue<String> queue){
        Queue<String> newQueue = new LinkedList<>();
        for(String player: queue){
            newQueue.add(player);
        }
        return newQueue;
    }
}



