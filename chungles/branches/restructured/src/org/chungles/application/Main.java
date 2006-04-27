package org.chungles.application;

import java.io.IOException;
import java.net.*;
import java.util.*;

import org.chungles.ui.UI;
import org.chungles.ui.swt.*;
import org.chungles.core.*;

public class Main
{
	public static UI ui;
	
    public static void main(String[] args) throws IOException
    {
        ui=new SWTUI(); // Place holder till UI selector setup
        if (!ConfigurationParser.parse())
        		ui.openPreferencesDialog();

        Enumeration netInterfaces = NetworkInterface.getNetworkInterfaces();
        while (netInterfaces.hasMoreElements())
        {
            NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
            Enumeration ipAddresses = (Enumeration) ni.getInetAddresses();
            while (ipAddresses.hasMoreElements())
            {                
                InetAddress ip = (InetAddress) ipAddresses.nextElement();
                if (ip.getHostAddress().indexOf(':')<0) // IPv6 currently not supported
                {
                    mDNSUtil.bindNewInterface(ip, new NodeDetect());
                }
            }
        }

        ServerThread server = new ServerThread();
        server.start();

        ui.takeover();

        // UI shuts down, we shut down.
        server.stopListening();
        mDNSUtil.closeInterfaces();
        System.exit(0);
    }

}
