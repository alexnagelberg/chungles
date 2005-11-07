import java.net.*;
import java.io.*;
import java.util.*;

public class Client
{
	private Socket socket;
	DataOutputStream out;
	DataInputStream in;
	
	private Client()
	{
		
	}
	
	public Client(String ip)
	{
		try
		{
			socket=new Socket(ip, 6565);
			in=new DataInputStream(socket.getInputStream());
			out=new DataOutputStream(socket.getOutputStream());
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
		try
		{
			out.write(ServerConnectionThread.LIST_SHARES);
			out.writeBytes(path+"\n");
			
			String inputLine;
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
	
	public boolean requestFileSend(String path, FileList file)
	{					
		
		int status=ServerConnectionThread.NO;
		
		try
		{
			if (file.getFileType()==FileList.FILE)
			{
				out.write(ServerConnectionThread.REQUEST_SEND);
				out.writeBytes(path+"\n");
				out.writeBytes(file.getRemotePath()+"\n");
				out.writeBytes(file.getSize()+"\n");
			}
			else
			{
				out.write(ServerConnectionThread.REQUEST_MKDIR);
				out.writeBytes(path+"\n");
				out.writeBytes(file.getRemotePath()+"\n");
			}
			status=in.read();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return status==ServerConnectionThread.OK;
	}
	
	public void sendFile(final FileList file, final SendProgressListener listener)
	{
		if (file.getFileType()==FileList.DIRECTORY)
		{
			listener.progressUpdate(file.getSize());
			return;
		}
		
		try
		{						
			DataInputStream filein=new DataInputStream(new FileInputStream(file.getLocalPath()));
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
		    e.printStackTrace();
		}					
	}
	
	public boolean requestRetrieveFile(final FileList file)
	{
	    int status=ServerConnectionThread.NO;
	    
	    try
		{
			if (file.getFileType()==FileList.FILE)
			{
				out.write(ServerConnectionThread.REQUEST_RECEIVE);				
				out.writeBytes(file.getRemotePath()+"\n");
			}
			else
			{
				// Make directory
			}
			status=in.read();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	    
	    return status==ServerConnectionThread.OK;
	}
	
	public void retrieveFile(final String savePath, final FileList file, final ReceiveProgressListener listener)
	{
	    if (file.getFileType()==FileList.DIRECTORY)
	    {
	        listener.progressUpdate(file.getSize());
			return;
	    }
	    
	    try
		{						
	        
			DataOutputStream fileout=new DataOutputStream(new FileOutputStream(savePath));			
			long totalread=0;			            
            long size=file.getSize();
            
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
	
	public FileList recurseFiles(String path)
	{
	    try
	    {
		    out.write(ServerConnectionThread.RECURSE_FILES);
		    out.writeBytes(path.substring(0,path.length()-1)+"\n");
		    FileList list=new FileList();
		    FileList head=list;		
		    String inputLine;
		    FileList.resetNumFiles();
		    FileList.resetTotalSize();
		    while ((inputLine = in.readLine()) != null &&
		    	inputLine.toCharArray()[0]!=ServerConnectionThread.TERMINATOR)
		    {		        
		        list.setRemotePath(inputLine);
		        list.setSize(Long.parseLong(in.readLine()));
		        FileList.setNumFiles(FileList.getNumFiles()+1);
		        FileList.setTotalSize(FileList.getTotalSize()+list.getSize());
		        list.setNext(new FileList());
		        list=list.getNext();
		    }
		    return head;
		    
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }
	    return null;
	}
}
