package client.gui;
/**
 * this is just a little something to hold two ints. Yes, i could have just done int[][], but i forgot
 * and now it's written sooo
 *
 * @author Kevin Augustine
 * @author Noah Alvard
 */
public class PointPair implements Comparable<PointPair>
{

    int player;
    int points;

    /**
     * Constructor
     * @param player the player id
     * @param points the number of points
     */
    PointPair(int player, int points)
    {
        this.player = player;
        this.points = points;
    }

    /**
     * Compare to
     * @param a the other one
     * @return a value indicating which one is bigger, >0, this is bigger, <0, a is bigger, 0, they are equal
     */
    @Override
    public int compareTo(PointPair a)
    {
        return a.points-this.points; //yes this is backwards from what you might think, but it works, no touchy
    }

    /**
     * getter method
     * @return the player
     */
    public int getPlayer()
    {
        return this.player;
    }

    /**
     * getter method
     * @return the points
     */
    public int getPoints()
    {
        return this.points;
    }

}
