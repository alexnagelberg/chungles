import java.io.File;

public class FileList
{
	private final static int DIRECTORY=0;
	private final static int FILE=1;
	
	private String remotePath, localPath;
	private int fileType;
	private long size;
	private FileList next=null, previous=this, head;
	
	public FileList()
	{
		this.head=this;
	}
	
	public FileList(FileList head)
	{
		this.head=head;
	}
	
	public void setNext(FileList next)
	{
		this.next=next;
	}
	
	public FileList getNext()
	{
		return next;
	}
	
	public void setPrev(FileList previous)
	{
		this.previous=previous;
	}
	
	public FileList getPrev()
	{
		return previous;
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
	
	private void setHead(FileList head)
	{
		this.head=head;
	}
	
	private FileList getHead()
	{
		return head;
	}
	
	public static FileList recurseFiles(String[] files)
	{
		int i;
		FileList list=null;
		FileList head=null;
		for (i=0; i<files.length; i++)
		{
			if (list==null)
			{
				list=new FileList();
				head=list;
			}
			else
			{
				FileList templist=new FileList(list.getHead());
				list.setNext(templist);
				templist.setPrev(list);
				list=templist;
			}
			list.setLocalPath(files[0]);
			list.setSize(new File(files[0]).length());
			list.setRemotePath(files[0].substring(files[0].lastIndexOf('/')));
			if (new File(files[0]).isFile())
			{
				list.setFileType(FILE);
			}
			else
			{
				list.setFileType(DIRECTORY);
				FileList next=recurseFiles(list);
				list.setNext(next.getHead());
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
		FileList head=list;
		for (i=0; i<files.length; i++)
		{
			FileList item=new FileList(head);
			list.setNext(item);
			item.setPrev(list);			
			item.setLocalPath(files[0].getAbsolutePath());
			int offset=list.getLocalPath().length()-list.getRemotePath().length();
			item.setRemotePath(files[0].getAbsolutePath().substring(offset));
			item.setSize(files[0].length());
			if (files[0].isFile())
			{
				item.setFileType(FILE);
				list=item;
			}
			else
			{
				item.setFileType(DIRECTORY);
				FileList next=recurseFiles(item);
				item.setNext(next.getHead());
				list=next;
			}			
		}
		return list;
	}
}
