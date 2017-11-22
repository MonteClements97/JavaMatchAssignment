package cs21120.assignment2017.solution;
import cs21120.assignment2017.IManager;
import cs21120.assignment2017.Match;
import cs21120.assignment2017.NoNextMatchException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.LinkedList;
import java.util.Queue;


/**
 * <p>
 * Generates queues and stacks to keep track of the current state of the competition.
 * If no matches are left then will return the winner.
 * </p><p>
 * Includes a functional undo and redo operation. However, if you undo and redo the last match,
 * CompetitionManager.java will just exit without displaying the winner.
 * </p><p>
 * DoubleElimMoc17 also contains an inner class that stores the queues.
 * </p><p>
 * I have decided to create 4 queues, 2 stacks and an additional internal class to complete this assignment. 2 queues
 * will store the states of the winners and losers queue during the current match, so when the winners and losers queues
 * are eventually pulled from, this will not affect how the queues will look inside the undo and redo stacks. Both stacks
 * will store an instance of the internal class I have created, which will hold both the winners and losers queue. This
 * is where the 4 queues becomes beneficial, as I am able to pull from the winners and losers queue, whilst also being able to store
 * how the queues should have looked before anything was removed from them. At the start of every match, the winners queue and
 * the state of the winners queue will be exactly the same, likewise the losers queue.
 * </p><p>
 * The main problem I had was trying to build my code around code that was already provided, as I had my own
 * ideas of how I could have implemented a solution, but did not work with how the provided code was working.
 * Of course, this easy to resolve as I created the necessary classes to run the program so I was able to grasp
 * a firm understanding of how everything worked, then started building my methods as they were needed by the
 * provided code. This then meant my methods I was creating fit the provided code perfectly, as everything was modeled
 * to deal with everything it needed.
 * </p><p>
 * Both the redo and undo algorithms were almost identical, except for the stacks that needed to dealt with,
 * so to avoid code duplication, I have created a private method that both undo and redo calls when they are called,
 * passing in the stacks that need to be modified in a different order based on whether it was undo or redo that
 * was called.
 * </p><p>
 * A private copy queue method was created that would create a new queue
 * object and then return that queue object. It will copy all the contents of the queue object passed into it. This
 * was created so I could keep track of how the queue should look like the program started pulling from queues, as the
 * undo and redo stacks needed to know what the queues looking like before the program pulled from the queue.
 * </p><p>
 * I believe I would get 15% out of 20% for the main class structure. I believe I have maintained a rather decent structure
 * when building my class and all methods, variables and classes have been defined properly. My class also does implement the Imanager
 * interface. I believe I have done everything this section has asked me to do. However, the one doubt I have is that
 * the variable names could have been a lot better, as some of them don't really tell you what they are holding, for
 * example, stateOfWinnersQueue.
 * </p><p>
 * I believe I would get 27% out of 30% for competition logic. I have run test after test on the logic for my system, and
 * haven't found a case where the system trips itself. All the players are placed on the winners queue at the start, matches
 * are taken from the winners queue if it is bigger than the losers queue, else taken from the losers queue,
 * losers are discarded if they lose from the lose queue. This goes on until there are
 * only 2 teams left, one in the winners queue and one in the losers queue, in which they will then play the final game. The
 * overall winner is then decided on this match. I believe I have done everything that has been asked in specification, so
 * I do expect a high mark for this section.
 * </p><p>
 * Likewise competition logic, I believe I have implemented Undo and Redo very effectively. I believe I have done everything
 * that has been asked of me on this section. Undo does not work on the first match, and redo does not work unless there
 * has been an undo performed before hand. The redo stack is cleared if the program resumes after an undo. However, in
 * competition manager, if you undo and then redo on the final match, the program will just exit. This is because after it
 * redoes the last match, it then checks if there is a next match. As it is, lastMatch will be set to true, which when it
 * hits the while hasNextMatch(), it will return as false and then exit the program.
 * </p>
 */
public class DoubleElimMoc17 implements IManager {
    /**stores the state the queue is in before players are removed*/
    private Queue<String> stateOfWinnersQueue;

    /**stores the state the queue is in before players are removed*/
    private Queue<String> stateOfLosersQueue;

    /**The queue the program will remove players from*/
    private Queue<String> winnersQueue;

    /**The queue the program will remove players from*/
    private Queue<String> losersQueue;

    /**The teams playing in the current match*/
    private Match currentPlayers;

    /**The winner of the overall competition*/
    private String winner;

    /**Where the players were taken from. True if winners queue, anything else is false*/
    private Boolean whatQueue;

    /**Used to check if it is the last match to be played*/
    private Boolean lastMatch;

