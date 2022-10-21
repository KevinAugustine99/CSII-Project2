package server.gui;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class represents the game from the server side
 *
 * @author Kevin Augustine
 * @author Noah Alvard
 */
public class WAMGame extends Thread{

    private Mole[] board; //the board of moles
    private int rows; //the amount of rows
    private int cols; //the amount of columns
    private int time; //time in seconds
    private List<WAMPlayer> players; //the list of players
    private WAMServer server;

    /**The constructor of the WAMGame
     *
     * @param rows: the amount of rows
     * @param cols: the amount of columns
     * @param time: time in seconds
     */
    public WAMGame(int rows, int cols, int time, WAMServer server){
        this.server = server;
        this.rows = rows;
        this.cols = cols;
        this.time = time;
        board = new Mole[rows*cols]; //init the board full of moles
        Arrays.setAll(board, i -> new Mole(i, this));
    }


    /**
     * run runs the WAMGame
     */
    @Override
    public void run() {
        try {
            Arrays.stream(board).forEach(Mole::start); //start all of the moles
            players.forEach(player -> {
                player.start(); //start all of the players
                player.welcomeGame(rows, cols, players.size()); //send welcome message to all of the players
            });

            sleep(time*1000); //sleep throughout the game

            players.forEach(WAMPlayer::stopRunning);  //stop all of the players
            Arrays.stream(board).forEach(Mole::halt); //stop all of the moles

            String scoreString = getScoreString(); //get the string of scores
            players.forEach(wamPlayer -> wamPlayer.sendScore(scoreString)); //send score to all players

            int top = Integer.MIN_VALUE; //set the top to min Integer
            List<WAMPlayer> winners = new ArrayList<>(); //make new array list of winners
            List<WAMPlayer> losers = new ArrayList<>(); //make new array list of losers
            for (WAMPlayer player : players) {
                if (top < player.getPoints()) { //if players points is greater than top
                    if (winners.size() != 0){ //if winners is not empty
                        losers.addAll(winners); //add all of the people in the winners to losers
                        winners.clear(); //clear winners
                    }
                    winners.add(player); //add player to winner
                    top = player.getPoints(); //set top this persons amount of points
                } else if (top == player.getPoints()) winners.add(player); //if tied then add player to winner list
                else losers.add(player); //if not winning then add to losers list
            }

            boolean one_winner = winners.size() == 1; // true if only one winner
            for(WAMPlayer player: winners){ //for each player in winners
                if(one_winner) player.gameWon(); //send game won if one winner
                else player.gameTied(); //send game tied if not one winner
            }

            losers.forEach(WAMPlayer::gameLost); //for each loser send game lost message

            players.forEach(WAMPlayer::close); //close each player

        } catch (InterruptedException e ) {
            e.printStackTrace();
        }
    }

    /**
     * what each instance does to indicate a mole whack
     * mole whacks are received through the network
     * if the mole was actually up, the player gets two points
     * if the mole wasn't actually up, the player loses a point.
     * @param moleID the id(index) of the mole
     * @param player the id of the player
     */
    public synchronized void whack(int moleID, WAMPlayer player)
    {
        boolean point = board[moleID].gotWhacked(); //gotWhacked returns true if the mole was
        // up, false if the mole was down
        player.changePoints(point ? 2 : -1);
        String scoreString = getScoreString();
        players.forEach(wamPlayer -> wamPlayer.sendScore(scoreString));
    }

    /**
     * gets a string representation of the scores of each player, in player id order
     * @return the score string of the scores of all of the platers
     */
    public String getScoreString(){
        String scoreString = players.stream().map(wamplayer -> wamplayer.getPoints() + " ").collect(Collectors.joining());
        scoreString.trim();
        server.updateGuiScoreboard(scoreString);
        return scoreString;
    }

    /**
     * a method for the moles to tell the server that they are up or down
     */
    public void report(int id, boolean up)
    {
        players.forEach(player -> player.report(id, up));
    }


    /**
     * a setter method
     * @param players the list of players to be set
     */
    public void setPlayers(List<WAMPlayer> players) {
        this.players = players;
    }
}
