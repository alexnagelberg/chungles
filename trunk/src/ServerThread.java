import java.io.IOException;
import java.net.*;

public class ServerThread extends Thread
{
	private boolean listening = true;
	
	public ServerThread()
	{
		super("ServerThread");
	}
	
	public void stopListening()
	{
		listening=false;
	}
	
	public void run()
	{
		ServerSocket serverSocket = null;
	    	
	    try
		{
	        serverSocket = new ServerSocket(6565);
	        while (listening)
		    {
		    	new ServerConnectionThread(serverSocket.accept()).start();
		    }
		    serverSocket.close();
	    }
	    catch (IOException e)
		{
	        e.printStackTrace();
	    }	    
	}
}
