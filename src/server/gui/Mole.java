package server.gui;
import java.util.Random;

/**
 * A class to simulate moles. Each mole once activated (going) runs on it's own thread
 * and goes up and down randomly within the bounds set.
 *
 * @author Kevin Augustine
 * @author Noah Alvard
 */
public class Mole extends Thread
{
    private static final int DOWN_MIN = 2000;
    private static final int DOWN_MAX = 10000;
    private static final int UP_MIN = 3000;
    private static final int UP_MAX = 5000;

    private static final Random rand = new Random();

    private int id;
    private boolean up;
    private boolean whacked;
    private boolean going;
    private WAMGame game;


    public Mole(int id, WAMGame wamGame)
    {
        this.id = id;
        going = false;
        up = false;
        whacked = false;
        game = wamGame;
    }

    /**
     * sees if the mole was up when the player hit it.
     * if it is, it sets the mole to whacked so no other player gets credit
     * sets the mole to down
     * reports the hit to the server
     * and returns that it was a good hit(this is what tells the server to give or subtract points)
     * @return if the mole got whacked from this player
     */
    public synchronized boolean gotWhacked()
    {
        boolean worth = false;
        if(up && !whacked)
        {
            whacked = true;
            up = false;
            game.report(id, false);
            worth = true;
        }
        return worth;
    }


    /**
     * Stops the mole. stop() was taken and final, so we have halt.
     */
    public void halt()
    {
        this.going = false;
    }

    /**
     * starts the mole going up and down on random (but bounded) intervals
     */
    public void run()
    {
        going = true;
        while(going)
        {
            if(up)
            {
                try
                {
                    sleep(nextInt(UP_MIN, UP_MAX));
                }
                catch(Exception e)
                {
                    System.err.println(e.getMessage());
                }
                this.up = false;
                game.report(id, false);
            }
            else
            {
                whacked = false;
                try
                {
                    sleep(nextInt(DOWN_MIN, DOWN_MAX));
                }
                catch(Exception e)
                {
                    System.err.println(e.getMessage());
                }
                this.up = true;
                game.report(id, true);
            }
        }
    }

    /**
     *
     * @param min the min result
     * @param max the max result
     * @return a random int inclusively between min and max
     */
    private int nextInt(int min, int max)
    {
        int val = rand.nextInt(max-min+1);
        return val+min;
    }

    /**
     * duh
     * @return string representation of the mole
     */
    public String toString()
    {
        return "Mole #" + id + " " + up;
    }
}
