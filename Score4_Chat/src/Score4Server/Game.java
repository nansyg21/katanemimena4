package Score4Server;


// Game Class
// Game.java

// All code graciously developed by Greg Turner. You have the right
// to reuse this code however you choose. Thanks Greg!

// Imports
import java.io.*;
import java.util.*;

public class Game {
  public static final int ERROR = -1;
  public static final int IWON = -2;
  public static final int IQUIT = -3;
  public static final int ITIED = -4;
  public static final int YOURTURN = -5;
  public static final int SENTSTRING = -6;
  private Connect4Player  player1 = null;
  private Connect4Player  player2 = null;
  private ArrayList<Connect4Player>  players = new ArrayList<Connect4Player>();
  private ArrayList<Connect4Player>  team1 = new ArrayList<Connect4Player>();
  private ArrayList<Connect4Player>  team2 = new ArrayList<Connect4Player>();
  private Vector          p1Queue = null;
  private Vector          p2Queue = null;
  private Vector          p3Queue = null;
  private Vector          p4Queue = null;
  private String          sentString;
  int turnCounter = 0;

  public Game(Connect4Player p1, Connect4Player p2) {
    player1 = p1;
    player2 = p2;
    p1Queue = new Vector(10,10); //Vector(int size, int incr)
    p2Queue = new Vector(10,10);
  }
  
  public Game(ArrayList<Connect4Player> players) {
	  this.players = players;
	  team1.add(players.get(0));
	  team1.add(players.get(2));
	  team2.add(players.get(1));
	  team2.add(players.get(3));
	  player1 = players.get(0); 	//this is for not breaking the getStatus
	  player2 = players.get(1);
	  p1Queue = new Vector(10,10);
	  p2Queue = new Vector(10,10);
	  p3Queue = new Vector(10,10);
	  p4Queue = new Vector(10,10);
	  
  }

  public void playGame(Connect4Player me) {
    String instr;
    boolean playgame = true;
    boolean theirturn = false;

    try {
      if (me == player2) {
        theirturn = true;
      }
      else if (me != player1) {
        System.out.println("Illegal call to playGame!");
        return;
      }

      while (playgame) {
        if (!theirturn) {
          me.send("YOURTURN"); 		//me = the client of the current player and this message tells player that it's his turn
          instr = me.receive();		//receive players move
          instr = instr.toUpperCase();
          instr = instr.trim();
          if (instr.startsWith("IQUIT")) {
            sendStatus(me, IQUIT);
            playgame = false;
          }
          else if (instr.startsWith("IWON")) {
            sentString = me.receive();
            sentString = sentString.toUpperCase();
            sentString = sentString.trim();
            sendStatus(me, IWON);
            sendStatus(me, SENTSTRING);
            playgame = false;
          }
          else if (instr.startsWith("ITIED")) {
            sentString = me.receive();
            sentString = sentString.toUpperCase();
            sentString = sentString.trim();
            sendStatus(me, ITIED);
            sendStatus(me, SENTSTRING);
          }
          else {
            sentString = instr;
            sendStatus(me, SENTSTRING);
          }
        }
        else {
          theirturn = false;
        }

        if (playgame) {
          me.send("THEIRTURN");
          int stat = getStatus(me);
          if (stat == IWON) {
            me.send("THEYWON");
            if (getStatus(me) != SENTSTRING) {
              System.out.println("Received Bad Status");
              me.closeConnections();
            }
            me.send(sentString);
            playgame = false;
          }
          else if (stat == ITIED) {
            me.send("THEYTIED");
            if (getStatus(me) != SENTSTRING) {
              System.out.println("Received Bad Status");
              me.closeConnections();
            }
            me.send(sentString);
            playgame = false;
          }
          else if (stat == IQUIT) {
            me.send("THEYQUIT");
            playgame = false;
          }
          else if (stat == SENTSTRING) {
            me.send(sentString);
          }
          else if (stat == ERROR) {
            me.send("ERROR");
            me.closeConnections();
            playgame = false;
          }
          else {
            System.out.println("Received Bad Status");
            sendStatus(me,ERROR);
            me.closeConnections();
            playgame = false;
          }
        }
      }
      me.closeConnections();
      return;
    }
    catch (IOException e) {
      System.out.println("I/O Error: " + e);
      System.exit(1);
    }
  }
  

  

  //as soon as there is a message from the other player it gets read, or else the thread sleeps
  private synchronized int getStatus(Connect4Player me) {
    Vector ourVector = ((me == player1) ? p1Queue : p2Queue);
    while (ourVector.isEmpty()) {
      try {
        wait();
      }
      catch (InterruptedException e) {
        System.out.println("Error: " + e);
      }
    }
    try {
      Integer retval = (Integer)(ourVector.firstElement());
      try {
        ourVector.removeElementAt(0);
      }
      catch (ArrayIndexOutOfBoundsException e) {
        System.out.println("Array index out of bounds: " + e);
        System.exit(1);
      }
      return retval.intValue();
    }
    catch (NoSuchElementException e) {
      System.out.println("Couldn't get first element: " + e);
      System.exit(1);
      return 0; // never reached, just there to appease compiler
    }
  }
  
