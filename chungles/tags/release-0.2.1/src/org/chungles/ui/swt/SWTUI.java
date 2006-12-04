package org.chungles.ui.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import java.io.*;
import java.util.Hashtable;

import org.chungles.ui.UI;


public class SWTUI implements UI
{
	private SWTUtil swtutil;
	private static Hashtable<String, TreeItem> nodes;
	
	public SWTUI()
	{		
		swtutil=SWTUtil.getInstance();
		nodes=new Hashtable<String, TreeItem>();
	}
	
	public boolean takeoverWaitsForInterfaces()
	{
		return false;
	}
	
	public void takeover()
	{		
		swtutil.mainLoop();
	}
	
	public void addNode(final String IP, final String compname, final Hashtable<String, String> ips, final Hashtable<String, String> compnames)
	{
        final Tree tree=swtutil.getTree();
		swtutil.getShell().getDisplay().syncExec(new Runnable()
		{			
			public void run()
			{
				if (!nodes.containsKey(compname))
				{
					TreeItem node=new TreeItem(tree, SWT.NONE);
					node.setText(compname);
					new TreeItem(node, SWT.NONE);
					InputStream in=ClassLoader.getSystemClassLoader().getResourceAsStream("images/node.gif");	  	    	   
				    node.setImage(new Image(swtutil.getShell().getDisplay(), in));
				    nodes.put(compname, node);
				    ips.put(compname, IP);
		    		compnames.put(IP, compname);
				}
			}
		});
	}
	
	public void removeNode(final String IP, final String compname, final Hashtable<String, String> ips, final Hashtable<String, String> compnames)
	{		
		swtutil.getShell().getDisplay().syncExec(new Runnable()
		{
			public void run()
			{
                if (compname==null)
                    return;
                
				final TreeItem node=nodes.get(compname);
				if (node!=null)
				{
					node.dispose();
					nodes.remove(compname);
					ips.remove(compname);
					compnames.remove(IP);
				}
			}
		});
	}
	
	public void openPreferencesDialog()
	{
		SWTPreferencesDialog dialog=SWTPreferencesDialog.getInstance(swtutil.getShell().getDisplay());
		dialog.openDialog();
	}
}
