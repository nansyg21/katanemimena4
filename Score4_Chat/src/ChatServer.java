import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.io.*;
import Score4Server.Connect4Daemon;

public class ChatServer implements Runnable
{ 
	private ChatServerThread clients[] = new ChatServerThread[50];
	private ServerSocket server = null;
	private Thread       thread = null;
	private int clientCount = 0;
	private Connection con = null;
	private int playerNum=0;

	public ChatServer(int port)
	{ 
		try
		{ 
			System.out.println("Binding to port " + port + ", please wait  ...");
			server = new ServerSocket(port);  
			System.out.println("Server started: " + server);
			start(); }
		catch(IOException ioe)
		{ 
			System.out.println("Can not bind to port " + port + ": " + ioe.getMessage());
		}

		//Connect to the database
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			String username="pdpuser";
			String password="resupdp";
			String url="jdbc:mysql://195.251.209.12:3306/it12Score4_Chat";
			con=DriverManager.getConnection(url,username,password);

		}
		catch(Exception e) 
		{
			System.out.println(e);
		}


	}
	public void run()
	{ 
		while (thread != null)
		{ 
			try
			{ 
				System.out.println("Waiting for a client ..."); 
				addThread(server.accept()); }
			catch(IOException ioe)
			{ 
				System.out.println("Server accept error: " + ioe); stop(); 
			}
		}
	}
	public void start()
	{  if (thread == null)
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
	private int findClient(String ID)
	{ 
		for (int i = 0; i < clientCount; i++)
			if (clients[i].getID().equals(ID))
				return i;
		return -1;
	}
	public synchronized void handle(String ID, String input, String property)
	{  
		if((property.equals("#login_verification2#"))||(property.equals("#login_verification4#")))
		{
			try
			{
				/* When the player hits the login button the server searches if the player already exist on the dbms
				 * If the player does not exists then the server send the message to the client to make the registration
				 * #not_authentication#
				 * If the player exist the server sends the message of authentication to the client #authentication#
				 * */
				Statement st=(Statement) con.createStatement();
				String query ="select * from Players where Player_Name='"+input+"'";
				ResultSet rs=st.executeQuery(query);

				if (!rs.isBeforeFirst() ) {    
					clients[findClient(ID)].send(new Communication("You are not a member","#not_authentication#"));
				} 

				while(rs.next())
				{
					if(rs!=null)
					{
						clients[findClient(ID)].setname(input);
						clients[findClient(ID)].send(new Communication("You have been authenticated","#authentication#"));
						//checks if the mode is for 2 or 4 players
						if(property.equals("#login_verification2#"))
							playerNum=2;
						else if(property.equals("#login_verification4#"))
							playerNum=4;						
					}
					else {
						clients[findClient(ID)].send(new Communication("You are not a member","#not_authentication#"));
					}
				}
				//	rs.close();
				st.close();

			}
			catch(Exception e)
			{
				System.out.println(e);
			}


		}
		/* If the client tries to register the server checks if the username exists in the database
		 * If the username exists the server sends #not_authentication# to the client
		 * If the username is unique then the server authenticates the client #authentication#
		 * */
		else if(property.equals("#register#"))
		{
			try
			{
				Statement st4=(Statement) con.createStatement();
				//"select * from Players where Player_Name="+currentID;
				String query4 ="select * from Players where Player_Name='"+input+"'";
				ResultSet rs4=st4.executeQuery(query4);

				if (!rs4.isBeforeFirst() ) {    
					clients[findClient(ID)].send(new Communication("Register Successful","#authentication#"));
					Statement st5=(Statement) con.createStatement();
					query4="INSERT INTO Players " + "VALUES ('"+input+"')";
					st5.executeUpdate(query4);
					st5.close();
				}
				else
				{
					clients[findClient(ID)].send(new Communication("The username exists","#not_authentication#"));
				}


				//	rs.close();
				st4.close();


			}
			catch(Exception e)
			{
				System.out.println(e);
			}


		}

		if (input.equals(".bye"))
		{
			clients[findClient(ID)].send(new Communication("Public",".bye") );
			remove(ID);
		}
		else if(property.equals("Private"))//Message is for Team
		{
			//FIND THE PARTNER ID BASED ON YOUR ID
			if(findClient(ID) %4 ==0)
			{//first player to join the team
				clients[findClient(ID)].send(new Communication("("+ID+")"+clients[findClient(ID)].getname() + ": " + input, property) );
				try{
					clients[findClient(ID)+2].send(new Communication("("+ID+")"+clients[findClient(ID)].getname() + ": " + input, property) );
				}//prospatheise na steileis ston 2, an den exei syndethei: No teammate joined!!!
				catch(NullPointerException e){
					clients[findClient(ID)].send(new Communication("No teammate joined!!!", property) );
				}
			}
			else if(findClient(ID) %4 ==1)//second player to join the team
			{
				clients[findClient(ID)].send(new Communication("("+ID+")"+clients[findClient(ID)].getname() + ": " + input, property) );
				try{//prospatheise na steileis ston 3, an den exei syndethei: No teammate joined!!!
					clients[findClient(ID)+2].send(new Communication("("+ID+")"+clients[findClient(ID)].getname() + ": " + input, property) );
				}catch(NullPointerException e)
				{
					clients[findClient(ID)].send(new Communication("No teammate joined!!!", property) );
				}
			}
			else if(findClient(ID) %4 ==2)//third player to join the team
			{
				clients[findClient(ID)].send(new Communication("("+ID+")"+clients[findClient(ID)].getname() + ": " + input, property) );
				try{//prospatheise na steileis ston 1, an den exei syndethei: No teammate joined!!!
					clients[findClient(ID)-2].send(new Communication("("+ID+")"+clients[findClient(ID)].getname() + ": " + input, property) );
				}catch(NullPointerException e)
				{
					clients[findClient(ID)].send(new Communication("No teammate joined!!!", property) );
				}
			}
			else
			{//fourth player to join the team
				clients[findClient(ID)].send(new Communication("("+ID+")"+clients[findClient(ID)].getname() + ": " + input, property) );
				try{//prospatheise na steileis ston 2, an den exei syndethei: No teammate joined!!!
					
					clients[findClient(ID)-2].send(new Communication("("+ID+")"+clients[findClient(ID)].getname() + ": " + input, property) );
				}catch(NullPointerException e)
				{
					clients[findClient(ID)].send(new Communication("No teammate joined!!!", property) );
				}
			}


		
		}
		else if(property.equals("#win_state#"))
		{
			/* If the player wins then the server updates the database with the players username the current date and time
			 * and the WIN as the state
			 * */
			try
			{
				
				Statement st1=(Statement) con.createStatement();
				String newstring = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date());
				String query1 ="INSERT INTO Games (Name, Date, State) " + "VALUES ('"+input+"', '"+newstring+"', 'WIN')";
				st1.executeUpdate(query1);

				st1.close();

			}
			catch(Exception e)
			{
				System.out.println(e);
			}

		}
		else if(property.equals("#lost_state#"))
		{
			try
			{
				/* If the player looses then the server updates the database with the players username the current date and time
				 * and the LOST as the state
				 * */
				Statement st2=(Statement) con.createStatement();
				String newstring = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date());
				String query2 ="INSERT INTO Games (Name, Date, State) " + "VALUES ('"+input+"', '"+newstring+"', 'LOST')";
				st2.executeUpdate(query2);

				st2.close();

			}
			catch(Exception e)
			{
				System.out.println(e);
			}

		}
		else if(property.equals("#tie_state#"))
		{
			/* If the player ties then the server updates the database with the players username the current date and time
			 * and the TIE as the state
			 * */
			try
			{
				Statement st3=(Statement) con.createStatement();
				String newstring = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date());
				String query3 ="INSERT INTO Games (Name, Date, State) " + "VALUES ('"+input+"', '"+newstring+"', 'TIE')";
				st3.executeUpdate(query3);

				st3.close();

			}
			catch(Exception e)
			{
				System.out.println(e);
			}

		}
		else              ////Message is for Public
			for (int i = 0; i < clientCount; i++)
				clients[i].send(new Communication("("+ID+")"+clients[findClient(ID)].getname() + ": " + input, property) );
	}
	public synchronized void remove(String ID)
	{  
		int pos = findClient(ID);
		if (pos >= 0)
		{ 
			ChatServerThread toTerminate = clients[pos];
			System.out.println("Removing client thread " + ID + " at " + pos);
			if (pos < clientCount-1)
				for (int i = pos+1; i < clientCount; i++)
					clients[i-1] = clients[i];
			clientCount--;
			try
			{ 
				toTerminate.close();
			}
			catch(IOException ioe)
			{  
				System.out.println("Error closing thread: " + ioe);
			}
			toTerminate.stop(); 
		}
	}

	private void addThread(Socket socket)
	{ 
		if (clientCount < clients.length)
		{ 
			System.out.println("Client accepted: " + socket);
			clients[clientCount] = new ChatServerThread(this, socket);
			try
			{ 
				clients[clientCount].open(); 
				clients[clientCount].start();  
				clientCount++; 
				//If the player number is 4 start the game only when all the 4 players are in
				if(playerNum==4)
				{
				if(clientCount%4==0)
				{					
				for (int i = 0; i < clientCount; i++)
						
					clients[i].send(new Communication("Start", "#start#"));
				}
				}
				//If the player number is 2 start the game only when all the 2 players are in
				else if(playerNum==2)
				{
					if(clientCount%2==0)
					{
						
					for (int i = 0; i < clientCount; i++)
							
						clients[i].send(new Communication("Start", "#start#"));
					}
				}
			}
			catch(IOException ioe)
			{ 
				System.out.println("Error opening thread: " + ioe); 
			}
		}
		else
			System.out.println("Client refused: maximum " + clients.length + " reached.");
	}
	public static void main(String args[])
	{  
		ChatServer server = null;
		Thread thread = null;


		//if (args.length != 1)
		//  System.out.println("Usage: java ChatServer port");
		//  else
		server = new ChatServer(4444);


		new Connect4Daemon().start();
	}

}
