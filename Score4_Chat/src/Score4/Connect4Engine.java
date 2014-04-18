package Score4;

// Connect4Engine Class
// Connect4Engine.java

// All code based on original ideas and code by Keith Pomakis
// and Sven Wiebus. Thank you both very much for your hard work
// and generosity!

// Imports
import java.awt.*;
import java.util.Random;

class Connect4Engine {
  private static Random rand = new
    Random(System.currentTimeMillis());
  private Connect4State state;

  public Connect4Engine() {
    state = new Connect4State();
  }

  public Point makeMove(int player, int xPos) {
    int yPos = state.dropPiece(player, xPos);
    return (new Point(xPos, yPos));
  }

  public Point computerMove(int player, int level)
  {
    int bestXPos = -1, goodness = 0, bestWorst = -30000;
    int numOfEqual = 0;

    // Simulate a drop in each of the columns
    for (int i = 0; i < 7; i++) {
      Connect4State tempState = new Connect4State(state);

      // If column is full, move on
      if (tempState.dropPiece(player, i) < 0)
        continue;

      // If this drop wins the game, then cool
      if (tempState.isWinner(player)) {
        bestWorst = 25000;
        bestXPos = i;
      }
      // Otherwise, look ahead to see how good it is
      else
        goodness = tempState.evaluate(player, level, 1, -30000,
          -bestWorst);

      // If this move looks better than previous moves, remember it
      if (goodness > bestWorst) {
        bestWorst = goodness;
        bestXPos = i;
        numOfEqual = 1;
      }

      // If two moves are equally good, make a random choice
      if (goodness == bestWorst) {
        numOfEqual++;
        if (Math.abs(rand.nextInt()) % 10000 <
          (10000 / numOfEqual))
          bestXPos = i;
      }
    }

    // Drop the piece in the best column
    if (bestXPos >= 0) {
      int yPos = state.dropPiece(player, bestXPos);
      if (yPos >= 0)
        return (new Point(bestXPos, yPos));
    }
    return null;
  }

  public int[][] getBoard() {
    return state.board;
  }

  public boolean isWinner(int player) {
    return state.isWinner(player);
  }

  public boolean isTie() {
    return state.isTie();
  }
}
