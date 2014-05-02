import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;
import Score4Server.Connect4Daemon;

public class ChatServer implements Runnable
{  private ChatServerThread clients[] = new ChatServerThread[50];
   private ServerSocket server = null;
   private Thread       thread = null;
   private int clientCount = 0;
   private Connection con = null;

   public ChatServer(int port)
   {  try
      {  System.out.println("Binding to port " + port + ", please wait  ...");
         server = new ServerSocket(port);  
         System.out.println("Server started: " + server);
         start(); }
      catch(IOException ioe)
      {  System.out.println("Can not bind to port " + port + ": " + ioe.getMessage()); }
   
       
   try
   {
    Class.forName("com.mysql.jdbc.Driver");
    String username="pdpuser";
    String password="resupdp";
    String url="jdbc:mysql://195.251.209.12:3306/it12Score4_Chat";
    con=DriverManager.getConnection(url,username,password);

   }
   catch(Exception e) {
    System.out.println(e);
   }
   
   
   }
   public void run()
   {  while (thread != null)
      {  try
         {  System.out.println("Waiting for a client ..."); 
            addThread(server.accept()); }
         catch(IOException ioe)
         {  System.out.println("Server accept error: " + ioe); stop(); }
      }
   }
   public void start()
   {  if (thread == null)
      {  thread = new Thread(this); 
         thread.start();
      }
   }
   public void stop()
   {  if (thread != null)
      {  thread.stop(); 
         thread = null;
      }
   }
   private int findClient(String ID)
   {  for (int i = 0; i < clientCount; i++)
         if (clients[i].getID().equals(ID))
            return i;
      return -1;
   }
   public synchronized void handle(String ID, String input, String property)
   {  
	   if(property.equals("#login_verification#"))
		{
			try
			{
			Statement st=(Statement) con.createStatement();
			//"select * from Players where Player_Name="+currentID;
			String query ="select * from Players where Player_Name='"+input+"'";
			ResultSet rs=st.executeQuery(query);
			while(rs.next())
			{
				if(rs!=null)
				{
					clients[findClient(ID)].setname(input);
					clients[findClient(ID)].send(new Communication("You have been authenticated","#authentication#"));
				}
				else {
					System.out.println("not");
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
	   
	   
	   if (input.equals(".bye"))
	   {
		   clients[findClient(ID)].send(new Communication("Public",".bye") );
		   remove(ID);
	   }
	   else if(property.equals("Private"))//Message is for Team
	   {
		   //FIND THE PARTNER ID BASED ON YOUR ID
		   if(findClient(ID) %2 ==0)
		   {//first player to join the team
			   clients[findClient(ID)].send(new Communication("("+ID+")"+clients[findClient(ID)].getname() + ": " + input, property) );
			   try{
				   clients[findClient(ID)+1].send(new Communication("("+ID+")"+clients[findClient(ID)].getname() + ": " + input, property) );
			   }
			   catch(NullPointerException e){
				   clients[findClient(ID)].send(new Communication("No teammate joined!!!", property) );
			   }
		   }
		   else
		   {
			   clients[findClient(ID)].send(new Communication("("+ID+")"+clients[findClient(ID)].getname() + ": " + input, property) );
			   clients[findClient(ID)-1].send(new Communication("("+ID+")"+clients[findClient(ID)].getname() + ": " + input, property) );
		   }
		   
		  
	   }
	   else if(property.equals("#win_state#"))
	   {
		   try
			{
			Statement st1=(Statement) con.createStatement();
			//"select * from Players where Player_Name="+currentID;
			String newstring = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date());
			String query1 ="INSERT INTO Games (Name, Date, State) " + "VALUES ('"+input+"', '"+newstring+"', 'WIN')";
			st1.executeUpdate(query1);
			
			//	rs.close();
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
			Statement st2=(Statement) con.createStatement();
			//"select * from Players where Player_Name="+currentID;
			String newstring = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date());
			String query2 ="INSERT INTO Games (Name, Date, State) " + "VALUES ('"+input+"', '"+newstring+"', 'LOST')";
			st2.executeUpdate(query2);
			
			//	rs.close();
			st2.close();
			
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		   
	   }
	   else if(property.equals("#tie_state#"))
	   {
		   try
			{
			Statement st3=(Statement) con.createStatement();
			//"select * from Players where Player_Name="+currentID;
			String newstring = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date());
			String query3 ="INSERT INTO Games (Name, Date, State) " + "VALUES ('"+input+"', '"+newstring+"', 'TIE')";
			st3.executeUpdate(query3);
			
			//	rs.close();
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
   {  int pos = findClient(ID);
      if (pos >= 0)
      {  ChatServerThread toTerminate = clients[pos];
         System.out.println("Removing client thread " + ID + " at " + pos);
         if (pos < clientCount-1)
            for (int i = pos+1; i < clientCount; i++)
               clients[i-1] = clients[i];
         clientCount--;
         try
         {  toTerminate.close(); }
         catch(IOException ioe)
         {  System.out.println("Error closing thread: " + ioe); }
         toTerminate.stop(); }
   }
   private void addThread(Socket socket)
   {  if (clientCount < clients.length)
      {  System.out.println("Client accepted: " + socket);
         clients[clientCount] = new ChatServerThread(this, socket);
         try
         {  clients[clientCount].open(); 
            clients[clientCount].start();  
            clientCount++; }
         catch(IOException ioe)
         {  System.out.println("Error opening thread: " + ioe); } }
      else
         System.out.println("Client refused: maximum " + clients.length + " reached.");
   }
   public static void main(String args[])
   {  ChatServer server = null;
      Thread thread = null;
     
      
      //if (args.length != 1)
       //  System.out.println("Usage: java ChatServer port");
    //  else
      server = new ChatServer(4444);


      new Connect4Daemon().start();
   }

}
