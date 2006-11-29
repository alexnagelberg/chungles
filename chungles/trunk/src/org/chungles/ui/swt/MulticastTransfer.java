package org.chungles.ui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

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
        String file=fileDialog.getFilterPath()+separator+fileDialog.getFileNames()[0];
        
        
        
    }
}
