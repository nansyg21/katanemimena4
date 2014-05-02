import java.net.*;
import java.io.*;

public class ChatServerThread extends Thread
{  private ChatServer       server    = null;
   private Socket           socket    = null;
   private String           ID        =""+ -1;//String, NOT int
   private String           Name      = " "; //proairetiko pedio
   private DataInputStream  streamIn  =  null;
   private ObjectInputStream streamInObject = null;
   private DataOutputStream streamOut = null;
   private ObjectOutputStream streamOutObject = null;
   private String           property  = null;//added

   public ChatServerThread(ChatServer _server, Socket _socket )
   {  super();
      server   = _server;
      socket   = _socket;
      ID       = ""+socket.getPort();
 
   }
   public ChatServerThread(ChatServer _server, Socket _socket, String _ID )
   {  super();
      server   = _server;
      socket   = _socket;
      ID       = _ID;
 
   }
   public void send(Communication comm)
   {   try
       {  //streamOut.writeUTF(comm.getMessage());
          //streamOut.flush();
	        streamOutObject.writeObject(comm);
	        streamOutObject.flush();
       }
       catch(IOException ioe)
       {  System.out.println(ID + " ERROR sending: " + ioe.getMessage());
          server.remove(ID);
          stop();
       }
   }
   public String getID()
   {  return ID;
   }
   public void setID(String id)
   {  ID=id;
   }
   public String getProperty()
   {  return property;
   }
  
   public String getname() {
	return Name;
}
public void setname(String name) {
	Name = name;
}
public void run()
   {  System.out.println("Server Thread " + ID + " running.");
      while (true)
      {  try
         {  //server.handle(ID, streamIn.readUTF());
    	       //accept Serializable object
      	      Communication comm_temp = (Communication)streamInObject.readObject();
    	      server.handle(ID, comm_temp.getMessage(), comm_temp.getProperty());
         }
         catch(IOException ioe)
         {  System.out.println(ID + " ERROR reading: " + ioe.getMessage());
            server.remove(ID);
            stop();
         } catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
      }
   }
   public void open() throws IOException
   {  streamIn = new DataInputStream(new 
                        BufferedInputStream(socket.getInputStream()));
       streamInObject = new ObjectInputStream (socket.getInputStream());
      streamOut = new DataOutputStream(new
                        BufferedOutputStream(socket.getOutputStream()));
      streamOutObject = new ObjectOutputStream(socket.getOutputStream());
   }
   public void close() throws IOException
   {  if (socket != null)    socket.close();
      if (streamIn != null)  streamIn.close();
      if (streamInObject != null)  streamInObject.close();
      if (streamOut != null) streamOut.close();
      if (streamOutObject != null) streamOutObject.close();
   }
}
