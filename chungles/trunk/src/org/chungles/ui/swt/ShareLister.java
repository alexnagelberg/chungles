package org.chungles.ui.swt;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import java.io.InputStream;
import java.util.*;

import org.chungles.core.*;
import org.chungles.application.*;

public class ShareLister implements Listener
{
	public void handleEvent(Event event)
	{				
		TreeItem node=(TreeItem)event.item;
		TreeItem children[]=node.getItems();
		int i;
		for (i=0; i<children.length; i++)
			children[i].dispose();
				
		String path=getPath(node);					
		String ip=getIP(node);
		Client client=new Client(ip);
		ListIterator<String> iterator=client.listDir(path).listIterator();
		client.close();
		
		while (iterator.hasNext())
		{
			String name=iterator.next();			
			TreeItem child=new TreeItem(node, SWT.NONE);
			child.setText(name.substring(1));
			if (name.charAt(0)==ServerConnectionThread.IS_DIRECTORY)
			{
			    InputStream in=ClassLoader.getSystemClassLoader().getResourceAsStream("images/folder.gif");	  	    	   			    
				child.setImage(new Image(SWTUtil.getInstance().getShell().getDisplay(), in));
				new TreeItem(child, SWT.NONE);
			}
			else
			{
			    InputStream in=ClassLoader.getSystemClassLoader().getResourceAsStream("images/file.gif");	
				child.setImage(new Image(SWTUtil.getInstance().getShell().getDisplay(), in));
			}
		}
	}
	
	public static String getPath(TreeItem node)
	{
		String path="/";
		while (node.getParentItem()!=null)
		{
			path="/"+node.getText()+path;
			node=node.getParentItem();
		}
		return path;
	}
	
	public static String getIP(TreeItem node)
	{
		while (node.getParentItem()!=null)
			node=node.getParentItem();
		return NodeDetect.getIP(node.getText());
	}
}
