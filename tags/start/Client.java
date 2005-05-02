import java.net.*;
import java.io.*;
import java.util.*;

public class Client
{
	private Socket socket;
	PrintWriter out;
	BufferedReader in;
	
	private Client()
	{
		
	}
	
	public Client(String ip)
	{
		try
		{
			socket=new Socket(ip, 6565);
			in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out=new PrintWriter(socket.getOutputStream(), true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		try
		{
			socket.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();		
		}
	}
	
	public LinkedList listDir(String path)
	{		
		LinkedList list=new LinkedList();
		out.write(ServerConnectionThread.LIST_SHARES);
		out.println(path);
		
		String inputLine;
		try
		{
			while ((inputLine = in.readLine()) != null &&
					inputLine.toCharArray()[0]!=ServerConnectionThread.TERMINATOR)
			{					
				list.add(inputLine);
			}			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return list;
		
	}
	
	public boolean requestFileSend(String path, File file)
	{			
		if (file.isFile())
		{
			out.write(ServerConnectionThread.REQUEST_SEND);
			out.println(path);
			out.println(file.getAbsolutePath());
			out.println(""+file.length());
		}
		else if (file.isDirectory())
		{
			out.write(ServerConnectionThread.REQUEST_MKDIR);
			out.println(path);
			out.println(file.getAbsolutePath());
		}
		
		int status=ServerConnectionThread.NO;
		
		try
		{
			status=in.read();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return status==ServerConnectionThread.OK;
	}
	
	public void sendFile(final String filename, final SendProgressListener listener)
	{
		try
		{
			DataOutputStream out=new DataOutputStream(socket.getOutputStream());			
			DataInputStream filein=new DataInputStream(new FileInputStream(filename));
			byte[] buffer=new byte[1024];
			int read;
			long sent=0;
			while ((read = filein.read(buffer)) != -1)
			{
				out.write(buffer, 0, read);				
				sent+=read;
				listener.progressUpdate(sent);
			}
			filein.close();
		}
		catch (Exception e)
		{
		}					
	}
}
