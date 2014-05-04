package Score4Server;


// Connect4Daemon Class
// Connect4Daemon.java

// All code graciously developed by Greg Turner. You have the right
// to reuse this code however you choose. Thanks Greg!

// Imports
import java.net.*;
import java.io.*;

public class Connect4Daemon extends Thread {
  public static final int PORTNUM = 1234;
  public static int paired=0;
  private ServerSocket    port;
  private Connect4Player  playerWaiting = null;
  private Game            thisGame = null;
  public Game waitingGame=null;

  public Connect4Daemon() {
    super("Connect4Daemon");
    // Try to grab the port
    try {
      port = new ServerSocket(PORTNUM);
    }
    catch (IOException e) {
      System.out.println("Couldn't access port " + PORTNUM + ": " + e);
      System.exit(1);
    }
  }
  
public void run() {
    // Even though we are functioning as a daemon in regard to the
    // game, we don't want to be declared as a daemon thread here
    // because we don't want the runtime system to kill us off.
    Socket clientSocket;
    while (true) {
      if (port == null) {
        System.out.println("Sorry, the port disappeared.");
        System.exit(1);
      }
      try {
        clientSocket = port.accept();
        new Connect4Player(this, clientSocket).start();
      }
      catch (IOException e) {
        System.out.println("Couldn't connect player: " + e);
        System.exit(1);
      }
    }
  }

  public synchronized Game waitForGame(Connect4Player p) {
    Game retval = null;
    if (playerWaiting == null) {
      playerWaiting = p;
      thisGame = null;    // just in case!
      p.send("PLSWAIT");
      while (playerWaiting != null) {
        try {
          wait();
        }
        catch (InterruptedException e) {
          System.out.println("Error: " + e);
        }
      }
      return thisGame;
    }
    else {
      thisGame = new Game(playerWaiting, p);
      retval = thisGame;
      playerWaiting = null;
      notify();
      return retval;
    }
  } 

  protected void finalize() {
    if (port != null) {
      try { 
        port.close(); 
      }
      catch (IOException e) {
        System.out.println("Error closing port: " + e);
      }
      port = null;
    }
  }
}