  private synchronized int getStatus4(Connect4Player me) {
	  Vector ourVector;
	  if(team1.contains(me)){
		 ourVector = ((me == players.get(0)) ? p1Queue : p3Queue);
	  } else {
		 ourVector = ((me == players.get(1)) ? p2Queue : p4Queue);
	  }
	    
	  
		while (ourVector.isEmpty()) {
	      try {
	        wait();
	      }
	      catch (InterruptedException e) {
	        System.out.println("Error: " + e);
	      }
	    }
	    try {
	      Integer retval = (Integer)(ourVector.firstElement());
	      try {
	        ourVector.removeElementAt(0);
	        //maybe notify() here
	      }
	      catch (ArrayIndexOutOfBoundsException e) {
	        System.out.println("Array index out of bounds: " + e);
	        System.exit(1);
	      }
	      return retval.intValue();
	    }
	    catch (NoSuchElementException e) {
	      System.out.println("Couldn't get first element: " + e);
	      System.exit(1);
	      return 0; // never reached, just there to appease compiler
	    }
	  }

  // puts a message in the vector queue as soon as the current player made its move
  private synchronized void sendStatus(Connect4Player me, int message) {
    Vector theirVector = ((me == player1) ?  p2Queue : p1Queue);
    theirVector.addElement(new Integer(message));
    notify();
  }
  
  private synchronized void sendStatus4(Connect4Player me, int message) {
	  Vector tmVector ;
	  if(team1.contains(me)){
			 tmVector = ((me == players.get(2)) ? p1Queue : p3Queue);
			 tmVector.addElement(new Integer(message));
			 p2Queue.addElement(new Integer(message));
			 p4Queue.addElement(new Integer(message));
	  } else {
			 tmVector = ((me == players.get(3)) ? p2Queue : p4Queue);
			 tmVector.addElement(new Integer(message));
			 p1Queue.addElement(new Integer(message));
			 p3Queue.addElement(new Integer(message));
	  }
	  notify();
  }
  
  public void playGame4(Connect4Player me) {
	  String instr;
	  boolean playgame = true;
	  int teammate = 0;
	  try{
		  while(playgame) {
			  System.out.println("9--playGame4():250 - players"+me.getId());
			  me = players.get(turnCounter);

			  me.send("YOURTURN"); 		//me = the client of the current player and this message tells player that it's his turn
			  teammate = (turnCounter+2) % 4;
			  turnCounter = (turnCounter + 1) % 4;
			  instr = me.receive();		//receive players move
			  instr = instr.toUpperCase();
			  instr = instr.trim();
			  if (instr.startsWith("IQUIT")) {
				  sendStatus4(me, IQUIT);
				  playgame = false;
			  }
			  else if (instr.startsWith("IWON")) {
				  sentString = me.receive();
				  sentString = sentString.toUpperCase();
				  sentString = sentString.trim();
				  sendStatus4(me, IWON);
				  sendStatus4(me, SENTSTRING);
				  playgame = false;
			  }
			  else if (instr.startsWith("ITIED")) {
				  sentString = me.receive();
				  sentString = sentString.toUpperCase();
				  sentString = sentString.trim();
				  sendStatus4(me, ITIED);
				  sendStatus4(me, SENTSTRING);
			  }
			  else {
				  sentString = instr;
				  sendStatus4(me, SENTSTRING);
			  }
		  }

		  players.get(teammate).send("TEAMMATE");
		  getStatus4(players.get(teammate));
		  if (playgame) {
			  me.send("THEIRTURN");
			  playgame = updateOpponents((turnCounter+1)%4);
			  playgame = updateOpponents((turnCounter+3)%4);
			  getStatus(me);
		  }
	  }
	  catch (IOException e) {
		  System.out.println("I/O Error: " + e);
		  System.exit(1);
	  }
  }
  
  
  private boolean updateOpponents(int n) {
	  boolean playgame = true;
	  Connect4Player me = players.get(n%4);
	  int stat = getStatus(me);
      if (stat == IWON) {
        me.send("THEYWON");
        if (getStatus(me) != SENTSTRING) {
          System.out.println("Received Bad Status");
          me.closeConnections();
        }
        me.send(sentString);
        playgame = false;
      }
      else if (stat == ITIED) {
        me.send("THEYTIED");
        if (getStatus(me) != SENTSTRING) {
          System.out.println("Received Bad Status");
          me.closeConnections();
        }
        me.send(sentString);
        playgame = false;
      }
      else if (stat == IQUIT) {
        me.send("THEYQUIT");
        playgame = false;
      }
      else if (stat == SENTSTRING) {
        me.send(sentString);
      }
      else if (stat == ERROR) {
        me.send("ERROR");
        me.closeConnections();
        playgame = false;
      }
      else {
        System.out.println("Received Bad Status");
        sendStatus(me,ERROR);
        me.closeConnections();
        playgame = false;
      }
      return playgame;
    }
  
  
}
