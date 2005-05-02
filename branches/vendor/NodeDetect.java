import java.util.*;
import javax.jmdns.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

public class NodeDetect implements ServiceListener
{
	private static Hashtable nodes;
	
	public void addService(JmDNS mdns, String type, String name)
	{						
		ServiceInfo service=mdns.getServiceInfo(type, name);
		final SWTUtil swt=SWTUtil.getInstance();
		if (swt == null)
            return;
        final String ip=name.substring(0, name.length() - (type.length() + 1));
		final Tree tree=swt.getTree();				
		if (!swt.isActive())
			return;
						
		if (nodes==null)
			nodes=new Hashtable();		
		
		swt.getShell().getDisplay().syncExec(new Runnable()
				{
					public void run()
					{
						if (!nodes.containsKey(ip))
						{
							TreeItem node=new TreeItem(tree, SWT.NONE);
							node.setText(ip);
							new TreeItem(node, SWT.NONE);
							node.setImage(new Image(swt.getShell().getDisplay(), "node.gif"));																											
							nodes.put(ip, node);
						}
					}
				});
	}

	public void removeService(JmDNS mdns, String type, String name)
	{	
		ServiceInfo service=mdns.getServiceInfo(type, name);
		final SWTUtil swt=SWTUtil.getInstance();
        if (swt == null)
            return;
		final String ip=name.substring(0, name.length() - (type.length() + 1));
		final Tree tree=swt.getTree();				
		if (!swt.isActive())
			return;
						
		if (nodes==null)
			nodes=new Hashtable();		
		
		swt.getShell().getDisplay().syncExec(new Runnable()
				{
					public void run()
					{
						TreeItem node=(TreeItem)nodes.get(ip);
						if (node!=null)
						{
							node.dispose();
							nodes.remove(ip);
						}
					}
				});
		
	}

	public void resolveService(JmDNS mdns, String type, String name, ServiceInfo service)
	{		
	}	
}
