package org.chungles.ui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.chungles.core.*;

public class SWTDeleteSelection implements SelectionListener
{
	public void widgetDefaultSelected(SelectionEvent e) {}
    
    public void widgetSelected(SelectionEvent e)
    {
    	Shell shell=SWTUtil.getInstance().getShell();
    	MessageBox box=new MessageBox(shell, SWT.ICON_QUESTION | SWT.NO | SWT.YES);
		box.setMessage("Are you sure you want to delete these files?");
		if (box.open()==SWT.YES)
		{
			// The fun begins, we must find all items, connect to client(s),
			// and send delete command.
			TreeItem[] items = SWTUtil.getInstance().getTree().getSelection();
			for (int i=0; i<items.length; i++)
			{
				String IP=ShareLister.getIP(items[i]);
				String file=ShareLister.getPath(items[i]);
				file=(file.charAt(file.length()-1)=='/')?file.substring(0, file.length()-1):file;
				
				Client client=new Client(IP);
				if (!client.deleteFile(file))
					System.out.println("Err deleting " + file + " from " + IP);
				else
				{
					items[i].dispose();
					SWTUtil.getInstance().deselectAllInTree();
				}
                client.close();
			}			
		}
    }
}
