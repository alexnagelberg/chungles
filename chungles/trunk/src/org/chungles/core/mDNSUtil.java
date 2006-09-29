package org.chungles.core;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.util.*;

import javax.jmdns.*;

public class mDNSUtil
{
	private static Hashtable<String, JmDNS> mdnslist;		
	
	public static void bindNewInterface(InetAddress ip, ServiceListener nodeDetector, boolean isServing) throws IOException
	{
		if (mdnslist==null)
		{
			mdnslist=new Hashtable<String, JmDNS>();
		}
		
		System.out.println("Listening on " + ip.getHostAddress());
		try
        {
    		JmDNS mdns=new JmDNS(ip);		
    		mdnslist.put(mdns.getInterface().getHostAddress(), mdns);
    		mdns.addServiceListener("_chungles._tcp.local.", nodeDetector);
    		
    		if (isServing)
    		{
    			ServiceInfo service=new ServiceInfo("_chungles._tcp.local.", 
    		        mdns.getInterface().getHostAddress()+"._chungles._tcp.local.", 6565, 0, 0,
    		        Configuration.getComputerName());
    			mdns.registerService(service);
    		}
        }
        catch (BindException e)
        {
            System.out.println("Failed to listen on interface: " + ip.getHostAddress());
            return;
        }
		
		
	}
	
	public static void closeInterfaces() throws IOException
	{
		Enumeration<JmDNS> enumerator=mdnslist.elements();
		while (enumerator.hasMoreElements())
		{
			JmDNS mdns=enumerator.nextElement();
			System.out.println("Shutting down " + mdns.getInterface().getHostAddress());
			mdns.close();			
		}		
	}
	
}