    /**Stores the previous states of the queues*/
    private Stack<StateOfQueues> undoStack;

    /**Stores the state of the queues as the program is undoing*/
    private Stack<StateOfQueues> redoStack;

    /**
     * <p>
     * A class that will be store the state of the queues and then be thrown into either the undo or redo stack
     * depending on what is required.
     * </p><p>
     * I decided to implement a class inside the DoubleElimMoc17 class as I felt I needed a way of storing both the
     * winners queue and the losers queue in the stack. To save myself from creating 4 stacks overall, 2 for undo and
     * 2 for redo, I decided to combine both winners and losers queue into a class with it's own getters, which then
     * will be thrown into a stack. This way I will only need 2 stacks instead of 4 stacks.
     * </p>
     */
    private class StateOfQueues{
        /**Stores the current state of the winners queue*/
        private Queue<String> currentWinnersQueue;
        /**Stores the current state of the losers queue*/
        private Queue<String> currentLosersQueue;

        /**
         * The current state of the queues are passed into this constructor.
         * @param currentWinnersQueue the state of the winners queue
         * @param currentLosersQueue the state of the losers queue
         */
        private StateOfQueues(Queue<String> currentWinnersQueue, Queue<String> currentLosersQueue){
            this.currentLosersQueue = currentLosersQueue;
            this.currentWinnersQueue = currentWinnersQueue;
        }

        /**
         * Gets the winners queue
         * @return the winners queue
         */
        private Queue<String> getWinnersQueue(){return currentWinnersQueue;
        }

        /**
         * Gets the losers queue
         * @return the losers queue
         */
        private Queue<String> getLosersQueue(){
            return currentLosersQueue;
        }

    }


    /**
     * The first method that is called when the program will run. This will initialise lastMatch to false, as well as
     * creating the first instances of the undoStack, redoStack, winnersQueue and losersQueue objects. A for each loop
     * is used to add all the players from the players ArrayList into the winnersQueue queue.
     * @param players the players or teams
     */
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

    /**
     * Simply checks whether the lastMatch boolean is true or false.
     * @return False if lastMatch is true. True if lastMatch is false
     */
    @Override
    public boolean hasNextMatch() {
        return !lastMatch;
    }

