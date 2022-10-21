package client.gui;
/**WAMGUI is the view for the client's gui in the whack a mole game
 *
 * @author Kevin Augustine
 * @author Noah Alvard
 *
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Random;

/**
 * GUI is a funny word. Say GUI like 10 times and it starts to sound like not a word
 */
public class WAMGUI extends Application implements Observer<WAMModel>
{
    //it's of note that i absolutely stole that image of a mole from some website online
    //i made the green and the hole, but the actual depiction of the animal i did not make.
    private final static String HIT_SOUND_MP3 = "sounds/hit.mp3";
    private final static String WIN_SOUND_MP3 = "sounds/win.mp3";

    private static Random rand = new Random();
    private static AudioClip hit;
    private static AudioClip win;
    private final static String MOLE_UP_PNG =  "images/mole_up.png";
    private final static String MOLE_DOWN_PNG = "images/mole_down.png";
    private final static String ICON_PNG = "images/icon.png";
    private final static String INTRO_IMAGE_PNG = "images/intro.png";
    //i also ripped these songs from youtube. This is probably covered under fair use...right? No commercial value made
    private final static String[] MUSICS = {"sounds/moon.mp3", "sounds/e1m1.mp3", "sounds/c418.mp3", "sounds/xcom.mp3"};
    private static AudioClip song;

    private Image imUp;
    private Image imDown;


    private final static double WIDTH = 600;
    private final static double HEIGHT = 650;
    private final static double SCORE_WIDTH = 230;

    private final static int TITLE_FONT_SIZE = 44;
    private final static int SCORE_FONT_SIZE = 24;


    private WAMClient client;
    private Button[] bees;
    private Stage stage;
    private Text playerState;
    private Text[] scoreBoard;

    private boolean gameInPro = false;
    private WAMModel model;
    /**
     * BEGIN
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        if(args.length == 2)
        {
            Application.launch(args);
        }
        else
        {
            System.err.println("Usage:\njava WAMGUI.java <Host Address> <Port>");
        }
    }

    /** javafx GUI init()
     * makes a WAMClient as client
     * adds <this> as an observer of that client
     * and then starts the WAMClient up to connect to the server/listen to messages
     * it then waits until the client receives the Welcome message
     * and makes the board. Once that begins, we can make the GUI
     * we can't start before that otherwise we won't know how many buttons to make
     */
    @Override
    public void init() throws Exception
    {

        super.init();
        hit = new AudioClip(getClass().getResource(HIT_SOUND_MP3).toURI().toString());
        win = new AudioClip(getClass().getResource(WIN_SOUND_MP3).toURI().toString());

        ArrayList<String> pm = new ArrayList<>(this.getParameters().getRaw());//i didn't know you could do this, but that's cool

        //setting up the whole MVC system
        model = new WAMModel();
        model.addObserver(this);
        try {
            client = new WAMClient(pm.get(0), Integer.parseInt(pm.get(1)), model);
        }catch (IOException e){
            System.err.println("Usage:\njava WAMGUI.java <Host Address> <Port>");
        }
        client.start();

        //waiting until the client(client) receives the Welcome message and knows how many rows and columns to put

    }

    /**
     * Sets up the GUI
     * At the top there's a title "Whack-A-Mole"
     * then below that there's a grid of buttons, rows*cols large
     * each with a picture of a mole hole on them.
     * when you click on one, the event handler calls client's whack()
     * then shows the stage to begin the game
     * @param s the initial stage passed by launch
     */
    @Override
    public void start(Stage s) //just sets up the intro screen
    {

        stage=s;//making it a static variable so update() can access it (this might be bad form?)

        ImageView intr = new ImageView(new Image((getClass().getResourceAsStream(INTRO_IMAGE_PNG))));
        Pane p = new Pane();
        p.getChildren().add(intr);
        stage.setScene(new Scene(p));
        stage.show();

    }

