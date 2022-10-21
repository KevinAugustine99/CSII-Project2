package server.gui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The server's GUI, runs everything. breaks everything.
 * @author Kevin Augustine
 * @author Noah Alvard
 */
public class WAMServerGUI extends Application implements common.WAMProtocol {

    private static Text waitingOnPLayers;
    private static Stage stage;
    private WAMServer host;
    private Text[] scoreBoard;

    public WAMServerGUI()
    {
    }

    /**
     * sets up the serversocket
     * fills the board with moles
     * initializes points[]
     * for each player args[3] creates an instance of a WAMServer
     * then runs it. each server then runs and waits for a client
     * once a server has a client, it ++counter, and waits();
     * once the last client is connected, counter will equal args[3]
     * and the last server will notify everyone that it's time to go
     * <p>
     * *see run() for this part*
     * <p>
     * once the servers are done, they'll each --counter. Once counter
     * is 0 main will continue
     * then the scoring process will begin and the winners/losers/tied players
     * will be determined. the messages are sent out, and then the servers are closed
     *
     * @param args the program arguments
     */
    public static void main(String[] args)
    {
        Application.launch(args);
        System.out.println("starting server");

    }

    /**
     * starts the parameter input screen
     * @param s
     */
    public void start(Stage s)
    {
        stage = s;
        String error = " ";
        List<String> args2 = getParameters().getRaw();
        String[] args = args2.toArray(new String[0]);
        if(args.length == 5)
        {
            int port = Integer.parseInt(args[0]);
            int rows = Integer.parseInt(args[1]);
            int cols = Integer.parseInt(args[2]);
            int players = Integer.parseInt(args[3]);
            int time = Integer.parseInt(args[4]);
            if (Integer.parseInt(args[3]) < 1) {
                error = "Minimum number of players is 1.";
            } else if ((Integer.parseInt(args[1]) < 1) || (Integer.parseInt(args[2]) < 1)) {
                error = "Mole board must have at least one row and at least one column";
            }
            else
            {
                error = "";
                waitOnPlayers(port, rows, cols, players, time);
            }
        }
        if(!error.equals(""))
        {
            startMenu(error);
        }

    }

    /**
     * the parameter entry method
     * @param error the error message
     */
    public void startMenu(String error)
    {

        VBox vb = new VBox();

        Text title = new Text("Whack-A-Mole\nServer");
        title.setTextAlignment(TextAlignment.CENTER);
        title.setFont(Font.font("Helvetica", 48));
        vb.getChildren().add(title);
        vb.setAlignment(Pos.CENTER);


        Text errorMessage = new Text(error);
        errorMessage.setTextAlignment(TextAlignment.CENTER);
        vb.getChildren().add(errorMessage);

        Text[] labels = new Text[5];
        TextField[] tfs = new TextField[5];

        EventHandler dumbledore = event -> {
            boolean doit = false;
            if(event.getEventType().equals(KeyEvent.KEY_PRESSED))
            {
                KeyEvent ke = (KeyEvent) event;
                if(ke.getCode() == KeyCode.ENTER)
                {
                    doit = true;
                }
            }
            else if(event.getEventType().equals(MouseEvent.MOUSE_CLICKED))
            {
                doit = true;
            }
            if(doit)
            {
                //these try/catch statements prevent an empty submitted field from causing problems
                //this combined with the only digits thing we set up ensures good input
                int port =0;
                try{port = Integer.parseInt(tfs[0].getText());}
                catch(NumberFormatException ignored){}

                int rows = 0;
                try{rows = Integer.parseInt(tfs[1].getText());}
                catch(NumberFormatException ignored){}

                int cols = 0;
                try{cols = Integer.parseInt(tfs[2].getText());}
                catch(NumberFormatException ignored){}

                int players = 0;
                try{players = Integer.parseInt(tfs[3].getText());}
                catch(NumberFormatException ignored){}

                int time = 0;
                try{time = Integer.parseInt(tfs[4].getText());}
                catch(NumberFormatException ignored){}

                String errorMess = "";
                if(!(port>1023) || !(port < 65535))
                {
                    errorMess = "Port must be between 1024 and 65535";
                }
                else if(rows < 1 || cols < 1)
                {
                    errorMess = "Minimum of 1 row and 1 column";
                }
                else if(players<1)
                {
                    errorMess = "Minimum of 1 player";
                }
                else if(time<1)
                {
                    errorMess = "Time must be at least one second";
                }
                if(errorMess.equals(""))
                {
                    waitOnPlayers(port, rows, cols, players, time);
                }
                else
                {
                    errorMessage.setText(errorMess);
                }
            }
        };

        GridPane gp = new GridPane();
        gp.setAlignment(Pos.CENTER);
        vb.getChildren().add(gp);
        gp.addColumn(0);
        gp.addColumn(1);
        gp.getColumnConstraints().add(new ColumnConstraints(70));
        gp.getColumnConstraints().add(new ColumnConstraints(130));
        String[] fields = {"Port:", "Rows:", "Columns:", "Players:", "Time:"};
        for(int i = 0; i < 5; i++)
        {
            final int usableI = i; //god this is stupid
            gp.addRow(i);
            labels[i] = new Text(fields[i]);
            tfs[i] = new TextField("");
            tfs[i].textProperty().addListener((observableValue, old, knew) ->
            {if(!knew.matches("[0-9]*") && !knew.equals("")){tfs[usableI].setText(old);}});
            tfs[i].setAlignment(Pos.CENTER_RIGHT);
            gp.add(labels[i], 0, i);
            gp.add(tfs[i], 1, i);
            tfs[i].addEventFilter(KeyEvent.KEY_PRESSED, dumbledore);
        }

        Button submit = new Button("Start");
        submit.addEventFilter(MouseEvent.MOUSE_CLICKED, dumbledore);

        vb.getChildren().add(submit);
        stage.setResizable(false);
        stage.setMinWidth(350);
        stage.setScene(new Scene(vb));
        stage.show();
    }

