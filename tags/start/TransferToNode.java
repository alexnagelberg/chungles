import java.io.*;
import java.util.*;

import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

public class TransferToNode extends DropTargetAdapter
{
    public void drop(DropTargetEvent event)
    {
    	String [] droppedFiles=(String [])event.data;       	
    	int i;
    	FileList list=FileList.recurseFiles(droppedFiles);
    	while (list!=null)
    	{
    		System.out.println(list.getLocalPath());
    		list=list.getNext();
    	}

    	TreeItem item=(TreeItem)event.item;
    	if (item.getParentItem()!=null) // Gotta have a path
    	{
    		String path=ShareLister.getPath(item);
    		String ip=ShareLister.getIP(item);
    		//long totalSize=calculateTotalSize(files);
    		SWTTransferDialog dialog=SWTTransferDialog.getInstance(SWTUtil.getInstance().getShell().getDisplay());
    		dialog.openDialog();
    		sendFiles(ip, path, droppedFiles);    		
    		    		
    	}    	    	    	
    }
    
    public File[] recurseFiles(File[] files)
    {    	
    	
    	if ((files.length==0) || (files.length==1 && files[0].isFile()))    		
    	{    		
    		return files;
    	}
    	    	
    	int i;
    	LinkedList list=new LinkedList(Arrays.asList(files));
    	for (i=0; i<files.length; i++)
    	{
    		if (files[i].isDirectory())
    		{    		    			
    			list.addAll(Arrays.asList(recurseFiles(files[i].listFiles())));
    		}
    			
    	}    	
    	return (File[])list.toArray(new File[list.size()]);
    }
    
    
    public long calculateTotalSize(File[] files)
    {
    	int i;
    	long size=0;
    	for (i=0; i<files.length; i++)
    	{    		
    		size+=files[i].length();
    	}
    	return size;
    }
    
    private void sendFiles(final String ip, final String path, final String[] files)
    {    	
    	Thread thread=new Thread()
		{
    		public void run()
    		{
    			Display display=SWTUtil.getInstance().getShell().getDisplay();
    			SWTTransferDialog dialog=SWTTransferDialog.getInstance(display);
		    	Client client=new Client(ip);		    	
		    	int i;
		    	for (i=0; i<files.length; i++)
		    	{
		    		int relativeoffset=files[i].substring(0,files[i].lastIndexOf('/')).length();
		    		
		    		
		    		/*
		    		if (client.requestFileSend(path, files[i]))
		    		{    			
		    			final long fileSize=files[i].length();
		    			dialog.updateLables(files[i].getAbsolutePath(), i+1, files.length);
		    			client.sendFile(files[i].getAbsolutePath(), new SendProgressListener()
		    					{
		    						public void progressUpdate(long bytesSent)
		    						{
		    							dialog.updateProgress(bytesSent, fileSize, totalSize);		    							
		    						}
		    					});
		    		}
		    		else
		    		{
		    			// Error, cannot send, abort
		    			System.out.println("ERR SENDING");
		    			dialog.closeDialog();
		    			return;
		    		}*/
		    	}
		    	client.close();
		    	dialog.closeDialog();
    		}
		};
		thread.start();
    }
}
