package server.gui;

import common.WAMProtocol;

import java.io.*;
import java.net.Socket;

/**WAMPlayer represents a player in the game
 * @author Kevin Augustine
 * @author Noah Alvard
 */
public class WAMPlayer extends Thread implements WAMProtocol , Closeable{
    private Socket client;
    private BufferedReader reader;
    private PrintWriter writer;
    private int id;
    private int points;
    private boolean run;
    private WAMGame game;


    /**
     * constructor
     * @param socket the socket this player owns
     * @param id the idof this player
     * @param game the game this player is playing in
     * @throws IOException
     */
    public WAMPlayer(Socket socket, int id, WAMGame game) throws IOException{
        client = socket;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(socket.getOutputStream(), true);
        this.id = id;
        this.game = game;
        points = 0;
        run = true;
    }

    /**
     * sends the score to the client
     * @param scoreString the score
     */
    public void sendScore(String scoreString){
        writer.println(SCORE + " " + scoreString);
    }

    /**
     * sends that the game has tied to the client
     */
    public void gameTied(){
        writer.println(GAME_TIED);
    }

    /**
     * sends that the client has won the game
     */
    public void gameWon(){
        writer.println(GAME_WON);
    }

    /**
     * sends that the client has lost
     */
    public void gameLost(){
        writer.println(GAME_LOST);
    }

    /**
     * sends the Welcome message to the clients, see WAMProtocol
     * @param rows the rows in the game
     * @param col the col in the game
     * @param players the amount of players
     */
    public void welcomeGame(int rows, int col, int players){
        writer.println(WELCOME + " " + rows + " " + col + " " + players + " " + id);
    }

    /**
     * sends that a mole went up to the clients
     * @param id the id of the mole
     * @param up true if the mole went up, false if it went down
     */
    public void report(int id, boolean up){
        writer.println(up ? MOLE_UP + " " + id : MOLE_DOWN + " " + id);
    }

    /**
     * helper method to make code look nicer
     * @return if the input stream is ready to be read
     */
    public boolean ready()  {
        try {
            return reader.ready();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * helper method to make code look nicer
     * @return the string that is read
     */
    public String readline()  {
        try {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * setter method for points
     * @param change_in_points the change in points
     */
    public void changePoints(int change_in_points){
        points += change_in_points;
    }

    /**
     * getter method for points
     * @return this players points
     */
    public int getPoints() {
        return points;
    }

    /**
     * stops game from running when time is up
     */
    public void stopRunning(){
        run = false;
    }

    /**
     * closes all the ports/networking stufff
     */
    @Override
    public void close() {
        try {
            reader.close();
            writer.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * receives clients messages and interfaces with the game
     */
    @Override
    public void run() {
        while (run){
            if (ready()) {
                String nextCommand = readline();
                String[] commandPees = nextCommand.trim().split("\\s+");
                //idk what else it could be, but if it's not WHACK, we dont' care
                if(commandPees[0].equals(WHACK))
                {
                    //whack is like gui in that if you say it a bunch it stops sounding like a real word
                    //plus if you say it a bunch you sound like a duck
                    //oh, also this sends the received mole id and player id to whack()
                    game.whack(Integer.parseInt(commandPees[1]), this);
                }else break;
            }
        }
    }
}
