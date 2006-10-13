package org.chungles.ui.console;

import org.chungles.ui.UI;
import org.chungles.core.*;

import java.io.*;
import java.util.*;

public class ConsoleUI implements UI
{	
	private String workingpath="/";
	private Hashtable<String, String> ips, compnames;
	private Client client;
	
	public void takeover()
	{
		String input="";
		while (!input.equals("quit"))
		{
			System.out.print("chungles> ");
			try
			{
				input=new BufferedReader(new InputStreamReader(System.in)).readLine();
				parseCommand(input);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void addNode(String IP, String compname, Hashtable<String, String> ips, Hashtable<String, String> compnames)
	{
		if (!ips.containsKey(compname))
		{
		    ips.put(compname, IP);
    		compnames.put(IP, compname);
    		this.ips=ips;
    		this.compnames=compnames;
		}		
	}
	
	public void removeNode(String IP, String compname, Hashtable<String, String> ips, Hashtable<String, String> compnames)
	{
		if (compname==null)
			return;
		
		ips.remove(compname);
		compnames.remove(compname);
		this.ips=ips;
		this.compnames=compnames;
	}
	
	public void openPreferencesDialog()
	{
		
	}
	
	private void parseCommand(String cmd)
	{
		StringTokenizer tok=new StringTokenizer(cmd);
        int numTokens=tok.countTokens();
		String firstTok=tok.nextToken();
		if (cmd.equals("quit"))
			System.out.println("kthxbai");
		else if (cmd.equals("help"))
			printHelp();
		else if (cmd.equals("ls"))
			listPath();
		else if (firstTok.equals("get"))
		{
			if (numTokens>=2)		
				getFile(tok.nextToken("\n").trim());
			else
				System.out.println("Syntax: get <file>");
		}
		else if (firstTok.equals("put"))
		{
			if (numTokens>=2)
				putFile(tok.nextToken("\n").trim());
			else
				System.out.println("Syntax: put <file>");
		}
		else if (firstTok.equals("cd"))
		{
			if (numTokens>=2)
				changeDirectory(tok.nextToken("\n").trim());
			else
				System.out.println("Syntax: cd <directory> (" + numTokens + ")");
		}
		else
			System.out.println("Unrecognized command.");
		System.out.println();
	}
	
	private void printHelp()
	{
		
	}
	
	private void listPath()
	{
		System.out.println("Path - " + workingpath);
		if (workingpath.equals("/"))
		{
			Enumeration<String> compenum=compnames.elements();
			while(compenum.hasMoreElements())
			{				
				String name=compenum.nextElement();
				System.out.println(name);
			}
		}
		else
		{
			if (client!=null)
			{
				int start=workingpath.indexOf('/', 1);
				LinkedList<String> listing;
				if (start==-1)
					listing=client.listDir("/");
				else
				{
					String path=workingpath.substring(start);
					if (path.charAt(path.length()-1)!='/')
						path+="/";
					listing=client.listDir(path);
				}
				
				ListIterator<String> iter=listing.listIterator();
				while (iter.hasNext())
				{
					String name=iter.next();
					if (name.charAt(0)==ServerConnectionThread.IS_DIRECTORY)
						System.out.print("(D) ");
					else
						System.out.print("(F) ");
					System.out.println(name.substring(1));
				}
			}
		}
	}
	
	private void changeDirectory(String path)
	{
		if (path.substring(0,1).equals("/")) // Absolute path
		{
			changeAbsolutePath(path);
		}
		else // Relative path
		{		
			/* 
			 * This piece of code was fun. It tokenizes the current working path
			 * with / as the delimiting character and pushes each token in to a stack.
			 * Then it tokenizes the relative path, and checks each token. If the token
			 * is a "..", it pops a string off the stack. Otherwise it adds the token
			 * to the stack. It then pops the whole stack to an absolute path name. ^_^
			 */
			Stack<String> pathstack=new Stack<String>();
			StringTokenizer tok=new StringTokenizer(workingpath, "/");
			while (tok.hasMoreElements())
			{
				pathstack.push(tok.nextToken());
			}
			
			tok=new StringTokenizer(path, "/");
			while (tok.hasMoreElements())
			{
				String curTok=tok.nextToken();
				if (curTok.equals("..") && !pathstack.isEmpty())					
					pathstack.pop();
				else
					pathstack.push(curTok);
			}
			
			path="";
			while (!pathstack.isEmpty())
			{
				path=pathstack.pop() + "/" + path;
			}
			
			path="/"+path;
			changeAbsolutePath(path);
		}
	}
	
	private void changeAbsolutePath(String path)
	{
		if (client!=null)
		{
			client.close();
			client=null;
		}
		
		if (path.length()>1)
		{
			
			StringTokenizer tok=new StringTokenizer(path.substring(1),"/");						
			String compname=tok.nextToken();				
			client=new Client(ips.get(compname));
			if (!tok.hasMoreElements() || client.pathExists(path.substring(compname.length()+1)))
			{
				workingpath=path;
				System.out.println("Changed to " + path);
			}
			else
			{
				System.out.println("Invalid path.");
			}
		}
		else if (path.equals("/"))
			workingpath=path;
		else
			System.out.println("Invalid path.");
	}
	
	private void getFile(String file)
	{
		String path = (workingpath.charAt(workingpath.length()-1)=='/') ? 
				workingpath+file : workingpath+"/"+file;
        
        if (path.charAt(path.length()-1)!='/') // This is dumb, I apologize
            path+="/";
        
		path=path.substring(path.indexOf('/', 1));
		System.out.println(path);
		if (client!=null && client.pathExists(path))
		{
			System.out.println("Retrieving files...");
			FileList filelist=client.recurseFiles(path);
			if (!client.requestRetrieveFile(filelist, System.getProperty("user.dir")))
				System.out.println("Err");
			else
			{
				client.retrieveFile(System.getProperty("user.dir")+"/"+file, filelist, new ReceiveProgressListener()
				{
					private int hundredkb=0;
					public void progressUpdate(long bytesReceived)
					{
						int temp=(int)(bytesReceived/100000);
						if (temp>hundredkb)
							System.out.println((temp*100) + "K");
						hundredkb=temp;
					}
				});
			}
		}
		else
		{
			System.out.println("File not found");
		}
		
	}
	
	private void putFile(String file)
	{
		if (file==null || file.equals(""))
		{
			System.out.println("Syntax: put <file>");
			return;
		}
		
		StringTokenizer tok = new StringTokenizer(workingpath, "/");
		if (tok.countTokens()>=2)
		{
			String files[]={file};
			FileList list=FileList.recurseFiles(files);
			String path=tok.nextToken();
			path=workingpath.substring(path.length()+1);
			if (client.requestFileSend(path, list))
			{
				client.sendFile(list, new SendProgressListener()
				{
					private int hundredkb=0;
					public void progressUpdate(long bytesSent)
					{
						int temp=(int)(bytesSent/100000);
						if (temp>hundredkb)
							System.out.println((temp*100) + "K");
						hundredkb=temp;
					}
				});
			}
			else
			{
				System.out.println("There was an error sending.");
			}
		}
		else
		{
			System.out.println("You're not in a chungles share.");
		}
		}
	}
