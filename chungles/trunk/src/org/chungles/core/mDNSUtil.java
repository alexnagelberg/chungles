package org.chungles.core;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.util.*;

import javax.jmdns.*;

public class mDNSUtil
{
	private static Hashtable<String, JmDNS> mdnslist;		
    private static Hashtable<JmDNS, ServiceInfo> servicelist;
    
	public static void bindNewInterface(InetAddress ip, ServiceListener nodeDetector, boolean isServing) throws IOException
	{
		if (mdnslist==null || servicelist==null)
		{
			mdnslist=new Hashtable<String, JmDNS>();
            servicelist=new Hashtable<JmDNS, ServiceInfo>();
		}
		
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
                servicelist.put(mdns, service);
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
        servicelist.clear();
	}
    
    public static void reloadInterfaces() throws IOException
    {
        Enumeration<JmDNS> enumerator=mdnslist.elements();
        while (enumerator.hasMoreElements())
        {
            JmDNS mdns=enumerator.nextElement();            
            mdns.unregisterService(servicelist.get(mdns));
            servicelist.remove(mdns);
            ServiceInfo service=new ServiceInfo("_chungles._tcp.local.", 
                    mdns.getInterface().getHostAddress()+"._chungles._tcp.local.", 6565, 0, 0,
                    Configuration.getComputerName());
            mdns.registerService(service);
            servicelist.put(mdns, service);
        }
    }
    
    public static boolean isBound(InetAddress addr)
    {
    	Enumeration<JmDNS> enumerator=mdnslist.elements();
        while (enumerator.hasMoreElements())
        {
        	JmDNS mdns=enumerator.nextElement();
        	try
        	{
        		if (mdns.getInterface().equals(addr))
        			return true;
        	}
        	catch (Exception e)
        	{
        		e.printStackTrace();
        	}
        }
        return false;
    }
	
}