    /**<p>
     * Checks whether there is a next match, and will throw NoNextMatchException if there is not one.
     * </p><p>
     * Stores the state of both the winners queue and losers queue before removing any players from the queues. A new
     * queue object is given to both stateOfWinnersQueue and stateOfLosersQueue by calling the copyQueue method.
     * </p><ul>
     * <li>If the winners queue is larger than the losers queue, the players will be taken from the winners queue.
     * <li>If the losers queue is greater than or equal to the winners queue, the players will be taken from the losers queue.
     * <li>If both the winners and losers queue only has 1 player, then the players will be pulled from both the winners and losers queue.
     * </ul>
     * <p>
     * whatQueue will keep track of where the players were taken from by being assigned true if it came from the winners queue,
     * and false if from the losers or both queues
     * </p>
     * @return The players that will be playing in the next match based on how large the queues are.
     * @throws NoNextMatchException If there is no next match
     */
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
        } else {
            currentPlayers = new Match(losersQueue.poll(), losersQueue.poll());
            whatQueue = false;
            return currentPlayers;
        }
    }

    /**<p>
     * A match's scores have been entered into the system, therefore we can clear the redo stack as there will now be
     * nothing to redo. The undo stack will now need to store a new copy of the current state of both the winners and losers
     * by creating a new StateOfQueues object and passing both stateOfWinnersQueue and stateOfLosersQueue into it.
     * </p><p>
     * However, a problem I did not notice while doing this is that I was passing in the objects stateOfWinnersQueue and
     * stateOfLosersQueues into this new object, and then modifying the same objects after. What I failed to do was create
     * new objects with my copyQueues method when passing them into StateOfQueues. So to fix this, I simply called the
     * copy queues method when passing both the queues into StateOfQueues, which then made them their own objects which in
     * turn did not get modified as I then started changing stateOfWinnersQueue and stateOfLosersQueue
     * </p><p>
     * Sets the overall winner if both the winners and losers queue are empty, and if whatQueue is false. This should
     * always work as if next match determined it need to pull 2 players from the losers queue, this must mean that there
     * must be at least 1 player in the winners queue, as losers and winners cannot play until the final game.
     * </p><p>
     * If player1 is true, that means player1 won. If the players came from the winners queue, player1 will then be returned
     * to the back of the winners queue, and player2 will be put onto the back of the losers queue. If they came from
     * the losers queue, player1 will be put back to the back of the losers queue, whereas player2 will be eliminated
     * from the competition and will not be placed anywhere. If player2 wins, the direct opposite will happen with how
     * the players are treated.
     * </p>
     * @param player1 true indicates the first player wins, otherwise player 2
     */
    @Override
    public void setMatchWinner(boolean player1) {
        redoStack.clear();
        undoStack.push(new StateOfQueues(copyQueue(stateOfWinnersQueue), copyQueue(stateOfLosersQueue)));
        if (winnersQueue.isEmpty() && losersQueue.isEmpty() && !whatQueue){
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

    /**
     * Just returns the competition winner that is set by setMatchWinner
     * @return The competition winner
     */
    @Override
    public String getWinner() {
        return winner;
    }

    /**
     * <p>
     * Undo took a rather long time to implement. At first I was trying to store the names of the players that needed to
     * be stored in undo and a number that indicated where they came from. I stored these in a class called StateOfMatch.
     * However, as I was implementing this, I decided that just storing the queues as I went along would be easier to implement,
     * so I changed the class name and what it stored, and got to creating my undo method. First thing was to check if you can
     * undo at the point you are on, so a call to canUndo is made. I then decided that if you undo, you cannot possibly be on
     * the last match, so lastMatch will be set to false, for when you undo the last match. I will be pushing the queues into
     * the redo stack and popping the queues from the undo stack.
     * </p>
     */
    @Override
    public void undo() {
        if (canUndo()) {
            lastMatch = false;
            redoOrUndoAlgorithm(redoStack, undoStack);
        }
    }


    /**
     * <p>
     * Redo took a lot longer than undo to implement. At first, both undo and redo had different algorithms. I was trying
     * to store winnersQueue and losersQueue in redo as I believed that I should of been storing what the queues were at that
     * time, rather than the state of the queues at the start of the match. This led to queues being left empty, or just containing
     * information that just did not make sense. As I started redesigning my redo, it became to look more and more like my
     * undo method. So instead of having 2 methods that do the same thing, I decided to combine them into one, then call this
     * method from the redo method, passing in the undo stack as the stack I will be pushing the queues into, and the
     * redo stack as the stack I will be popping the queues from.
     * </p>
     */
    @Override
    public void redo() {
        if (canRedo()){
            redoOrUndoAlgorithm(undoStack, redoStack);
            if (redoStack.empty() && stateOfWinnersQueue.isEmpty() && stateOfLosersQueue.isEmpty()) lastMatch = true;
        }
    }

    /**
     * <p>
     * First we must push the current state of the queues into either the undo or redo stack, which is dependent on
     * where it was called from. We then have to take the queues out from the stack that needs to have the queues taken
     * from. The winnersQueue will then need to be set to what has been popped from the stack by calling the getWinnersQueue
     * method. The same will be done with the losers queue.
     * </p><p>
     * At one point, this is all that was in the algorithm. However, I quickly came to realise that when you undo and then
     * redo, nothing was updating the state of the queues, so after the winnersQueue and losersQueue were set, I also set
     * the states of the queues to a copy of the actual queues. This then allowed me to pass both the JUnit test and
     * run fluently on Competition manager. Without setting the states, I am still able to work undo and redo perfectly
     * on Competition manager, but not able to pass the redo test.
     * </p>
     * @param pushStack If undo, this will be the redoStack. If redo, this will be the undoStack
     * @param popStack If undo, this will be the undoStack. If redo, this will be the redoStack
     */
    private void redoOrUndoAlgorithm(Stack<StateOfQueues> pushStack, Stack<StateOfQueues> popStack){
        pushStack.push(new StateOfQueues(copyQueue(stateOfWinnersQueue), copyQueue(stateOfLosersQueue)));
        StateOfQueues currentQueues = popStack.pop();
        winnersQueue = currentQueues.getWinnersQueue();
        losersQueue = currentQueues.getLosersQueue();
        stateOfLosersQueue = copyQueue(losersQueue);
        stateOfWinnersQueue = copyQueue(winnersQueue);
    }


    /**
     * Simply checks whether the undo stack is empty or not
     * @return True if the stack contains data, false if the stack does not
     */
    @Override
    public boolean canUndo() { return !undoStack.empty(); }

    /**
     * Simply checks whether the redo stack is empty or not
     * @return True if the stack contains data, false if the stack does not
     */
    @Override
    public boolean canRedo() { return !redoStack.empty(); }

    /**
     * Takes a queue object, copies all the contents from the queue object and places it into a new queue object, then
     * returns the new queue object.
     * @param queue
     * @return A new queue object
     */
    private Queue<String> copyQueue(Queue<String> queue){
        Queue<String> newQueue = new LinkedList<>();
        for(String player: queue){
            newQueue.add(player);
        }
        return newQueue;
    }
}



