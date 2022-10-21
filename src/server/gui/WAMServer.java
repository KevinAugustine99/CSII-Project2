package server.gui;

import javafx.application.Platform;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

/**
 * The server, runs everything. breaks everything.
 * @author Kevin Augustine
 * @author Noah Alvard
 */
public class WAMServer extends Thread implements common.WAMProtocol {

    private ServerSocket server;
    private int rows;
    private int cols;
    private int players;
    private int time;
    private WAMGame game;
    private WAMServerGUI gui;

    /**
     * starts everything
     * @param args the program arguments
     */
    public static void main(String[] args)
    {
        int port=0;
        int rows=0;
        int cols=0;
        int players=0;
        int time=0;
        if(args.length == 5)
        {
            try
            {
                port = Integer.parseInt(args[0]);
                rows = Integer.parseInt(args[1]);
                cols = Integer.parseInt(args[2]);
                players = Integer.parseInt(args[3]);
                time = Integer.parseInt(args[4]);
            }
            catch(NumberFormatException e)
            {
                System.err.println("Please only input numbers");
            }
            if (players < 1)
            {
                System.err.println("Minimum number of players is 1.");
            } else if (cols < 1 || rows < 1)
            {
                System.err.println("Mole board must have at least one row and at least one column");
            }
            else
            {
                WAMServer p = new WAMServer(port, rows, cols, players, time, null);
                p.start();
            }
        }else{
            System.err.println("Usage:\njava WAMServer.java <Port> <Rows> <Columns> <Players> <Time>");
        }
    }

    /**
     * constructor
     * @param port the port number
     * @param rows number of rows
     * @param cols number of cols
     * @param players number of players
     * @param time game time
     * @param gui the gui
     */
    public WAMServer(int port, int rows, int cols, int players, int time, WAMServerGUI gui)
    {
        this.gui = gui;
        this.rows = rows;
        this.cols = cols;
        this.players = players;
        this.time = time;
        try
        {
            this.server = new ServerSocket(port);

        }
        catch(IOException e){
            System.err.println(e.getMessage());
        }
    }

    /**
     * runs the game
     */
    public void run()
    {
        List<WAMPlayer> set_players = new ArrayList<>();
        game = new WAMGame(rows, cols, time, this);


        System.out.println("waiting for players");
        try {
            for (int i = 0; i < players; i++) {
                set_players.add(new WAMPlayer(server.accept(), i, game));
                if(gui!= null)
                {
                    Platform.runLater(() -> gui.playerJoined());
                }
                System.out.println("player " + i + " connected");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("starting game");
        game.setPlayers(set_players);
        game.start();
        if(gui!=null)
        {
            Platform.runLater(() -> gui.gameRunning(time, players));
        }
    }


    /**
     * @return the number of players
     */
    public int getPlayers()
    {
        return players;
    }

    /**
     * tells the gui to update the scoreboard
     * @param scoreString the scoreString
     */
    public void updateGuiScoreboard(String scoreString)
    {
        if(gui!= null){
            Platform.runLater(() -> gui.updateScores(scoreString));
        }
    }
}
