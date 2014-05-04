package Score4Server;


// Connect4Player Class
// Connect4Player.java

// All code graciously developed by Greg Turner. You have the right
// to reuse this code however you choose. Thanks Greg!

// Imports
import java.net.*;
import java.io.*;




public class Connect4Player extends SocketAction {
  private Connect4Daemon daemon = null;

  public Connect4Player(Connect4Daemon server, Socket sock) {
    super(sock);
    daemon = server;
  }

  public void run() {
	  System.out.println("4 -- Connect4Player(): 26");
   //daemon.waitForGame(this).playGame(this);
	 daemon.waitForGame4(this).playGame4(this);
  }

  public void closeConnections() {
    super.closeConnections();
    if (outStream != null) {
      send("GAMEOVER");
    }
  }
}
