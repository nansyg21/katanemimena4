package Score4;


// Connect4ClientConnection Class
// Connect4ClientConnection.java

// All code graciously developed by Greg Turner. You have the right
// to reuse this code however you choose. Thanks Greg!

// Imports

import java.io.*;
import java.net.*;
import java.applet.*;

public class Connect4ClientConnection extends SocketAction {
	public static final int PORTNUM = 1234;
	public static final int ERROR = -1;
	public static final int PLSWAIT = -2;
	public static final int YOURTURN = -3;
	public static final int THEIRTURN = -4;
	public static final int THEYWON = -5;
	public  static final int THEYQUIT = -6;
	public  static final int THEYTIED = -7;
  public static final int GAMEOVER = -8;

  public Connect4ClientConnection(Applet a) throws IOException {
    super(new Socket(a.getCodeBase().getHost(), PORTNUM));
  }

  public int getTheirMove() {
    // Make sure we're still connected
    if (!isConnected()) 
      throw new NullPointerException("Attempted to read closed socket!");

    try {
      String s = receive();
      System.out.println("Received: " + s);
      if (s == null)
        return GAMEOVER;
      s = s.trim();
      try {
        return (new Integer(s)).intValue();
      }
      catch (NumberFormatException e) {
        // It was probably a status report error
        return getStatus(s);
      }
    }
    catch (IOException e) {
      System.out.println("I/O Error: " + e);
      System.exit(1);
      return 0;
    }
  }

  private int getStatus(String s) {
    s = s.trim();
    if (s.startsWith("PLSWAIT"))
      return PLSWAIT;
    if (s.startsWith("THEIRTURN"))
      return THEIRTURN;
    if (s.startsWith("YOURTURN"))
      return YOURTURN;
    if (s.startsWith("THEYWON"))
      return THEYWON;
    if (s.startsWith("THEYQUIT"))
      return THEYQUIT;
    if (s.startsWith("THEYTIED"))
      return THEYTIED;
    if (s.startsWith("GAMEOVER"))
      return GAMEOVER;

    // Something has gone horribly wrong!
    System.out.println("received invalid status from server: " + s);
    return ERROR;
  }

  public void sendMove(int col) {
    String s = (new Integer(col)).toString();
    send(s);
  }

  public void sendIQUIT() {
    send("IQUIT");
  }

  public void sendIWON() {
    send("IWON");
  }

  public void sendITIED() {
    send("ITIED");
  }
}
