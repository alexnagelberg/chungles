package org.chungles.core;

import java.net.*;
import java.io.*;
import java.util.*;

public class Client
{
	private Socket socket;
	private OutputStream out;
	private InputStream in;
	
	private Client()
	{
		
	}
	
	public Client(String ip)
	{
		try
		{
			socket=new Socket(ip, 6565);
			in=socket.getInputStream();
			out=socket.getOutputStream();
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
        DataOutputStream dout=new DataOutputStream(out);
        BufferedReader bin=new BufferedReader(new InputStreamReader(in));
		LinkedList list=new LinkedList();		
		try
		{
			dout.write(ServerConnectionThread.LIST_SHARES);
			dout.writeBytes(path+"\n");
			
			String inputLine;
			while ((inputLine = bin.readLine()) != null &&
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
        DataOutputStream dout=new DataOutputStream(out);        
		int status=ServerConnectionThread.NO;
		
		try
		{
			if (file.getFileType()==FileList.FILE)
			{
				dout.write(ServerConnectionThread.REQUEST_SEND);
				dout.writeBytes(path+"\n");
				dout.writeBytes(file.getRemotePath()+"\n");
				dout.writeBytes(file.getSize()+"\n");
			}
			else
			{
				dout.write(ServerConnectionThread.REQUEST_MKDIR);
				dout.writeBytes(path+"\n");
				dout.writeBytes(file.getRemotePath()+"\n");
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
	
	public boolean requestRetrieveFile(final FileList file, final String savePath)
	{
        DataOutputStream dout=new DataOutputStream(out);
	    int status=ServerConnectionThread.NO;
	    try
		{
			if (file.getFileType()==FileList.FILE)
			{
				dout.write(ServerConnectionThread.REQUEST_RECEIVE);				
				dout.writeBytes(file.getRemotePath()+"\n");
				return in.read()==ServerConnectionThread.OK;
			}
			else
			{
				File dir=new File(savePath);
				// If path exists, then:
				// Return success/fail if it's a directory or not
				// Otherwise, return the success/fail of creating that directory
				return dir.exists()?dir.isDirectory():dir.mkdir();
			}			
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
                listener.progressUpdate(totalread);
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
        DataOutputStream dout=new DataOutputStream(out);
        BufferedReader bin=new BufferedReader(new InputStreamReader(in));
	    try
	    {
		    dout.write(ServerConnectionThread.RECURSE_FILES);
		    dout.writeBytes(path.substring(0,path.length()-1)+"\n");
		    FileList list=new FileList();            
		    FileList head=list;		
		    String inputLine;
		    int numFiles=0;
            long totalSize=0; 
		    while ((inputLine = bin.readLine()) != null &&
		    	inputLine.toCharArray()[0]!=ServerConnectionThread.TERMINATOR)
		    {		        
		        list.setRemotePath(inputLine);
		        list.setFileType(Integer.parseInt(bin.readLine()));
		        list.setSize(Long.parseLong(bin.readLine()));
		        numFiles++;
                totalSize+=list.getSize();                
		        list.setNext(new FileList());
		        list=list.getNext();
		    }
            FileList.setNumFiles(numFiles);
            FileList.setTotalSize(totalSize);
		    return head;
		    
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	public boolean pathExists(String path)
	{
		DataOutputStream dout=new DataOutputStream(out);
		
		try
		{
			dout.write(ServerConnectionThread.PATH_EXISTS);
			dout.writeBytes(path+"\n");
			return (in.read()==ServerConnectionThread.YES);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
}
