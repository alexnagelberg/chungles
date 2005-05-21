import java.io.InputStream;
import java.util.*;
import javax.jmdns.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

public class NodeDetect implements ServiceListener
{
	private static Hashtable nodes;
	private static Hashtable ips;
	private static Hashtable compnames; // provides reverse lookup
	
	public void addService(JmDNS mdns, String type, String name)
	{			    
		ServiceInfo service=mdns.getServiceInfo(type, name);
		final SWTUtil swt=SWTUtil.getInstance();
		if (swt == null)
            return;
        final String ip=name.substring(0, name.length() - (type.length() + 1));
        final String compname=service.getNiceTextString()+" [" + ip + "]";
		final Tree tree=swt.getTree();				
		if (!swt.isActive())
			return;
						
		if (nodes==null || ips==null || compnames==null)
		{
			nodes=new Hashtable();		
			ips=new Hashtable();
			compnames=new Hashtable();
		}
		
		swt.getShell().getDisplay().syncExec(new Runnable()
				{
					public void run()
					{
						if (!nodes.containsKey(compname))
						{
							TreeItem node=new TreeItem(tree, SWT.NONE);
							node.setText(compname);
							new TreeItem(node, SWT.NONE);
							InputStream in=ClassLoader.getSystemClassLoader().getResourceAsStream("images/node.gif");	  	    	   
						    node.setImage(new Image(swt.getShell().getDisplay(), in));																																	
							nodes.put(compname, node);
							ips.put(compname, ip);
							compnames.put(ip, compname);
						}
					}
				});
	}

	public void removeService(JmDNS mdns, String type, String name)
	{		    
		final SWTUtil swt=SWTUtil.getInstance();
        if (swt == null)
            return;
		final String ip=name.substring(0, name.length() - (type.length() + 1));
		final String compname=(String)compnames.get(ip);
		final Tree tree=swt.getTree();				
		if (!swt.isActive())
			return;
						
		if (nodes==null || ips==null || compnames==null)
		{
			nodes=new Hashtable();
			ips=new Hashtable();
			compnames=new Hashtable();
		}
		
		swt.getShell().getDisplay().syncExec(new Runnable()
				{
					public void run()
					{
						TreeItem node=(TreeItem)nodes.get(compname);
						if (node!=null)
						{
							node.dispose();
							nodes.remove(compname);
							ips.remove(compname);
							compnames.remove(ip);
						}
					}
				});
		
	}

	public void resolveService(JmDNS mdns, String type, String name, ServiceInfo service)
	{		
	}
	
	public static String getIP(String compname)
	{
	    return (String)ips.get(compname);
	}
}
