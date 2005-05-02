import java.io.IOException;
import java.net.*;
import java.util.*;

public class Main
{
	public static void main(String[] args) throws IOException
	{
        DaemonUtil dutil = DaemonUtil.getInstance();
        dutil.parseArgs(args);
        SWTUtil swt=SWTUtil.getInstance();

		Enumeration netInterfaces=NetworkInterface.getNetworkInterfaces();
		while (netInterfaces.hasMoreElements())
		{
			NetworkInterface ni=(NetworkInterface)netInterfaces.nextElement();
			InetAddress ip=(InetAddress) ni.getInetAddresses().nextElement();
			if (!ip.getHostAddress().equals("127.0.0.1"))
			{
				mDNSUtil.bindNewInterface(ip);
			}
		}
	    
		ServerThread server=new ServerThread();
        server.start();

	    // Go! If daemon mode run daemon loop otherwise run window loop
        if (dutil.getConfig().getBoolean("daemon"))
        {
            dutil.run();
        }
        else
        {
            swt.mainLoop();
        }

	    // Window or daemon shuts down, we shut down
	    server.stopListening();
	    mDNSUtil.closeInterfaces();
	    System.exit(0);
	}
	
	
}
