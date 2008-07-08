package org.chungles.application;

import java.io.*;
import java.net.*;
import java.util.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.rmi.*;

import org.chungles.core.*;
import org.chungles.plugin.*;

public class Main
{
	
	private static boolean isServer;
    
    public static void main(String[] args) throws IOException
    {    	
        PluginAction.loadPlugins();
    	
        if (!ConfigurationParser.parse())
        	PluginAction.openPreferencesDialog();
                
        try
        {
        	Registry registry = LocateRegistry.getRegistry("localhost");
        	registry.lookup("running");
        	System.out.println("Detected application already running. Disabling server.");
        	isServer=false;
        }
        catch (Exception e)
        {
        	isServer=true;
        }
        
        if (isServer)
        {
        	Remote rmi=(Remote)UnicastRemoteObject.exportObject(new RMIServer(),0);
        	Registry registry=LocateRegistry.createRegistry(1099);
        	try
        	{
        		registry.bind("running", rmi);
        	}
        	catch (Exception e)
        	{
        		e.printStackTrace();
        	}
        }
        /*if (ui.takeoverWaitsForInterfaces())
        	blockingmDNSBind();
        else
        	threadedmDNSBind();*/     
        threadedmDNSBind();

        ServerThread server = new ServerThread();
        mClient mclient=new mClient(new FinishNotification()
        {
        	public void finished(boolean successfully)
        	{
        		if (successfully)
        			PluginAction.finishnotification(true, "Received Multicast file.");
        		else
        			PluginAction.finishnotification(false, "Error receiving incoming Multicast");
        	}
        });
        
        if (isServer)
        {
            server.start();
            mclient.start();
        }

        PluginAction.mainloop();

        // UI shuts down, we shut down.        
        if (isServer)
        {
            server.stopListening();
            mclient.stopListening();
        }
        
        mDNSUtil.closeInterfaces();
        System.exit(0);
    }

    /*private static void blockingmDNSBind()
    {
    	try
        {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements())
            {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> ipAddresses = ni.getInetAddresses();
                while (ipAddresses.hasMoreElements())
                {                
                    InetAddress ip = ipAddresses.nextElement();
                    if (ip.getHostAddress().indexOf(':')<0) // IPv6 currently not supported
                    {
                        mDNSUtil.bindNewInterface(ip, new NodeDetect(), isServer);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }*/
    
    private static void threadedmDNSBind()
    {
    	Thread mdnsthread=new Thread()
        {
            public void run()
            {
                try
                {
                    Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
                    while (netInterfaces.hasMoreElements())
                    {
                        NetworkInterface ni = netInterfaces.nextElement();
                        Enumeration<InetAddress> ipAddresses = ni.getInetAddresses();
                        while (ipAddresses.hasMoreElements())
                        {                
                            InetAddress ip = ipAddresses.nextElement();
                            if (ip.getHostAddress().indexOf(':')<0) // IPv6 currently not supported
                            {
                                mDNSUtil.bindNewInterface(ip, new NodeDetect(), isServer);
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        mdnsthread.start();
    }
    
    
}
