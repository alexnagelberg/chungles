package org.chungles.core;

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
	public final static int REQUEST_RECEIVE=5;
	public final static int OK=4;
	public final static int YES=4;
	public final static int NO=5;
	public final static int RECURSE_FILES=6;
	public final static int PATH_EXISTS=7;
	public final static int CHECK_PROTOCOL_VERSION=8;
	public final static int REQUEST_DELETE=9;
	public final static int BEGIN_MULTICAST=10;
	public final static int RECOVER_PACKETS=11;	
	
	public final static char IS_FILE='F';
	public final static char IS_DIRECTORY='D';
	
    private final static int PACKET_SIZE=1024;
    
	public final static Version PROTOCOL_VERSION=new Version(0,3,0);
	
    private InputStream in;
    private OutputStream out;    
    
	public ServerConnectionThread(Socket socket)
	{
		super("ServerConnectionThread");
		this.socket=socket;
        try
        {
            in=socket.getInputStream();
            out=socket.getOutputStream();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
	}
	
	public void run()
	{
		try
		{			
			DataInputStream din = new DataInputStream(in);
			
			int command=din.read();
			while (command!=-1)
			{				
				executeCommand(command);
				command=din.read();
			}
			socket.close();			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	private void executeCommand(int command)
	{
		try
		{		
			switch(command)
			{
				case LIST_SHARES:
				{
					listShares();
					break;
				}
				case REQUEST_SEND:
				{
					requestSend();
					break;
				}
				case REQUEST_RECEIVE:
				{
				    requestReceive();
				    break;
				}
				case REQUEST_MKDIR:
				{
					requestMakeDirectory();
					break;
				}
				case RECURSE_FILES:
				{
				    recurseFiles();
				    break;
				}
				case PATH_EXISTS:
				{
					checkPath();
					break;
				}
				case CHECK_PROTOCOL_VERSION:
				{
					returnVersion();
					break;
				}
				case REQUEST_DELETE:
				{
					deleteFile();
					break;
				}
                case RECOVER_PACKETS:
                {
                    if (mServer.isFallbackEnabled())
                        recoverPackets();
                    break;
                }
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void listShares() throws IOException
	{
        BufferedReader bin=new BufferedReader(new InputStreamReader(in));
        DataOutputStream dout=new DataOutputStream(out);
		String path=bin.readLine();
		
		// Send root list
		if (path.equals("/"))
		{
			Iterator<String> iterator=Configuration.getSharesIterator();
			while (iterator.hasNext())
			{
				dout.write(IS_DIRECTORY);
				dout.writeBytes(iterator.next()+"\n");
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
					dout.write(IS_FILE);
				else
					dout.write(IS_DIRECTORY);
				dout.writeBytes(files[i]+"\n");
		  	   }
            }
        }
		
		// terminator
		dout.write(TERMINATOR);
		dout.writeBytes("\n");
	}
	
	/**
	 * Client requests a file from server thread
	 */
	public void requestReceive()
	{
        BufferedReader bin=new BufferedReader(new InputStreamReader(in));
	    try
	    {
	    	String separator=System.getProperty("file.separator");
	        String path=bin.readLine();
	        String share=path.substring(1, path.substring(1).indexOf('/')+1);			
			path=Configuration.getSharePath(share)+separator+path.substring(share.length()+2);
									
	        // KLUDGE, REMOVE AND PUT REAL CODE
			if (validPath(path))
				out.write(OK);
			else
			{
				out.write(NO);
				return;
			}			
			
            try
            {                       
                DataInputStream filein=new DataInputStream(new FileInputStream(path));
                byte[] buffer=new byte[1024];
                int read;
                while ((read = filein.read(buffer)) != -1)
                {
                    out.write(buffer, 0, read);
                }
                filein.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }            			
	    }
	    catch (Exception e)
	    {
	        e.printStackTrace();
	    }
	}
	
	/**
	 * Client requests to send to server thread 
	 */
	public void requestSend()
	{
        BufferedReader bin=new BufferedReader(new InputStreamReader(in));
		try
		{			
			String path=bin.readLine();
			String share=path.substring(1, path.substring(1).indexOf('/')+1);			
			path=Configuration.getSharePath(share)+"/"+path.substring(share.length()+2);
			String file=bin.readLine();			
			long totalread=0, size=Long.parseLong(bin.readLine());			
			DataOutputStream fileout=new DataOutputStream(new FileOutputStream(path+file));
			
			// KLUDGE, REMOVE AND PUT REAL CODE
			if (validPath(path))
				out.write(OK);
			else
			{
				out.write(NO);
				return;
			}
						
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
	
	public void requestMakeDirectory()
	{
        BufferedReader bin=new BufferedReader(new InputStreamReader(in));
		try
		{
			String path=bin.readLine();
			String directory=bin.readLine();            
			String share=path.substring(1, path.substring(1).indexOf('/')+1);
			
			path=Configuration.getSharePath(share)+path.substring(share.length()+2)+directory;
			
			File file=new File(path);
			if (validPath(path) && ((file.isDirectory() && !file.isFile()) || file.mkdirs()))
				out.write(OK);
			else
				out.write(NO);			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void recurseFiles() throws IOException
	{
        BufferedReader bin=new BufferedReader(new InputStreamReader(in));
        DataOutputStream dout=new DataOutputStream(out);
        
	    String path=bin.readLine();
		String share=path.substring(1, path.substring(1).indexOf('/')+1);			
		path=Configuration.getSharePath(share)+"/"+path.substring(share.length()+2);
		if (!validPath(path))
		{
			dout.write(TERMINATOR);
			dout.writeBytes("\n");
			return;
		}
		
		FileList list=FileList.recurseFiles(new String[] {path});
		while (list!=null)
		{
		    String localPath=list.getLocalPath();
		    if (File.separatorChar!='/')
		    	localPath=localPath.replace(File.separatorChar, '/');
			dout.writeBytes("/"+share+localPath.
		            substring(Configuration.getSharePath(share).length())+"\n");
			dout.writeBytes(list.getFileType()+"\n");
		    dout.writeBytes(list.getSize()+"\n");
		    list=list.getNext();
		}
		dout.write(TERMINATOR);
		dout.writeBytes("\n");
	}
	
	/**
	 * Checks to see if path exists on server.
	 * 
	 * @throws IOException
	 */
	private void checkPath() throws IOException
	{
		BufferedReader bin=new BufferedReader(new InputStreamReader(in));		
		String path=bin.readLine();
		StringTokenizer tok=new StringTokenizer(path, "/");
		String share=tok.nextToken();
		if (!tok.hasMoreTokens())
		{
			if (Configuration.getSharePath(share)!=null)
				out.write(YES);
			else
				out.write(NO);
		}
		else
		{		
			path=Configuration.getSharePath(share)+"/"+path.substring(share.length()+2);

			if (validPath(path) && new File(path).exists())
				out.write(YES);
			else
				out.write(NO);
		}
	}
	
	private void returnVersion() throws IOException
	{
		DataOutputStream dout=new DataOutputStream(out);
		dout.writeBytes(PROTOCOL_VERSION+"\n");
	}
	
	private void deleteFile() throws IOException
	{
		BufferedReader bin=new BufferedReader(new InputStreamReader(in));
		String path=bin.readLine();
		String share=path.substring(1, path.substring(1).indexOf('/')+1);			
		path=Configuration.getSharePath(share)+"/"+path.substring(share.length()+2);
						
		if (validPath(path) && recurseDelete(new File(path)))
			out.write(OK);
		else
			out.write(NO);
	}
	
	private boolean recurseDelete(File file)
	{		
		if (file.isDirectory())
		{
			File[] files=file.listFiles();
			int i;
			boolean ok=true;
			for (i=0; i<files.length; i++)
				if (!recurseDelete(files[i])) ok=false;
			return ok && file.delete();
		}
		else
		{
			return file.delete();
		}
	}
	
	private boolean validPath(String path)
	{
		StringTokenizer tok=new StringTokenizer(path,"/");
		while (tok.hasMoreTokens())
		{
			String curtok=tok.nextToken();
			if (curtok.equals(".."))
				return false;
		}
		return true;
	}
    
    private void recoverPackets() throws IOException
    {
        byte[] buf=new byte[4];
        in.read(buf, 0, 4);
        int numPackets=Util.byteToInt(buf);
        FileInputStream fin=new FileInputStream(mServer.getFileName());
        
        for (int i=0; i<numPackets; i++)
        {
            buf=new byte[8];
            byte[] outbuf=new byte[PACKET_SIZE];
            in.read(buf, 0, 8);
            long offset=Util.byteToLong(buf);
            fin.getChannel().position(offset);
            int length=fin.read(outbuf, 0, PACKET_SIZE);
            out.write(outbuf, 0, length);
        }
    }
}
