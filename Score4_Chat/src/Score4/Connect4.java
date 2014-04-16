package Score4;


import java.applet.*;
import java.awt.*;
import java.io.IOException;

public class Connect4 extends Applet implements Runnable {
  private Image           offImage, boardImg, handImg;
  private Image[]         pieceImg = new Image[2];
  private AudioClip       newGameSnd, sadSnd, applauseSnd,
    badMoveSnd, redSnd, blueSnd;
  private Graphics        offGrfx;
  private Thread          thread;
  private MediaTracker    tracker;
  private int             delay = 83; // 12 fps
  private Connect4Engine  gameEngine;
  private boolean         gameOver = true, myMove;
  private int             level, curXPos;
  private String          status = new String("Connecting...");
  private Font            statusFont = new Font("Helvetica",
    Font.PLAIN, 20);
  private FontMetrics     statusMetrics;
  private Connect4ClientConnection  connection = null;

  public String getAppletInfo() {
    return (new String("NetConnect4 Client by Michael Morrison/Greg Turner"));
  }

  public void init() {
    // Load and track the images
    tracker = new MediaTracker(this);
    boardImg = getImage(getCodeBase(), "Res/Board.gif");
    tracker.addImage(boardImg, 0);
    handImg = getImage(getCodeBase(), "Res/Hand.gif");
    tracker.addImage(handImg, 0);
    pieceImg[0] = getImage(getCodeBase(), "Res/RedPiece.gif");
    tracker.addImage(pieceImg[0], 0);
    pieceImg[1] = getImage(getCodeBase(), "Res/BluPiece.gif");
    tracker.addImage(pieceImg[1], 0);

    // Load the audio clips
    newGameSnd = getAudioClip(getCodeBase(), "Res/NewGame.au");
    sadSnd = getAudioClip(getCodeBase(), "Res/Sad.au");
    applauseSnd = getAudioClip(getCodeBase(), "Res/Applause.au");
    badMoveSnd = getAudioClip(getCodeBase(), "Res/BadMove.au");
    redSnd = getAudioClip(getCodeBase(), "Res/RedMove.au");
    blueSnd = getAudioClip(getCodeBase(), "Res/BlueMove.au");
  }

  public void start() {
    if (thread == null) {
      thread = new Thread(this);
      thread.start();
    }
  }

  public void stop() {
    if (thread != null) {
      thread.stop();
      thread = null;
    }
  }

  public void run() {
    // Track the images
    int gameState = 0;
    newGame();
    try {
      tracker.waitForID(0);
    }
    catch (InterruptedException e) {
      return;
    }

    try {
      // Create the connection
      connection = new Connect4ClientConnection(this);
      while (connection.isConnected()) {
        int istatus = connection.getTheirMove();
        if (istatus == Connect4ClientConnection.GAMEOVER) {
          myMove = false;
          gameState = 0;
          return;
        }
        // Wait for the other player
        else if (istatus == Connect4ClientConnection.PLSWAIT) {
          if (gameState == 0) {
            gameState = Connect4ClientConnection.PLSWAIT;
            status = new String("Wait for player");
            repaint();
          } else {
            System.out.println("Gameflow error!");
            return;
          }
        }
        else if (istatus == Connect4ClientConnection.THEIRTURN) {
          status = new String("Their turn.");
          myMove = false;
          gameState = Connect4ClientConnection.THEIRTURN;
          repaint();
        }
        else if (istatus == Connect4ClientConnection.YOURTURN) {
          gameState = Connect4ClientConnection.YOURTURN;
          status = new String("Your turn.");
          repaint();
          myMove = true;
        }
        else if (istatus == Connect4ClientConnection.THEYWON) {
          gameState = Connect4ClientConnection.THEYWON;
        }
        else if (istatus == Connect4ClientConnection.THEYQUIT) {
          gameState = Connect4ClientConnection.THEYQUIT;
          status = new String("Opponent Quit!");
          myMove = false;
          repaint();
          return;
        }
        else if (istatus == Connect4ClientConnection.THEYTIED) {
          gameState = Connect4ClientConnection.THEYTIED;
        }
        else if (istatus == Connect4ClientConnection.ERROR) {
          System.out.println("error!");
          gameState = Connect4ClientConnection.ERROR;
          status = new String("Error! Game Over");
          myMove = false;
          repaint();
          return;
        }
        else {
          if (gameState == Connect4ClientConnection.THEIRTURN) {
            // Note that we make the move, but wait for the *server*
            // to say YOURTURN before we change the status. Otherwise,
            // we have a race condition - if the player moves before
            // the server says YOURTURN, we go back into that mode,
            // allowing the player to make two turns in a row!
            Point pos = gameEngine.makeMove(1, istatus);
            blueSnd.play();
            repaint();
          }
          else if (gameState == Connect4ClientConnection.THEYWON) {
            status = new String("Sorry, you lose!");
            myMove = false;
            gameOver = true;
            repaint();
            sadSnd.play();
            return;
          }
          else if (gameState == Connect4ClientConnection.THEYTIED) {
            status = new String("Tie game!");
            myMove = false;
            gameOver = true;
            repaint();
            sadSnd.play();
            return;
          }
          else {
            System.out.println("Gameflow error!");
            return;
          }
        }
      }
    }
    catch (IOException e) {
      System.out.println("IOException: "+e);
    }
  }


