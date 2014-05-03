import java.net.*;
import java.io.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import Score4.Connect4;
import Score4.Connect4ClientConnection;
import Score4.Connect4Engine;
public class ChatClient extends Applet implements Runnable
{
	public ChatClient() {

	}  
	private Socket socket              = null;
	private DataInputStream  console   = null;
	private DataOutputStream streamOut = null;
	private ObjectOutputStream streamOutObject = null;
	private ChatClientThread clientPublic    = null;
	private TextArea  displayPublic = new TextArea(13,5);
	private TextArea displayPrivate = new TextArea(13,5);
	private TextField inputPublic   = new TextField();
	private TextField inputPrivate = new TextField();
	private Button    sendPublic    = new Button("Public"), connect = new Button("Connect"),
			quit    = new Button("Bye");
	private Button sendPrivate = new Button("Team");
	private String    serverName = "localhost";
	private int       serverPort = 4444;

	//
	private Image           offImage, boardImg, handImg;
	private Image[]         pieceImg = new Image[2];

	private AudioClip       newGameSnd, sadSnd, applauseSnd, badMoveSnd, redSnd, blueSnd;

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

	//
	private Panel cardPanel;
	private Panel loginPanel, gamePanel;
	private CardLayout cl;
	private TextField username=null;
	private Boolean authenticated=false;
	private String Player_Name=null;

	final static String LOGINPANEL="LOGINPANEL";
	final static String GAMEPANEL="GAMEPANEL";

