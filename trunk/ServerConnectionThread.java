import java.io.*;
import java.net.*;
import java.util.*;

public class ServerConnectionThread extends Thread
{
	private Socket socket;
	
	public final static int TERMINATOR=1;
	public final static int LIST_SHARES=2;
	public final static int REQUEST_SEND=3;
	public final static int REQUEST_MKDIR=4;
	public final static int OK=4;
	public final static int NO=5;
	public final static char IS_FILE='F';
	public final static char IS_DIRECTORY='D';
	
	public ServerConnectionThread(Socket socket)
	{
		super("ServerConnectionThread");
		this.socket=socket;		
	}
	
	public void run()
	{
		try
		{			
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			int command=in.read();
			while (command!=-1)
			{				
				executeCommand(command, in);
				command=in.read();
			}
			socket.close();			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	private void executeCommand(int command, BufferedReader in)
	{
		try
		{
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);		
		
			switch(command)
			{
				case LIST_SHARES:
				{
					listShares(in, out);
					break;
				}
				case REQUEST_SEND:
				{
					requestSend(in);
					break;
				}
				case REQUEST_MKDIR:
				{
					requestMakeDirectory(in);
					break;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void listShares(BufferedReader in, PrintWriter out) throws IOException
	{
		String path=in.readLine();
		
		// Send root list
		if (path.equals("/"))
		{
			Iterator iterator=Configuration.getSharesIterator();
			while (iterator.hasNext())
			{
				out.write(IS_DIRECTORY);
				out.println(iterator.next());
			}
		}
		else
		{
			String share=path.substring(1, path.substring(1).indexOf("/")+1);
			String fsPath=Configuration.getSharePath(share)+path.substring(share.length()+1);									
			String files[]=new File(fsPath).list();

            if (files != null)
            {
			  int i;
			  for (i=0; i<files.length; i++)
			  {
				if (new File(fsPath+files[i]).isFile())
					out.write(IS_FILE);
				else
					out.write(IS_DIRECTORY);
				out.println(files[i]);
		  	   }
            }
        }
		
		// terminator
		out.write(TERMINATOR);
		out.println("");
	}
	
	public void requestSend(BufferedReader in)
	{
		try
		{
			DataOutputStream out=new DataOutputStream(socket.getOutputStream());			
			String path=in.readLine();
			String file=in.readLine();			
			long totalread=0, size=Long.parseLong(in.readLine());			
			DataOutputStream fileout=new DataOutputStream(new FileOutputStream(file));
			
			// KLUDGE, REMOVE AND PUT REAL CODE
			out.write(OK);
						
			while (totalread<size)
			{
				DataInputStream datain=new DataInputStream(socket.getInputStream());
				byte[] buffer=new byte[1024];
				int read=datain.read(buffer);
				fileout.write(buffer, 0, read);
				totalread+=read;
			}
			fileout.close();
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		
	}
	
	public void requestMakeDirectory(BufferedReader in)
	{
		try
		{
			DataOutputStream out=new DataOutputStream(socket.getOutputStream());
			String path=in.readLine();
			String directory=in.readLine();
			String share=path.substring(1, path.substring(1).indexOf('/')+1);
			
			path=Configuration.getSharePath(share)+path.substring(share.length()+2)+directory;
			
			File file=new File(path);
			if (file.isDirectory() || file.mkdirs())
				out.write(OK);
			else
				out.write(NO);			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
