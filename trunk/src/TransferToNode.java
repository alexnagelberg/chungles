import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

public class TransferToNode extends DropTargetAdapter
{	
	private static long totalSent;
	
    public void drop(DropTargetEvent event)
    {
    	String [] droppedFiles=(String [])event.data;       	
    	TreeItem item=(TreeItem)event.item;
    	
    	if (item.getParentItem()!=null) // Gotta have a path
    	{
    		String path=ShareLister.getPath(item);
    		String ip=ShareLister.getIP(item);
    		SWTTransferDialog dialog=SWTTransferDialog.getInstance(SWTUtil.getInstance().getShell().getDisplay());
    		dialog.openDialog();
    		sendFiles(ip, path, droppedFiles);    		    		
    	}    	    	    	
    }
    
    private void sendFiles(final String ip, final String path, final String[] files)
    {    	
    	totalSent=0;
    	Thread thread=new Thread()
		{
    		public void run()
    		{
    			Display display=SWTUtil.getInstance().getShell().getDisplay();
    			final SWTTransferDialog dialog=SWTTransferDialog.getInstance(display);
		    	Client client=new Client(ip);		    	
		    	int i=0;
		    	FileList list=FileList.recurseFiles(files);
		    	dialog.progressThread();
		    	while (list!=null)
		    	{		    		
		    		i++;
		    		if (client.requestFileSend(path, list))
		    		{    			
		    			final long fileSize=list.getSize();
						final long totalSize=FileList.getTotalSize();
		    			dialog.updateLables(list.getLocalPath(), i, FileList.getNumFiles());
		    			client.sendFile(list, new SendProgressListener()
		    					{		    						
		    						private long lastSent=0;
		    						
		    						public void progressUpdate(long bytesSent)
		    						{
		    							totalSent+=bytesSent-lastSent;
		    							lastSent=bytesSent;
		    							dialog.updateProgress(bytesSent, fileSize, totalSent, totalSize);		    							
		    						}
		    					});
		    		}
		    		else
		    		{
		    			// Error, cannot send, abort
		    			System.out.println("ERR SENDING");
		    			dialog.closeDialog();
		    			return;
		    		}
		    		list=list.getNext();
		    	}
		    	client.close();
		    	dialog.closeDialog();
    		}
		};
		thread.start();
    }
}
