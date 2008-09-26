package org.chungles.ui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import java.io.*;
import java.util.Hashtable;

import org.chungles.plugin.*;

public class SWTUI implements UIPlugin
{
	private SWTUtil swtutil;
	private static Hashtable<String, TreeItem> nodes;    	
    
	public String getAuthor()
    {
	    return "Alex Nagelberg";
    }
    
    public String getName()
    {
        return "SWT UI";
    }
    
    public String getVersion()
    {
        return "0.3";
    }
    
	public void init()
	{
        swtutil=SWTUtil.getInstance();
        nodes=new Hashtable<String, TreeItem>();
        swtutil.mainLoop();
        PluginAction.shutdown();        		
	}
	
    public void shutdown()
    {
        
    }
    
	public void addNode(final String IP, final String compname)
	{
        final Tree tree=swtutil.getTree();
		swtutil.getShell().getDisplay().asyncExec(new Runnable()
		{			
			public void run()
			{
				TreeItem node=new TreeItem(tree, SWT.NONE);
				node.setText(compname);
				new TreeItem(node, SWT.NONE);
				InputStream in=getClass().getClassLoader().getResourceAsStream("images/node.gif");	  	    	   
				node.setImage(new Image(swtutil.getShell().getDisplay(), in));
				nodes.put(compname, node);								
			}
		});
	}
	
	public void removeNode(final String IP, final String compname)
	{		
		swtutil.getShell().getDisplay().asyncExec(new Runnable()
		{
			public void run()
			{                
				final TreeItem node=nodes.get(compname);
				if (node!=null)
				{
					node.dispose();
					nodes.remove(compname);					
				}
			}
		});
	}
	
	public void openPreferencesDialog()
	{
		SWTPreferencesDialog dialog=SWTPreferencesDialog.getInstance(swtutil.getShell().getDisplay());
		dialog.openDialog();
	}
	
	public void finishnotification(final boolean success, final String message)
	{        
	    final Shell shell=swtutil.getShell();
        Display display=shell.getDisplay();
        
        display.syncExec(new Runnable()
        {
            public void run()
            {
        	    if (success)
                {
                    MessageBox box=new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
                    box.setMessage(message);
                    box.open();
                }
                else
                {
                    MessageBox box=new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                    box.setMessage(message);
                    box.open();
                }
            }
        });
	}

	public void notification(int type, String message) {
		// TODO Auto-generated method stub
		
	}
}
