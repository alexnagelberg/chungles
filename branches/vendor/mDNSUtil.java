import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

import javax.jmdns.*;

public class mDNSUtil
{
	private static Hashtable mdnslist;		
	
	public static void bindNewInterface(InetAddress ip) throws IOException
	{
		if (mdnslist==null)
		{
			mdnslist=new Hashtable();
		}
		
		System.out.println("Listening on " + ip.getHostAddress());
						
		JmDNS mdns=new JmDNS(ip);		
		mdnslist.put(mdns.getInterface().getHostAddress(), mdns);
		ServiceInfo service=new ServiceInfo("_chungles._tcp.local.", mdns.getInterface().getHostAddress()+"._chungles._tcp.local.", 6565, 0, 0, "Chungles Node");
		mdns.addServiceListener("_chungles._tcp.local.", new NodeDetect());
		mdns.registerService(service);   		
		
		
	}
	
	public static void closeInterfaces() throws IOException
	{
		Enumeration enum=mdnslist.elements();
		while (enum.hasMoreElements())
		{
			JmDNS mdns=(JmDNS)enum.nextElement();
			System.out.println("Shutting down " + mdns.getInterface().getHostAddress());
			mdns.close();			
		}		
	}
	
}
