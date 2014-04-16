package Score4Server;

// Connect4Player Class
// Connect4Player.java

// All code graciously developed by Greg Turner. You have the right
// to reuse this code however you choose. Thanks Greg!

// Imports
import java.net.*;
import java.io.*;

class Connect4Player extends SocketAction {
  private Connect4Daemon daemon = null;

  public Connect4Player(Connect4Daemon server, Socket sock) {
    super(sock);
    daemon = server;
  }

  public void run() {
    daemon.waitForGame(this).playGame(this);
  }

  public void closeConnections() {
    super.closeConnections();
    if (outStream != null) {
      send("GAMEOVER");
    }
  }
}
