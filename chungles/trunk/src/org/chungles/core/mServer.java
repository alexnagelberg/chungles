package org.chungles.core;

import java.io.*;
import java.net.*;
import java.util.*;

public class mServer extends Thread
{
	private final static int PACKET_SIZE=1024;
	private static boolean fallbackEnabled=false;
	private static String localfile;
    private String remotefile;
    private SendProgressListener progress;
    
    public void run()
    {
        try
        {                                               
            InetAddress group = InetAddress.getByName("224.3.2.1");
            MulticastSocket s = new MulticastSocket();                      
            s.setLoopbackMode(true);
            s.setTimeToLive(1);
            
            // Put together header with command, file's size, and length of filename string
            long filesize=new File(localfile).length();
            FileInputStream fis=new FileInputStream(localfile);
            byte[] header=new byte[13];
            header[0]=ServerConnectionThread.BEGIN_MULTICAST;
            System.arraycopy(Util.longToBytes(filesize), 0, header, 1, 8);
            System.arraycopy(Util.intToBytes(remotefile.length()), 0, header, 9, 4);
            
            // Send header
            DatagramPacket pack = new DatagramPacket(header, header.length, group, 6565);                       
            s.send(pack);           
            
            // Send filename
            pack = new DatagramPacket(remotefile.getBytes(), remotefile.getBytes().length, group, 6565);
            s.send(pack);
            
            int length;
            long offset=0;
            
            do
            {
                byte[] filebuf=new byte[PACKET_SIZE];
                length=fis.read(filebuf, 0, PACKET_SIZE);
                byte[] outbuf=new byte[12 + PACKET_SIZE]; // Offset, Packet's size, Packet
                
                if (length > 0)
                {
                    System.arraycopy(Util.longToBytes(offset), 0, outbuf, 0, 8);
                    System.arraycopy(Util.intToBytes(length), 0, outbuf, 8, 4);
                    System.arraycopy(filebuf, 0, outbuf, 12, length);
                    
                    pack = new DatagramPacket(outbuf, 1036, group, 6565);
                    s.send(pack);
                    
                    offset+=length;
                    progress.progressUpdate(offset);                    
                }               
            }
            while (length==PACKET_SIZE);
            
            fis.close();
            
            // Initialize TCP fallback server, allow connections for 30s
            fallbackEnabled=true;
            Timer timer=new Timer();
            timer.schedule(new TimerTask()
            {
                public void run()
                {
                    fallbackEnabled=false;
                }
            }, 30000);          
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
	private mServer()
	{
		
	}
	
	public mServer(String localfile, String remotefile, SendProgressListener progress)
	{
		mServer.localfile=localfile;
        this.remotefile=remotefile;
        this.progress=progress;
	}
	
	public static boolean isFallbackEnabled()
	{
		return fallbackEnabled;
	}
    
    public static String getFileName()
    {
        return localfile;
    }
}