    /**
     * pulls up the actul game board screen so the game can be played
     * @param rows the number of rows
     * @param cols the number of cols
     * @param players the number of players
     * @param id the id of this player
     */
    protected void startTheGame(int rows, int cols, int players, int id) //puts the main game screen up
    {
        if(!gameInPro) {

            model.allocate(rows, cols, players, id);
            String choice = MUSICS[rand.nextInt(4)]; //number between 0 and 3 god i hate how it's bounded
            try {
                song = new AudioClip(getClass().getResource(choice).toURI().toString());
            } catch (URISyntaxException e) {
                System.err.println(e.toString());
            }

            HBox hb = new HBox();
            GridPane gp = new GridPane();
            VBox vb = new VBox();

            playerState = new Text("Playing");
            playerState.setFont(Font.font("Comic Sans MS", TITLE_FONT_SIZE));
            vb.getChildren().add(0, playerState);
            scoreBoard = new Text[model.getPlayers()];

            for (int i = 0; i < model.getPlayers(); i++) {

                scoreBoard[i] = new Text((i + 1) + ". Player " + model.getScores().get(i).getPlayer()
                        + " - " + model.getScores().get(i).getPoints());
                scoreBoard[i].setFont(Font.font("Comic Sans MS", SCORE_FONT_SIZE));
                if (model.getId() == model.getScores().get(i).getPlayer()) {
                    scoreBoard[i].setFill(Color.RED);
                } else {
                    scoreBoard[i].setFill(Color.BLACK);
                }
            }

            vb.getChildren().addAll(scoreBoard);

            hb.getChildren().add(gp);
            hb.getChildren().add(vb);
            Scene scene = new Scene(hb);
            stage.setMaxWidth(WIDTH + SCORE_WIDTH);
            stage.setMaxHeight(HEIGHT);

            Text title = new Text("WHACK A MOLE");
            title.setFont(Font.font("Comic Sans MS", TITLE_FONT_SIZE));

            GridPane.setHalignment(title, HPos.CENTER);

            gp.setPrefSize(WIDTH, HEIGHT);
            vb.setPrefSize(SCORE_WIDTH, HEIGHT);

            //in order to base the size of the buttons on the size of the stage on the size of the screen, the stage
            //must be put in action, and then everything is sized around that. more complicated yes,
            //but it makes other things simpler
            stage.getIcons().add(new Image(getClass().getResourceAsStream(ICON_PNG))); //add icon to stage
            stage.setScene(scene);

            //event handler for the buttons
            EventHandler<MouseEvent> gandalf = e -> {
                if (gameInPro)//prevents playing once the game is over
                {
                    hit.play();
                    client.whack(Integer.parseInt(((Button) e.getSource()).getId()));
                }
            };

            //setting up gridpane
            for (int i = 0; i < model.getRows() + 1; i++) {
                gp.addRow(i);
            }
            for (int i = 0; i < model.getCols(); i++) {
                gp.addColumn(i);
            }

            //setting up the title
            stage.setTitle("Whack-A-Mole");
            stage.setResizable(false);
            gp.getChildren().add(title);
            GridPane.setColumnSpan(title, model.getCols());
            GridPane.setRowIndex(title, 0);

            //Creation of the bees by Michelangelo------------------------------------------------

            this.bees = new Button[model.getRows() * model.getCols()];

            imUp = new Image(
                    getClass().getResourceAsStream(MOLE_UP_PNG),
                    WIDTH / model.getCols(),
                    (HEIGHT - title.getLayoutBounds().getHeight()) / model.getRows(),
                    false, true);

            imDown = new Image(getClass().getResourceAsStream(MOLE_DOWN_PNG),
                    WIDTH / model.getCols(),
                    (HEIGHT - title.getLayoutBounds().getHeight()) / model.getRows(),
                    false, true);

            for (int i = 0; i < bees.length; i++) {
                //size/position setup
                bees[i] = new Button("");
                gp.getChildren().add(bees[i]);
                GridPane.setRowIndex(bees[i], (i % model.getRows()) + 1);//i don't know what's wrong with this, but warning?
                GridPane.setColumnIndex(bees[i], (i / model.getRows()));
                bees[i].setPrefSize(WIDTH / model.getCols(), (HEIGHT - title.getLayoutBounds().getHeight()) / model.getRows());
                bees[i].setPadding(Insets.EMPTY);

                //image setup
                bees[i].setGraphic(new ImageView(imDown));

                //giving the bees a name and wizard
                bees[i].addEventFilter(MouseEvent.MOUSE_CLICKED, gandalf);
                bees[i].setId("" + i);
            }
            gameInPro = true;
            song.play(0.6, 0, 1, 0, 50);
        }
    }

    /**
     * this is what gets called when the observer is notified of a change in the client
     * the observer sends the client and the id of the mole that changed
     * this then does one of two things
     * if it's still running (status=0) then it updates the graphic of the button[moleID] to reflect it's "upness"
     * if it's not running (status!=0) then it determines if you won, and shows the scoreboard
     * scoreboard is a title of Winner/Loser/Tied
     * and then a list of scores with place number, player number, and score
     *
     * @param model the board
     * @param moleID the mole that got hit and needs to be updated
     */
    public void update(WAMModel model, int moleID)
    {
            {Platform.runLater(new Runnable()
            {
                final int mole = moleID;
                @Override
                public void run()
                {
                    if(model.getStatus() == WAMModel.Status.WELCOME && model.getBoard() != null) //game running, and the board has been initialized
                    {
                        startTheGame(model.getRows(), model.getCols(), model.getPlayers(), model.getId());
                        if(model.getBoard()[mole])
                        {

                            bees[mole].setGraphic(new ImageView(imUp));
                            //set the mole/button to up
                        }
                        else
                        {
                            bees[mole].setGraphic(new ImageView(imDown));
                            //set the mole/button to down
                        }
                        for(int i = 0; i < model.getPlayers(); i++)
                        {
                            scoreBoard[i].setText((i+1) + ". Player " + model.getScores().get(i).getPlayer()
                                    + " - " + model.getScores().get(i).getPoints());
                            if(model.getId()==model.getScores().get(i).getPlayer())
                            {
                                scoreBoard[i].setFill(Color.RED);
                            }
                            else
                            {
                                scoreBoard[i].setFill(Color.BLACK);
                            }
                        }

                    }
                    else
                    {
                        if(model.getStatus() == WAMModel.Status.GAME_WON)
                        {
                            gameInPro = false;
                            song.stop();
                            win.play();
                            playerState.setText("WINNER!");
                        }
                        else if(model.getStatus() == WAMModel.Status.GAME_LOST)
                        {
                            gameInPro = false;
                            song.stop();
                            playerState.setText("LOSER!");
                        }
                        else if(model.getStatus() == WAMModel.Status.GAME_TIED)
                        {
                            gameInPro = false;
                            song.stop();
                            playerState.setText("TIED!");
                        }
                    }
                }
            });
        }
    }
}