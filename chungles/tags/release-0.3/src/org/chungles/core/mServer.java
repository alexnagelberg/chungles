package org.chungles.core;

import java.io.*;
import java.net.*;
import java.util.*;

public class mServer
{
	private final static int PACKET_SIZE=1024;
	private static boolean fallbackEnabled=false;
	private static String localfile;        
    private MulticastSocket s;
    
	private mServer()
	{
		
	}
	
	public mServer(String localfile, String remotefile, SendProgressListener progress)
	{
		mServer.localfile=localfile;
		try
        {                                               
            InetAddress group = InetAddress.getByName("224.0.0.3");
            s = new MulticastSocket();                      
            s.setLoopbackMode(true);
            s.setTimeToLive(1);
            
            // Put together header with command, file's size, and length of filename string
            long filesize=new File(localfile).length();
            
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
                        
            
            if (Configuration.isMCastThrottled())
                sendThrottled(progress);
            else
                send(progress);                        
            
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
	
	public static boolean isFallbackEnabled()
	{
		return fallbackEnabled;
	}
    
    public static String getFileName()
    {
        return localfile;
    }
    
    private void sendThrottled(SendProgressListener progress)
    {
        try
        {
            FileInputStream fis=new FileInputStream(localfile);
            int length;
            long offset=0;
            do
            {
                int i=0;
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
                        
                        InetAddress group = InetAddress.getByName("224.0.0.3");
                        DatagramPacket pack = new DatagramPacket(outbuf, PACKET_SIZE+12, group, 6565);
                        s.send(pack);
                        
                        offset+=length;
                        progress.progressUpdate(offset);                    
                    }
                    i++;
                }
                while (i<Configuration.getMCastKBPSSpeed()/100 && length==PACKET_SIZE);
                Thread.sleep(10);
            }
            while (length==PACKET_SIZE);
            fis.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void send(SendProgressListener progress)
    {
        try
        {
            FileInputStream fis=new FileInputStream(localfile);
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
                       
                    InetAddress group = InetAddress.getByName("224.0.0.3");
                    DatagramPacket pack = new DatagramPacket(outbuf, PACKET_SIZE+12, group, 6565);
                    s.send(pack);
                      
                    offset+=length;
                    progress.progressUpdate(offset);                    
                }                
            }
            while (length==PACKET_SIZE);
            fis.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
