import java.io.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.*;

public class TransferFromNode extends DragSourceAdapter
{
	private File tempDirFile;
	private TreeItem[] items;    
    private String[] filesMoved;
    
	public void dragStart(DragSourceEvent event)
	{
		items=SWTUtil.getInstance().getTree().getSelection();        
        
        try
        {            
            File file=File.createTempFile("chungles","", new File(Configuration.getTemporaryFolder()));
            String tempdir=file.getAbsolutePath();
            file.delete();
            tempDirFile=new File(tempdir);
            file.mkdir();
            event.doit=true;
        }
        catch (Exception e)
        {
            event.doit=false;
            System.err.println("could not write to tempdir");
            e.printStackTrace();
        }
	}
	
    public void dragSetData(DragSourceEvent event)
    {   
        int i;    	
        Display display=SWTUtil.getInstance().getShell().getDisplay();
        SWTTransferDialog dialog=SWTTransferDialog.getInstance(display);
        dialog.openDialog();
        
    	FileList lists[]=new FileList[items.length];
        String IPs[]=new String[items.length];
        int numFiles=0;
        int totalSize=0;
        for (i=0; i<items.length; i++)
        {
            IPs[i]=ShareLister.getIP(items[i]);
            Client client=new Client(IPs[i]);            
            lists[i]=client.recurseFiles(ShareLister.getPath(items[i]));
            numFiles+=FileList.getNumFiles();
            totalSize+=FileList.getTotalSize();
            client.close();                        
        }
        filesMoved=new String[numFiles];
        getFiles(lists, IPs, numFiles, totalSize);
        //event.data=filesMoved;
        event.data=new String []{tempDirFile.getAbsolutePath()+"/test"};
        try
        {
            new File(tempDirFile.getAbsolutePath()+"/test").createNewFile();
        }
        catch (Exception e)
        {
            
        }
    }
    
    public void dragFinished(DragSourceEvent event)
    {
    	tempDirFile.delete();        
    }
    
    private void getFiles(final FileList[] lists, final String[] IPs, final int numFiles, final int totalSize)
    {
        Thread thread=new Thread()
        {
            public void run()
            {                
                int curFile=0;
                Display display=SWTUtil.getInstance().getShell().getDisplay();
    			final SWTTransferDialog dialog=SWTTransferDialog.getInstance(display);                
                int i;
                dialog.progressThread();
                for (i=0; i<lists.length; i++)
                {
                    FileList list=lists[i];
                    Client client=new Client(IPs[i]);                    
                    int offset=list.getRemotePath().lastIndexOf('/');
                    
                    while (list.getRemotePath()!=null)
                    {                        
                        String savePath=tempDirFile.getAbsolutePath()+list.getRemotePath().substring(offset);
                        //filesMoved[curFile++]=savePath;                        
                        if (client.requestRetrieveFile(list))
                        {
                            dialog.updateLables(list.getRemotePath(), curFile, numFiles);
                            client.retrieveFile(savePath, list, new ReceiveProgressListener()
                                    {
                                        public void progressUpdate(long bytesReceived)
                                        {
                                            
                                        }
                                    });
                        }
                        else
                        {
                            // Error, cannot get, abort
                            System.out.println("ERR RETRIEVING");
                            dialog.closeDialog();
                            return;
                        }
                        list=list.getNext();                
                    }
                    client.close();                    
                }
                dialog.closeDialog();
            }
        };
        thread.start();
    }
}
