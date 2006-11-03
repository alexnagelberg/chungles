package org.chungles.core;

import java.io.File;

public class FileList
{
	public final static int DIRECTORY=0;
	public final static int FILE=1;
	
	private String remotePath, localPath;
	private int fileType;
	private static long totalSize=0;
	private static int numFiles=0;
	private long size;
	private FileList next=null;
	
	public FileList()
	{
	}
	
	public void setNext(FileList next)
	{
		this.next=next;
	}
	
	public FileList getNext()
	{
		return next;
	}
	
	public void setRemotePath(String path)
	{
		remotePath=path;
	}
	
	public String getRemotePath()
	{
		return remotePath;
	}
	
	public void setLocalPath(String path)
	{
		localPath=path;
	}
	
	public String getLocalPath()
	{
		return localPath;		
	}
	
	public void setSize(long size)
	{
		this.size=size;
	}
	
	public long getSize()
	{
		return size;
	}
	
	public void setFileType(int type)
	{
		fileType=type;
	}
	
	public int getFileType()
	{
		return fileType;
	}
	
	public static long getTotalSize()
	{
		return totalSize;
	}
	
	public static void resetTotalSize()
	{
		totalSize=0;
	}
	
	public static void setTotalSize(long size)
	{
	    totalSize=size;
	}
	
	public static int getNumFiles()
	{
		return numFiles;
	}
	
	public static void resetNumFiles()
	{
		numFiles=0;
	}
	
	public static void setNumFiles(int num)
	{
	    numFiles=num;
	}
	
	public static FileList recurseFiles(String[] files)
	{
		int i;
		resetTotalSize();
		resetNumFiles();
		FileList list=null;
		FileList head=null;
		for (i=0; i<files.length; i++)
		{
		    File f = new File(files[i]);
			if (list==null)
			{
				list=new FileList();
				head=list;
			}
			else
			{
				FileList templist=new FileList();
				list.setNext(templist);
				list=templist;
			}
			list.setLocalPath(files[i]);
			list.setSize(f.length());
			totalSize+=list.getSize();
			list.setRemotePath("/"+f.getName());
			numFiles++;
			if (f.isFile())
			{
				list.setFileType(FILE);
			}
			else
			{
				list.setFileType(DIRECTORY);
				FileList next=recurseFiles(list);				
				list=next;
			}
		}
		
		return head;
	}
	
	private static FileList recurseFiles(FileList list)
	{
		File[] files=new File(list.getLocalPath()).listFiles();
		if (files.length==0)    		
    	{    		
    		return null;
    	}	
		
		int i;		
		for (i=0; i<files.length; i++)
		{
			FileList item=new FileList();
			list.setNext(item);	
			
			item.setLocalPath(files[i].getAbsolutePath());
			int offset=list.getLocalPath().length()-list.getRemotePath().length();
			String remotePath=files[i].getAbsolutePath().substring(offset);
			if (File.separatorChar!='/') //win32 crap >:o
			    remotePath=remotePath.replace(File.separatorChar, '/');
			item.setRemotePath(remotePath);
			item.setSize(files[i].length());
			totalSize+=files[i].length();
			numFiles++;
			if (files[i].isFile())
			{
				item.setFileType(FILE);
				list=item;
			}
			else
			{
				item.setFileType(DIRECTORY);
				FileList next=recurseFiles(item);
				if (next!=null)
				{
					list=next;
				}
				else
				{
					list=item;
				}
			}			
		}
		return list;
	}
}
