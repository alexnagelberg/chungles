package org.chungles.core;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

import javax.jmdns.*;

public class mDNSUtil
{
	private static Hashtable mdnslist;		
	
	public static void bindNewInterface(InetAddress ip, ServiceListener nodeDetector) throws IOException
	{
		if (mdnslist==null)
		{
			mdnslist=new Hashtable();
		}
		
		System.out.println("Listening on " + ip.getHostAddress());
						
		JmDNS mdns=new JmDNS(ip);		
		mdnslist.put(mdns.getInterface().getHostAddress(), mdns);
		ServiceInfo service=new ServiceInfo("_chungles._tcp.local.", 
		        mdns.getInterface().getHostAddress()+"._chungles._tcp.local.", 6565, 0, 0,
		        Configuration.getComputerName());
		mdns.addServiceListener("_chungles._tcp.local.", nodeDetector);
		mdns.registerService(service);   		
		
		
	}
	
	public static void closeInterfaces() throws IOException
	{
		Enumeration enumerator=mdnslist.elements();
		while (enumerator.hasMoreElements())
		{
			JmDNS mdns=(JmDNS)enumerator.nextElement();
			System.out.println("Shutting down " + mdns.getInterface().getHostAddress());
			mdns.close();			
		}		
	}
	
}
