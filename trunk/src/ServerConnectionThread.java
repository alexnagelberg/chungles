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
	public final static int RECURSE_FILES=6;
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
			DataInputStream in = new DataInputStream(socket.getInputStream());
			
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
	
	private void executeCommand(int command, DataInputStream in)
	{
		try
		{
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());		
		
			switch(command)
			{
				case LIST_SHARES:
				{
					listShares(in, out);
					break;
				}
				case REQUEST_SEND:
				{
					requestSend(in, out);
					break;
				}
				case REQUEST_MKDIR:
				{
					requestMakeDirectory(in, out);
					break;
				}
				case RECURSE_FILES:
				{
				    recurseFiles(in, out);
				    break;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void listShares(DataInputStream in, DataOutputStream out) throws IOException
	{
		String path=in.readLine();
		
		// Send root list
		if (path.equals("/"))
		{
			Iterator iterator=Configuration.getSharesIterator();
			while (iterator.hasNext())
			{
				out.write(IS_DIRECTORY);
				out.writeBytes(iterator.next()+"\n");
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
				out.writeBytes(files[i]+"\n");
		  	   }
            }
        }
		
		// terminator
		out.write(TERMINATOR);
		out.writeBytes("\n");
	}
	
	public void requestSend(DataInputStream in, DataOutputStream out)
	{
		try
		{			
			String path=in.readLine();
			String share=path.substring(1, path.substring(1).indexOf('/')+1);			
			path=Configuration.getSharePath(share)+path.substring(share.length()+2);
			String file=in.readLine();			
			long totalread=0, size=Long.parseLong(in.readLine());			
			DataOutputStream fileout=new DataOutputStream(new FileOutputStream(path+file));
			
			// KLUDGE, REMOVE AND PUT REAL CODE
			out.write(OK);
						
			while (totalread<size)
			{
				byte[] buffer=new byte[1024];
				int read;
				if (size-totalread<1024)
				{
					//conversion of long to int ok, it's under 1024 ;)
					read=in.read(buffer, 0, (int)(size-totalread));
				}
				else
				{
					read=in.read(buffer);
				}
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
	
	public void requestMakeDirectory(DataInputStream in, DataOutputStream out)
	{
		try
		{
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
	
	private void recurseFiles(DataInputStream in, DataOutputStream out) throws IOException
	{
	    String path=in.readLine();
		String share=path.substring(1, path.substring(1).indexOf('/')+1);			
		path=Configuration.getSharePath(share)+"/"+path.substring(share.length()+2);
		FileList list=FileList.recurseFiles(new String[] {path});
		while (list!=null)
		{
		    out.writeBytes("/"+share+list.getLocalPath().
		            substring(Configuration.getSharePath(share).length())+"\n");
		    out.writeBytes(list.getSize()+"\n");
		    list=list.getNext();
		}
		out.write(TERMINATOR);
		out.writeBytes("\n");
	}
}
