
import java.io.IOException;
import java.net.*;
import java.util.*;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        //DaemonUtil dutil = DaemonUtil.getInstance();
        //dutil.parseArgs(args);
	Configuration.parse();
        SWTUtil swt = SWTUtil.getInstance();

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
                    mDNSUtil.bindNewInterface(ip);
                }
            }
        }

        ServerThread server = new ServerThread();
        server.start();

        // Go! If daemon mode run daemon loop otherwise run window loop
        /*if (dutil.getConfig().getBoolean("daemon"))
        {
            dutil.run();
        }
        else
        {*/
            swt.mainLoop();
        //}

        // Window or daemon shuts down, we shut down
        server.stopListening();
        mDNSUtil.closeInterfaces();
        System.exit(0);
    }

}