    /**
     * sets up the stuff for the screen for waiting on players
     * @param port the port number
     * @param rows the rows
     * @param cols the cols
     * @param players the number of players
     * @param time the game time
     */
    private void waitOnPlayers(int port, int rows, int cols, int players, int time)
    {
        host = new WAMServer(port, rows, cols, players, time, this);
        host.start();
        GridPane gp = new GridPane();
        gp.addColumn(0);
        gp.addColumn(1);
        Text t1 = new Text("Server Started\nwaiting on players: ");
        t1.setFont(Font.font("Verdana", 36));
        waitingOnPLayers = new Text(players+"");
        waitingOnPLayers.setFont(Font.font("Verdana", 72));
        gp.add(t1, 0, 0);
        gp.add(waitingOnPLayers, 1, 0);
        stage.setScene(new Scene(gp));
        if(!stage.isShowing())
        {
            stage.show();
        }
    }

    /**
     * ticks down the waiting for player screen
     */
    protected void playerJoined()
    {
        waitingOnPLayers.setText((Integer.parseInt(waitingOnPLayers.getText())-1)+"");
    }

    /**
     * the stuff to set up the graphics that play when the game is running
     * @param time the game time
     * @param players the number of players
     */
    protected void gameRunning(int time, int players)
    {

        GridPane gp = new GridPane();
        gp.addColumn(0);
        gp.addColumn(1);
        gp.addRow(0);
        VBox scoreBox = new VBox();
        Text timeLeft = new Text(time+"");
        timeLeft.setFont(Font.font("Verdana", 72));
        Timer t = new Timer(timeLeft, time);
        gp.add(timeLeft, 0, 0);
        gp.add(scoreBox, 1, 0);
        gp.getColumnConstraints().add(new ColumnConstraints(200));
        gp.getColumnConstraints().add(new ColumnConstraints(400));
        scoreBoard = new Text[players+1];
        scoreBoard[0] = new Text("Scores:");
        scoreBoard[0].setFont(Font.font("Comic Sans MS", 24));
        for(int i = 0; i < players; i++) {
            scoreBoard[i+1] = new Text();
            scoreBoard[i+1].setFont(Font.font("Comic Sans MS", 24));
        }
        scoreBox.getChildren().addAll(scoreBoard);
        StringBuilder bsScores = new StringBuilder();
        for(int i = 0; i < players; i++)
        {
            bsScores.append("0 ");
        }
        updateScores(bsScores.toString());
        t.start();
        stage.setScene(new Scene(gp));
        stage.setResizable(true);


    }

    /**
     * updates the scores in the scoreboard
     * @param scoreString the score string
     */
    protected void updateScores(String scoreString)
    {
        ArrayList<PointPair> scoreAL;
        String[] scores = scoreString.trim().split("\\s+");
        scoreAL = sortScores(scores);
        for(int i = 0; i < host.getPlayers(); i++)
        {
            scoreBoard[i+1].setText((i+1) + ". Player " + scoreAL.get(i).getPlayer()
                    + " - " + scoreAL.get(i).getPoints());
        }
    }


    /**
     * parses the score message into an arraylist of PointPairs
     * @param comms the score message
     * @return that array list of pointpairs
     */
    private ArrayList<PointPair> sortScores(String[] comms)
    {
        ArrayList<PointPair> scores = new ArrayList<>();
        for(int i = 0; i < comms.length; i++)
        {
            scores.add(new PointPair(i, Integer.parseInt(comms[i])));
        }
        Collections.sort(scores);
        return scores;
    }

    /**
     * allows for the game timer
     */
    private class Timer extends Thread
    {
        private Text text;
        private int time;
        private Timer(Text text, int time)
        {
            this.text = text;
            this.time = time;
        }
        public void run()
        {
            for(int i = time; i >0; i--)
            {
                text.setText(String.valueOf(i));
                try
                {
                    sleep(1000);
                }
                catch(InterruptedException e){
                    System.err.println(e.getMessage());
                }
            }
        }
    }
}