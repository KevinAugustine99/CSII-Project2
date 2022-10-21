package client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**WAModel is the model for the MVC for the client
 * @author Kevin Augustine
 * @author Noah Alvard
 */
public class WAMModel
{

    public enum Status{
        GAME_WON, GAME_LOST, GAME_TIED, WELCOME, WAITING
    }

    private boolean[] board;
    private int rows;
    private int cols;
    private int players;
    private int id;
    private ArrayList<PointPair> pps;
    private List<Observer<WAMModel>> observers;
    private Status status;


    /**
     * Constructor
     */
    public WAMModel()
    {
        observers = new ArrayList<>();
        status = Status.WAITING;
    }

    /**
     * adds an observer to be notified
     * @param observer the observer to be notified
     */
    protected void addObserver(Observer<WAMModel> observer){ observers.add(observer);}

    /**
     * notifies all observers that mole <moleID> has changed
     * @param moleId the id of the mole changed
     */
    private void notifyObservers(int moleId){
        observers.forEach(observer -> observer.update(this, moleId));
    }

    protected void setScore(String[] comms)
    {
        this.pps = sortScores(comms);
        notifyObservers(0);
    }

    /**
     * called when MOLE_UP is received
     * sets the status of that mole on the board
     * @param moleID the id of the mole
     */
    protected void moleUp(int moleID)
    {
        board[moleID] = true;
        notifyObservers(moleID);
    }

    /**
     * called when MOLE_DOWN is received
     * sets the status of that mole on the board
     * @param moleID the id of the mole
     */
    protected void moleDown(int moleID)
    {
        board[moleID] = false;
        notifyObservers(moleID);
    }


    /**
     * initializes the array of PointPairs with player ids (0 to players) and points at 0
     * Makes the scoreboard look good from tick 1
     * @param players the number of players in the game
     * @return the the array list filled in with initialized pointpairs
     */
    private ArrayList<PointPair> initPPS(int players)
    {
        ArrayList<PointPair> pps = new ArrayList<>();
        for(int i = 0; i < players; i++)
        {
            pps.add(new PointPair(i, 0));
        }
        return pps;
    }


    /**
     * parses the score message into an arraylist of PointPairs
     * @param comms the score message
     * @return that array list of pointpairs
     */
    private ArrayList<PointPair> sortScores(String[] comms)
    {
        ArrayList<PointPair> scores = new ArrayList<>();
        for(int i = 1; i < comms.length; i++)
        {
            scores.add(new PointPair(i-1, Integer.parseInt(comms[i])));
        }
        Collections.sort(scores);
        return scores;
    }
    public boolean[] getBoard()
    {
        return board;
    }
    public int getRows()
    {
        return rows;
    }
    protected int getCols()
    {
        return cols;
    }
    protected int getId() {
        return id;
    }
    protected int getPlayers() {
        return players;
    }
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        notifyObservers(0);
    }

    protected ArrayList<PointPair> getScores() {
        return pps;
    }

    /**
     * accepts the information about the game once it's given by the Welcome message
     * @param rows the number of rows
     * @param cols the number of cols
     * @param players the number of players
     * @param id the id of this player
     */
    protected void allocate(int rows, int cols, int players, int id)
    {
        this.rows = rows;
        this.cols = cols;
        this.players = players;
        this.id = id;
        this.board = new boolean[rows*cols];
        this.pps = initPPS(players);
        status = Status.WELCOME;
        notifyObservers(0);
    }
}
