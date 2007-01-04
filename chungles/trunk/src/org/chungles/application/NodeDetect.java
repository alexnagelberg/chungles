package org.chungles.application;

import java.util.*;
import javax.jmdns.*;
import org.chungles.plugin.*;

public class NodeDetect implements ServiceListener
{
	public static Hashtable<String, String> ips;
	public static Hashtable<String, String> compnames; // provides reverse lookup
	
	public static void addNode(final String ip, final String compname)
	{
	    if (ips==null || compnames==null)
		{					
			ips=new Hashtable<String, String>();
			compnames=new Hashtable<String, String>();
		}

        if (!compnames.contains(compname))
        {
            ips.put(compname, ip);
            compnames.put(ip, compname);
            PluginAction.addNode(ip, compname);
        }
	    
	    
	}
	
	public void addService(JmDNS mdns, String type, String name)
	{			    
		ServiceInfo service=mdns.getServiceInfo(type, name);		
        final String ip=name.substring(0, name.length() - (type.length() + 1));
        final String compname=service.getNiceTextString()+" [" + ip + "]";
        
        addNode(ip, compname);
	}

	public void removeService(JmDNS mdns, String type, String name)
	{		    
		final String ip=name.substring(0, name.length() - (type.length() + 1));
		final String compname=(String)compnames.get(ip);
				
		if (ips==null || compnames==null)
		{			
			ips=new Hashtable<String, String>();
			compnames=new Hashtable<String, String>();
		}
		if (name==null)
            return;
        
		if (compnames.containsKey(ip))
		{
	        compnames.remove(ip);
	        ips.remove(compname);
	        PluginAction.removeNode(ip, compname);
		}
	}

	public void resolveService(JmDNS mdns, String type, String name, ServiceInfo service)
	{		
	}
	
	public static String getIP(String compname)
	{
	    return ips.get(compname);
	}	
}
