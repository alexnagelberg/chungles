import org.eclipse.swt.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import java.util.*;

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
		ListIterator iterator=client.listDir(path).listIterator();
		client.close();
		
		while (iterator.hasNext())
		{
			String name=(String)iterator.next();			
			TreeItem child=new TreeItem(node, SWT.NONE);
			child.setText(name.substring(1));
			if (name.charAt(0)==ServerConnectionThread.IS_DIRECTORY)
			{
				child.setImage(new Image(SWTUtil.getInstance().getShell().getDisplay(), "folder.gif"));
				new TreeItem(child, SWT.NONE);
			}
			else
			{
				child.setImage(new Image(SWTUtil.getInstance().getShell().getDisplay(), "file.gif"));
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
		return node.getText();
	}
}