	public void init()
	{ 


>>>>>>> upstream/master
		cardPanel=new Panel();
		cl=new CardLayout();
		cardPanel.setLayout(cl);

		this.setLayout(new BorderLayout());
		this.add(cardPanel, BorderLayout.EAST);


		//create login frame
		loginPanel=new Panel();
		username=new TextField("",20);
		Button loginBt=new Button("Login");
		loginPanel.add(username);
		loginBt.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				try
				{

					if(!authenticated)
					{
						Player_Name=username.getText();
						connect(serverName, serverPort);
						streamOutObject.writeObject(new Communication(username.getText(),"#login_verification#"));
					}
					else
					{
						cl.show(cardPanel, "game");
						repaint();
					}
				}

				catch(IOException ioe)
				{  
					printlnPublic("Sending error: " + ioe.getMessage()); close(); 
				}			
			}
		}
		);
		loginPanel.add(loginBt);

		//
		// Load and track the images
		setSize(800,285);
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
		//


		Panel keys = new Panel();
		keys.setLayout(new GridLayout(1,2));
		keys.add(quit); 
		keys.add(connect);
		Panel south = new Panel(); 
		south.setBounds(0, 250, 400, 34);
		south.setLayout(new GridLayout(1,5));
		south.add(keys); 

		south.add(inputPublic);
		south.add(sendPublic);
		south.add(inputPrivate);  
		south.add(sendPrivate);
		//setLayout(null);


		gamePanel=new Panel(new BorderLayout());
		//display.setBounds(200, 0, 200, 252);
		Panel display=new Panel(new GridLayout(1,2));
		display.add(displayPublic);
		display.add(displayPrivate);
		Panel helpPanel=new Panel(new GridBagLayout());
		GridBagConstraints c= new GridBagConstraints();

		c.fill=GridBagConstraints.HORIZONTAL;
		c.ipady=10;
		c.gridx=0;
		c.gridy=0;
		//c.ipady=10;
		helpPanel.add(display,c);

		c.ipady=0;
		c.gridx=0;
		c.gridy=1;

		helpPanel.add(south,c);
		gamePanel.add(helpPanel, BorderLayout.EAST);
		//gamePanel.add(display, BorderLayout.EAST);
		//gamePanel.add(displayPrivate, BorderLayout.EAST);
		//gamePanel.add(south, BorderLayout.SOUTH);



		cardPanel.add(loginPanel, LOGINPANEL);
		cardPanel.add(gamePanel, "game");	
		cl.show(cardPanel, LOGINPANEL);


		//add(display);  
		//	add(south);
		quit.disable(); 
		sendPublic.disable();
		sendPrivate.disable();
		getParameters();
	}

	public boolean action(Event e, Object o)
	{  
		if (e.target == quit)
		{ 
			inputPublic.setText(".bye");
			send(inputPublic,null); 
			send(inputPrivate,null);  
			quit.disable(); 
			sendPublic.disable();
			sendPrivate.disable(); 
			connect.enable();
		}
		else if (e.target == connect)
		{ 
			connect(serverName, serverPort); 
		}
		else if (e.target == sendPublic) //user sends public message
		{  
			send(inputPublic, "Public");
			inputPublic.requestFocus();
		}
		else if(e.target == sendPrivate)//user writes in team
		{ 
			send(inputPrivate, "Private");
			inputPrivate.requestFocus(); 
		}
		return true; 
	}
	public void connect(String serverName, int serverPort)
	{  
		printlnPublic("Establishing connection. Please wait ...");
		try
		{  socket = new Socket(serverName, serverPort);
		printlnPublic("Connected: " + socket);
		open(); 
		sendPublic.enable(); 
		sendPrivate.enable(); 
		connect.disable(); 
		quit.enable();
		}
		catch(UnknownHostException uhe)
		{  
			printlnPublic("Host unknown: " + uhe.getMessage());
		}
		catch(IOException ioe)
		{  
			printlnPublic("Unexpected exception: " + ioe.getMessage());
		} 
	}
	private void send(TextField txtField, String property)
	{ 
		try //send a message and a property of a message
		{  

			Communication comm_temp = new Communication(txtField.getText(), property);
			txtField.setText("");
			// streamOut.writeUTF(txtField.getText()); streamOut.flush(); txtField.setText("");
			streamOutObject.writeObject(comm_temp);
		}  	 
		catch(IOException ioe)

		{ 
			printlnPublic("Sending error: " + ioe.getMessage()); 
			close(); 
		} 
	}

	public void handle(String msg, String property)
	{
		if (msg.equals(".bye"))
		{ 

			printlnPublic("Good bye.");  
			close();
		}

		if (property.equals("Public"))
		{//emfanizetai se diaforetiko analoga me to propertry 
			printlnPublic(msg);
		}

		else if(property.equals("Private"))
		{
			printlnPrivate(msg); 
		}
		else if(property.equals("#authentication#"))
		{
			authenticated=true;
		}
<<<<<<< HEAD
=======
		else if(property.equals("#not_authentication#"))
		{
			String s=(String) JOptionPane.showInputDialog(msg+" Please register by giving your username");
			try
			{
				streamOutObject.writeObject(new Communication(s,"#register#"));
			}
			catch(IOException ioe)
			{  
				printlnPublic("Sending error: " + ioe.getMessage()); close(); 
			}	
		}

>>>>>>> upstream/master
	}
	
	public void open()
	{ 
		try
		{ 
			streamOut = new DataOutputStream(socket.getOutputStream());
			streamOutObject = new ObjectOutputStream(socket.getOutputStream());
			clientPublic = new ChatClientThread(this, socket); 
		}
		catch(IOException ioe)
		{ 
			printlnPublic("Error opening output stream: " + ioe);
		} 
	}
	public void close()
	{ 
		try
		{ 
			if (streamOut != null) 
				streamOut.close();
			if (streamOutObject != null) 
				streamOutObject.close();
			if (socket    != null) 
				socket.close();
		}
		catch(IOException ioe)
		{ 
			printlnPublic("Error closing ...");
		}
		clientPublic.close();
		clientPublic.stop();
	}
	private void printlnPublic(String msg) //for public TextArea
	{  
		displayPublic.appendText(msg + "\n"); 
	}

	private void printlnPrivate(String msg)//for private TextArea
	{ 
		displayPrivate.appendText(msg + "\n"); 
	}

	public void getParameters()
	{  
		serverName = getParameter("host");
		//serverPort = Integer.parseInt(getParameter("port")); 
		serverPort = 4444; 
	}

	public void start()
	{
		if (thread == null) 
		{
			thread = new Thread(this);
			thread.start();
		}
	}


	public void stop()
	{
		if (thread != null)
		{
			thread.stop();
			thread = null;
		}
	}

	public void run() 
	{
		// Track the images
		int gameState = 0;
		newGame();
		try 
		{
			tracker.waitForID(0);
		}
		catch (InterruptedException e)
		{
			return;
		}

		try
		{
			// Create the connection
			connection = new Connect4ClientConnection(this);
			while (connection.isConnected())
			{
				int istatus = connection.getTheirMove();
				if (istatus == Connect4ClientConnection.GAMEOVER) 
				{
					myMove = false;
					gameState = 0;
					return;
				}
				// Wait for the other player
				else if (istatus == Connect4ClientConnection.PLSWAIT) 
				{
					if (gameState == 0) 
					{
						gameState = Connect4ClientConnection.PLSWAIT;
						status = new String("Wait for player");
						repaint();
					} 
					else
					{
						System.out.println("Gameflow error!");
						return;
					}
				}
				else if (istatus == Connect4ClientConnection.THEIRTURN)
				{

					status = new String("Their turn.");
					myMove = false;
					gameState = Connect4ClientConnection.THEIRTURN;
					repaint();
				}
				else if (istatus == Connect4ClientConnection.YOURTURN)
				{
					gameState = Connect4ClientConnection.YOURTURN;
					status = new String("Your turn.");
					repaint();
					myMove = true;
				}

				else if (istatus == Connect4ClientConnection.THEYWON) 
				{
					gameState = Connect4ClientConnection.THEYWON;
				}
				else if (istatus == Connect4ClientConnection.THEYQUIT)
				{

					gameState = Connect4ClientConnection.THEYQUIT;
					status = new String("Opponent Quit!");
					myMove = false;
					repaint();
					return;
				}
				else if (istatus == Connect4ClientConnection.THEYTIED)
				{
					gameState = Connect4ClientConnection.THEYTIED;
				}
				else if (istatus == Connect4ClientConnection.ERROR)
				{
					System.out.println("error!");
					gameState = Connect4ClientConnection.ERROR;
					status = new String("Error! Game Over");
					myMove = false;
					repaint();
					return;
				}
				else
				{
					if (gameState == Connect4ClientConnection.THEIRTURN) 
					{
      				    // Note that we make the move, but wait for the *server*
						// to say YOURTURN before we change the status. Otherwise,
						// we have a race condition - if the player moves before
						// the server says YOURTURN, we go back into that mode,
						// allowing the player to make two turns in a row!
						Point pos = gameEngine.makeMove(1, istatus);
						blueSnd.play();
						repaint();
					}
					else if (gameState == Connect4ClientConnection.THEYWON)
					{
						status = new String("Sorry, you lose!");
						try
						{
							streamOutObject.writeObject(new Communication(Player_Name,"#lost_state#"));
						}
						catch(IOException ioe)
						{  
							printlnPublic("Sending error: " + ioe.getMessage()); close(); 
						}	

						myMove = false;
						gameOver = true;
						repaint();
						sadSnd.play();
						return;
					}
					else if (gameState == Connect4ClientConnection.THEYTIED)
					{
						status = new String("Tie game!");
						myMove = false;
						gameOver = true;
						repaint();
						sadSnd.play();
						return;
					}
					else 
					{
						System.out.println("Gameflow error!");
						return;
					}
				}
			}
		}
		catch (IOException e)
		{
			System.out.println("IOException: "+e);
		}
	}

	public void update(Graphics g)
	{
		if(authenticated)
		{
		// Create the offscreen graphics context
		if (offGrfx == null) 
		{
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
				switch(board[i][j]) 
				{
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
		offGrfx.drawString(status, 0,
				statusMetrics.getHeight());

		// Draw the image onto the screen
		g.drawImage(offImage, 0, 0, null);
	}
	}

	public void paint(Graphics g) 
	{
		if ((tracker.statusID(0, true) & MediaTracker.ERRORED) != 0)
		{
			// Draw the error rectangle
			g.setColor(Color.red);
			g.fillRect(0, 0, size().width, size().height);
			return;
		}
		if ((tracker.statusID(0, true) & MediaTracker.COMPLETE) != 0)
		{
			// Draw the offscreen image
			g.drawImage(offImage, 0, 0, null);
		}
		else
		{
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
	public boolean mouseMove(Event evt, int x, int y) 
	{
		// Update the current X position (for the hand selector)
		if (!gameOver && myMove)
		{

			curXPos = x / 28;
			repaint();
		}
		return true;
	}
	public boolean mouseDown(Event evt, int x, int y) 
	{
		if (gameOver) 
		{
			thread = null;
			thread = new Thread(this);
			thread.start();
		}
		else if (myMove) 
		{
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
					else
					{
						sadSnd.play();
						status = new String("It's a tie!");
						try
						{
							streamOutObject.writeObject(new Communication(Player_Name,"#tie_state#"));
						}
						catch(IOException ioe)
						{  
							printlnPublic("Sending error: " + ioe.getMessage()); close(); 
						}	

						gameOver = true;
						connection.sendITIED();
						connection.sendMove(pos.x);
					}
				else 
				{
					applauseSnd.play();
					status = new String("You won!");
					try
					{
						streamOutObject.writeObject(new Communication(Player_Name,"#win_state#"));
					}
					catch(IOException ioe)
					{  
						printlnPublic("Sending error: " + ioe.getMessage()); close(); 
					}	

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
	public void newGame() 
	{
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
