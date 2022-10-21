package client.gui;

import common.WAMProtocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;



/**
 * this is the controller of the MVC. It has all
 * the networking message stuff.
 *
 * @author Kevin Augustine
 * @author Noah Alvard
 */
public class WAMClient extends Thread implements WAMProtocol
{

    private int id;
    private WAMModel model;
    private Socket sock;
    private BufferedReader input;
    private PrintWriter output;


    /**
     * constructor
     * @param host the ip or domain name of the server
     * @param port the port the server uses
     */
    public WAMClient(String host, int port, WAMModel model)  throws  IOException{
        this.model = model;
        sock = new Socket(host, port);
        input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        output = new PrintWriter(sock.getOutputStream(), true);
    }

    /**
     * this is what's called by a buttons event handler
     * sends the whack signal to the server
     * the mole is set down here as well as when the server
     * sends back the message saying it's down
     * @param moleID the id of the mole that was whacked
     */
    protected void whack(int moleID)
    {
        output.println(WHACK + " " + moleID + " " + id);
        model.moleDown(moleID);
    }
    /**
     * the main client thread
     * firstly, listens for messages from the server, then acts on them
     * that's actually really it...
     */
    public void run()
    {
        System.out.println("Connected to Server");
        boolean going = true;
        while(going)
        {
            try
            {

                String comm = input.readLine();
                if(comm == null)
                {
                    throw new IOException("null avoider lol");
                }
                String[] comms = comm.split("\\s+");

                switch (comms[0])
                {
                    case WELCOME:
                        System.out.println("Welcome! " + comms[1] + " " + comms[2] + " " + comms[3] + " " + comms[4]);
                        int rows = Integer.parseInt(comms[1]);
                        int cols = Integer.parseInt(comms[2]);
                        int players = Integer.parseInt(comms[3]);
                        int id = Integer.parseInt(comms[4]);
                        this.id = id;
                        model.allocate(rows,cols,players,id);
                        System.out.println("THIS IS MY ID " + this.id);
                        break;
                    case MOLE_UP:
                        model.moleUp(Integer.parseInt(comms[1]));
                        break;
                    case MOLE_DOWN:
                        model.moleDown(Integer.parseInt(comms[1]));
                        break;
                    case SCORE:
                        model.setScore(comms);
                        break;
                    case GAME_WON:
                        System.out.println("GAME WON");
                        model.setStatus(WAMModel.Status.GAME_WON);
                        going = false;
                        break;
                    case GAME_LOST:
                        System.out.println("GAME LOST");
                        model.setStatus(WAMModel.Status.GAME_LOST);
                        going = false;
                        break;
                    case GAME_TIED:
                        System.out.println("GAME TIED");
                        model.setStatus(WAMModel.Status.GAME_TIED);
                        going = false;
                        break;
                    case ERROR:
                        throw new IOException("Server sent error. Shutting down.");
                    default:
                        throw new IOException("default case in switch hit. Server violated WAMProtocol");
                }
            }
            catch(IOException e)
            {
                System.out.println("Server Closed");
                System.out.println(e.getMessage());
                try
                {
                    input.close();
                    output.close();
                    sock.close();
                    break;
                }
                catch(IOException k)
                {
                    System.out.println(k.getMessage());
                }
            }


        }
    }
}

