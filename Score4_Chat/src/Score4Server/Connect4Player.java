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
  private int PlayerNum=0;

  public Connect4Player(Connect4Daemon server, Socket sock, int p) {
    super(sock);
    daemon = server;
    PlayerNum=p;
  }

  public void run() {
	 if(PlayerNum==2)
	 {
    daemon.waitForGame(this).playGame(this);
	 }
	 else if(PlayerNum==4)
	 {
		 daemon.waitForGame4(this).playGame4(this);
	}
  }

  public void closeConnections() {
    super.closeConnections();
    if (outStream != null) {
      send("GAMEOVER");
    }
  }
}
