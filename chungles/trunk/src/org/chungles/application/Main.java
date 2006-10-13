package org.chungles.application;

import java.io.*;
import java.net.*;
import java.util.*;

import org.chungles.ui.UI;
import org.chungles.ui.swt.*;
import org.chungles.ui.dummy.*;
import org.chungles.ui.console.*;
import org.chungles.core.*;

public class Main
{
	public static UI ui;
	private static boolean isServer;
    
    public static void main(String[] args) throws IOException
    {
    	ArgumentParser argparse=new ArgumentParser(args);
    	
    	String uitype=argparse.getUI();
    	if (uitype.equals("swt"))
    		ui=new SWTUI();
    	else if (uitype.equals("console"))
    		ui=new ConsoleUI();
    	else // daemon
    		ui=new NullUI();
    	
        if (!ConfigurationParser.parse())
        		ui.openPreferencesDialog();

        File lockfile=new File(System.getProperty("user.home")+"/.chungles/.lock");
        isServer=!lockfile.exists();
        
        if (!isServer)
        {
        	System.out.println("Detected " + lockfile.getAbsolutePath() + ": Assuming " + 
        			"another instance is running. Disabling server.");
        }
        else
        {        	        
        	lockfile.createNewFile();
        	lockfile.deleteOnExit();
        }
        
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
        

        ServerThread server = new ServerThread();
        if (isServer) server.start();

        ui.takeover();

        // UI shuts down, we shut down.        
        if (isServer) server.stopListening();
        mDNSUtil.closeInterfaces();
        System.exit(0);
    }

}
