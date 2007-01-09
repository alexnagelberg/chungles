package org.chungles.ui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

import java.io.*;
import org.chungles.core.*;

public class MulticastTransfer implements SelectionListener
{

	public void widgetDefaultSelected(SelectionEvent e) {}
    
    public void widgetSelected(SelectionEvent e)
    {
    	Shell shell=SWTUtil.getInstance().getShell();
        FileDialog fileDialog=new FileDialog(shell, SWT.OPEN);        
        fileDialog.setText("Choose files to send");
        if (fileDialog.open()==null)
            return;
        
        String separator=System.getProperty("file.separator");
        final String file=fileDialog.getFilterPath()+separator+fileDialog.getFileNames()[0];
        final long filesize=new File(file).length();
        final String remotefile=file.substring(file.lastIndexOf(separator)+1);
        
        Display display=SWTUtil.getInstance().getShell().getDisplay();
        final SWTTransferDialog dialog=SWTTransferDialog.getInstance(display);
        dialog.openDialog(new AbortListener()
        {
            public boolean shouldCloseDialog()
            {
                return false;
            }
            
            public void abort()
            {
                
            }
        });
        Thread thread=new Thread()
        {
        	public void run()
        	{
                dialog.progressThread();
                dialog.updateLables(file, 1, 1);
                mServer mserver=new mServer(file, remotefile, new SendProgressListener()
                {        	
                	public void progressUpdate(long bytesSent)
                	{        		
                		dialog.updateProgress(bytesSent, filesize, bytesSent, filesize);
                	}
                });
                
                dialog.closeDialog();
        	}
        };
        thread.start();
        
    }
}
