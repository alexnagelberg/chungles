import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

public class TransferToNode extends DropTargetAdapter implements SelectionListener
{	
	private static long totalSent;

    public void widgetDefaultSelected(SelectionEvent e) {}
    
    public void widgetSelected(SelectionEvent e)
    {
        Shell shell=SWTUtil.getInstance().getShell();
        FileDialog fileDialog=new FileDialog(shell, SWT.OPEN | SWT.MULTI);        
        fileDialog.setText("Choose files to send");
        if (fileDialog.open()==null)
            return;
        
        String files[]=fileDialog.getFileNames();
        String separator=System.getProperty("file.separator");
        int i;
        for (i=0; i<files.length; i++)
        {
            files[i]=fileDialog.getFilterPath()+separator+files[i];
        }
        
        SWTTransferDialog dialog=SWTTransferDialog.getInstance(SWTUtil.getInstance().getShell().getDisplay());
        dialog.openDialog();
        
        TreeItem item=SWTUtil.getInstance().getTree().getSelection()[0];
        String path=ShareLister.getPath(item);
        String ip=ShareLister.getIP(item);
        sendFiles(ip, path, files);
    }
    
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