  public void update(Graphics g) {
    // Create the offscreen graphics context
    if (offGrfx == null) {
      offImage = createImage(size().width, size().height);
      offGrfx = offImage.getGraphics();
      statusMetrics = offGrfx.getFontMetrics(statusFont);
    }

    // Draw the board
    offGrfx.drawImage(boardImg, 0, 0, this);

    // Draw the pieces
    int[][] board = gameEngine.getBoard();
    for (int i = 0; i < 7; i++)
    for (int j = 0; j < 6; j++)
    switch(board[i][j]) {
      case 0:
        offGrfx.drawImage(pieceImg[0], (i + 1) * 4 + i *
          pieceImg[0].getWidth(this), (6 - j) * 4 + (5 - j) *
          pieceImg[0].getHeight(this) + 67, this);
        break;

      case 1:
        offGrfx.drawImage(pieceImg[1], (i + 1) * 4 + i *
          pieceImg[1].getWidth(this), (6 - j) * 4 + (5 - j) *
          pieceImg[1].getHeight(this) + 67, this);
        break;

      default:
        offGrfx.setColor(Color.white);
        offGrfx.fillOval((i + 1) * 4 + i *
          pieceImg[0].getWidth(this), (6 - j) * 4 + (5 - j) *
          pieceImg[0].getHeight(this) + 67,
          pieceImg[0].getWidth(this), pieceImg[0].getHeight(this));
        break;
    }

    // Draw the hand selector
    if (!gameOver && myMove)
      offGrfx.drawImage(handImg, (curXPos + 1) * 4 + curXPos *
        pieceImg[0].getWidth(this) + (pieceImg[0].getWidth(this) -
        handImg.getWidth(this)) / 2, 63 - handImg.getHeight(this),
        this);

    // Draw the game status
    offGrfx.setColor(Color.black);
    offGrfx.setFont(statusFont);
    offGrfx.drawString(status, (size().width -
      statusMetrics.stringWidth(status)) / 2,
      statusMetrics.getHeight());

    // Draw the image onto the screen
    g.drawImage(offImage, 0, 0, null);
  }

  public void paint(Graphics g) {
    if ((tracker.statusID(0, true) & MediaTracker.ERRORED) != 0) {
      // Draw the error rectangle
      g.setColor(Color.red);
      g.fillRect(0, 0, size().width, size().height);
      return;
    }
    if ((tracker.statusID(0, true) & MediaTracker.COMPLETE) != 0) {
      // Draw the offscreen image
      g.drawImage(offImage, 0, 0, null);
    }
    else {
      // Draw the title message (while the images load)
      Font        f1 = new Font("TimesRoman", Font.BOLD, 28),
                  f2 = new Font("Helvetica", Font.PLAIN, 16);
      FontMetrics fm1 = g.getFontMetrics(f1),
                  fm2 = g.getFontMetrics(f2);
      String      s1 = new String("Connect4"),
                  s2 = new String("Loading images...");
      g.setFont(f1);
      g.drawString(s1, (size().width - fm1.stringWidth(s1)) / 2,
        ((size().height - fm1.getHeight()) / 2) + fm1.getAscent());
      g.setFont(f2);
      g.drawString(s2, (size().width - fm2.stringWidth(s2)) / 2,
        size().height - fm2.getHeight() - fm2.getAscent());
    }
  }

  public boolean mouseMove(Event evt, int x, int y) {
    // Update the current X position (for the hand selector)
    if (!gameOver && myMove) {
      curXPos = x / 28;
      repaint();
    }
    return true;
  }

  public boolean mouseDown(Event evt, int x, int y) {
    if (gameOver) {
      thread = null;
      thread = new Thread(this);
      thread.start();
    }
    else if (myMove) {
      // Make sure the move is valid
      Point pos = gameEngine.makeMove(0, x / 28);
      if (pos.y >= 0) {
        if (!gameEngine.isWinner(0))
          if (!gameEngine.isTie()) {
            redSnd.play();
            status = new String("Their turn.");
            connection.sendMove(pos.x);
            myMove = false;
          }
          else {
            sadSnd.play();
            status = new String("It's a tie!");
            gameOver = true;
            connection.sendITIED();
            connection.sendMove(pos.x);
          }
          else {
            applauseSnd.play();
            status = new String("You won!");
            gameOver = true;
            connection.sendIWON();
            connection.sendMove(pos.x);
          }
        repaint();
      }
    }
    else
      badMoveSnd.play();
    return true;
  }

  public void newGame() {
    // Setup a new game
    status = new String("Connecting");
    newGameSnd.play();
    gameEngine = new Connect4Engine();
    gameOver = false;
    myMove = false;
    level = 3;
    repaint();
  }
}
