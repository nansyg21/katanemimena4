package Score4;

// Connect4State Class
// Connect4State.java

// All code based on original ideas and code by Keith Pomakis
// and Sven Wiebus. Thank you both very much for your hard work
// and generosity!

// Imports
class Connect4State {
  public static final int winPlaces = 69, maxPieces = 42,
    Empty = 2;
  public static boolean[][][] map;
  public int[][]  board = new int[7][6];
  public int[][]  score = new int[2][winPlaces];
  public int      numPieces;

  public Connect4State() {
    // Initialize the map
    int i, j, k, count = 0;
    if (map == null) {
      map = new boolean[7][6][winPlaces];
      for (i = 0; i < 7; i++)
        for (j = 0; j < 6; j++)
          for (k = 0; k < winPlaces; k++)
            map[i][j][k] = false;

      // Set the horizontal win positions
      for (i = 0; i < 6; i++)
        for (j = 0; j < 4; j++) {
          for (k = 0; k < 4; k++)
            map[j + k][i][count] = true;
          count++;
        }
    
      // Set the vertical win positions
      for (i = 0; i < 7; i++)
        for (j = 0; j < 3; j++) {
          for (k = 0; k < 4; k++)
            map[i][j + k][count] = true;
          count++;
        }

      // Set the forward diagonal win positions
      for (i = 0; i < 3; i++)
        for (j = 0; j < 4; j++) {
          for (k = 0; k < 4; k++)
            map[j + k][i + k][count] = true;
          count++;
        }

      // Set the backward diagonal win positions
      for (i = 0; i < 3; i++)
        for (j = 6; j >= 3; j--) {
          for (k = 0; k < 4; k++)
            map[j - k][i + k][count] = true;
          count++;
        }
    }

    // Initialize the board
    for (i = 0; i < 7; i++)
      for (j = 0; j < 6; j++)
        board[i][j] = Empty;

    // Initialize the scores
    for (i = 0; i < 2; i++)
      for (j = 0; j < winPlaces; j++)
        score[i][j] = 1;

    numPieces = 0;
  }

  public Connect4State(Connect4State state) {
    // Copy the board
    for (int i = 0; i < 7; i++)
      for (int j = 0; j < 6; j++)
        board[i][j] = state.board[i][j];

    // Copy the scores
    for (int i = 0; i < 2; i++)
      for (int j = 0; j < winPlaces; j++)
        score[i][j] = state.score[i][j];

    numPieces = state.numPieces;
  }

  public boolean isWinner(int player) {
    for (int i = 0; i < winPlaces; i++)
      if (score[player][i] == 16)
        return true;
    return false;
  }

  public boolean isTie() {
    return (numPieces == maxPieces);
  }

  public int dropPiece(int player, int xPos) {
    int yPos = 0;
    while ((board[xPos][yPos] != Empty) && (++yPos < 6))
      ;

    // The column is full
    if (yPos == 6)
      return -1;

    // The move is OK
    board[xPos][yPos] = player;
    numPieces++;
    updateScore(player, xPos, yPos);

    return yPos;
  }

  public int evaluate(int player, int level, int depth, int alpha,
    int beta) {
    int goodness, best, maxab = alpha;

    if (level != depth) {
      best = -30000;
      for(int i = 0; i < 7; i++) {
        Connect4State tempState = new Connect4State(this);
        if (tempState.dropPiece(getOtherPlayer(player), i) < 0)
          continue;

        if (tempState.isWinner(getOtherPlayer(player)))
          goodness = 25000 - depth;
        else
          goodness = tempState.evaluate(getOtherPlayer(player),
            level, depth + 1, -beta, -maxab);
        if (goodness > best) {
          best = goodness;
          if (best > maxab)
            maxab = best;
        }
        if (best > beta)
          break;
      }

      // What's good for the other player is bad for this one
      return -best;
    }

    return (calcScore(player) - calcScore(getOtherPlayer(player)));
  }

  private int calcScore(int player) {
    int s = 0;
    for (int i = 0; i < winPlaces; i++)
      s += score[player][i];
    return s;
  }

  private void updateScore(int player, int x, int y) {
    for (int i = 0; i < winPlaces; i++)
      if (map[x][y][i]) {
        score[player][i] <<= 1;
        score[getOtherPlayer(player)][i] = 0;
      }
  }

  private int getOtherPlayer(int player) {
    return (1 - player);
  }
}
