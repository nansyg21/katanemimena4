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
  private int PlayerNum=0;

  public Game(Connect4Player p1, Connect4Player p2) {
    player1 = p1;
    player2 = p2;
    p1Queue = new Vector(10,10); //Vector(int size, int incr)
    p2Queue = new Vector(10,10);
    PlayerNum=2;
  }
  
  public Game(ArrayList<Connect4Player> players) {
	  this.players = players;
	  team1.add(players.get(0));
	  team1.add(players.get(2));
	  team2.add(players.get(1));
	  team2.add(players.get(3));	
	  
	  p1Queue = new Vector(10,10); //Vector(int size, int incr)
	  p2Queue = new Vector(10,10);
	  p3Queue = new Vector(10,10); //Vector(int size, int incr)
	  p4Queue = new Vector(10,10);
	  PlayerNum=4;
  }

  public void playGame(Connect4Player me) {
    String instr;
    boolean playgame = true;
    boolean theirturn = false;
    boolean teammate=false;

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
          me.send("YOURTURN");
          instr = me.receive();
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
  
  public void playGame4(Connect4Player me) {
	    String instr;
	    boolean playgame = true;
	    boolean theirturn = false;
	    boolean teammate=false;

	    try {
	      if ((me == team2.get(0))||(me == team2.get(1))) {
	        theirturn = true;
	      }
	      else if(me == team1.get(1))
	      {
	    	  teammate=true;
	      }
	      else if (me != team1.get(0)) {
	        System.out.println("Illegal call to playGame!");
	        return;
	      }

	      while (playgame) {
	        if ((!theirturn)&&(!teammate)) {
	          me.send("YOURTURN");
	          instr = me.receive();
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
	        	if(theirturn)
	        		theirturn = false;
	        	else if(teammate)
	        		teammate = false;
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

  private synchronized void sendStatus(Connect4Player me, int message) {
	Vector theirVector = ((me == player1) ?  p2Queue : p1Queue);
    theirVector.addElement(new Integer(message));
    notify();
  }
}
