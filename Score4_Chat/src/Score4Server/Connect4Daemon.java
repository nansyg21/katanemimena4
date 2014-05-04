package Score4Server;


// Connect4Daemon Class
// Connect4Daemon.java

// All code graciously developed by Greg Turner. You have the right
// to reuse this code however you choose. Thanks Greg!

// Imports
import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class Connect4Daemon extends Thread {
  public static final int PORTNUM = 1234;
  private ServerSocket    port;
  private Connect4Player  playerWaiting = null;
  private Game            thisGame = null;
  private ArrayList<Connect4Player>  players = new ArrayList<Connect4Player>();

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
        System.out.println("3 -- Connect4Deamon() 46, clientSocket"+clientSocket);
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
  
  public synchronized Game waitForGame4(Connect4Player p) {
	  Game retval = null;
	  boolean gameReady = false;
	  System.out.println("5 -- waitForGame4(): 84 - players.size="+players.size());
	  thisGame = null; 		//just in case!
	  p.send("PLSWAIT");
	  System.out.println("6 -- waitForGame4(): 88 - player="+p.getId());
	  players.add(p);
	  if(players.size()>3){
		  gameReady=true;
	  }
	  while (!gameReady) {	//spin lock the thread
		  try {
			  System.out.println("7 -- wait():91 - players.size="+players.size());
			  wait();
		  } catch (InterruptedException e) {
			  System.out.println("Error in waitForGame4 : "+e);
		  }
	  }


	  if(gameReady){
		  System.out.println("8 -- waitForGame4():101 - players.size="+players.size());
		  thisGame = new Game(players);
		  retval = thisGame;
		  notifyAll();
		  //players.clear();
		  return retval;
	  } else {
		  return thisGame;
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
