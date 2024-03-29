package org.chungles.core;

import java.io.*;
import java.net.*;
import java.util.*;

import org.chungles.plugin.PluginAction;
import org.chungles.plugin.StandardPlugin;

public class mClient extends Thread
{
	private boolean stoprunning=false;
	private static int PACKET_SIZE=1024;
	
	public void run()
	{
		try
		{
			InetAddress group = InetAddress.getByName("224.0.0.3");
			MulticastSocket s = new MulticastSocket(6565);			
			s.joinGroup(group);
			
			
			while (!stoprunning)
			{
				byte[] buf=new byte[13];
				DatagramPacket pack = new DatagramPacket(buf, buf.length);
				s.receive(pack);
				
				if (buf[0]==ServerConnectionThread.BEGIN_MULTICAST && !mDNSUtil.isBound(pack.getAddress()))
				{
					s.setSoTimeout(10000); // 10 seconds allowed between receiving packets before exception
					PluginAction.notify(StandardPlugin.NOTIFICATION_INFORMATION, "Receiving multicast.");
					Hashtable<Long, Boolean> unreceivedpackets=new Hashtable<Long, Boolean>();
					byte filesizebuf[]=new byte[8];
					byte strlengthbuf[]=new byte[4];
					
					System.arraycopy(buf, 1, filesizebuf, 0, 8);
					long filesize=Util.byteToLong(filesizebuf);
					System.arraycopy(buf, 9, strlengthbuf, 0, 4);
					buf=new byte[Util.byteToInt(strlengthbuf)];
					
					pack = new DatagramPacket(buf, Util.byteToInt(strlengthbuf));
					s.receive(pack);
					
					String filename=new String(buf);										
					InetAddress host=pack.getAddress();
					
	                File file=new File(Configuration.getMCastShare()+"/"+filename);
	                if (file.exists())
	                	file.delete();
	                
	                RandomAccessFile fout=new RandomAccessFile(
	                		Configuration.getMCastShare()+"/"+filename, "rw");
	                
	                // Receive Multicast packets till 'finished' command arrives	                
	                long lastoffset=-PACKET_SIZE;
	                try
	                {
						do
						{
							buf=new byte[12+PACKET_SIZE];
							
							byte[] offsetbuf=new byte[8];
							byte[] lengthbuf=new byte[4];
	
							pack = new DatagramPacket(buf, 12+PACKET_SIZE);
							s.receive(pack);
							if (pack.getAddress().equals(host))
							{
								System.arraycopy(buf, 0, offsetbuf, 0, 8);
								System.arraycopy(buf, 8, lengthbuf, 0, 4);
									
								long offset=Util.byteToLong(offsetbuf);
								int length=Util.byteToInt(lengthbuf);
		                        if (offset>lastoffset)
		                        {
		                        	if (offset>(lastoffset+PACKET_SIZE))
		                            {
		                            	// zero-fill for placeholder to write over later
		                                int gapsize=(int)(offset-lastoffset-PACKET_SIZE);
		                                byte[] zeros=new byte[gapsize];
		                                fout.write(zeros, 0, gapsize);
		                                for (long i=lastoffset+PACKET_SIZE; i<offset; i+=PACKET_SIZE)
		                                	unreceivedpackets.put(i, true);
		                            }
		                            fout.write(buf, 12, length);                                                                
		                            lastoffset=offset;
		                        }
		                        else
		                        {
		                        	// backwriting
		                        	fout.seek(offset);
		                            fout.write(buf, 12, length);
		                            fout.seek(fout.length());
		                            unreceivedpackets.remove(offset);                            	
		                        }
							}
							
						}
	                    while (lastoffset<filesize-PACKET_SIZE);
	                }
	                catch (SocketTimeoutException e)
	                {
                        for (long i=lastoffset+PACKET_SIZE; i<filesize; i+=PACKET_SIZE)
                        {
                            unreceivedpackets.put(i, true);
                            if (filesize-i>=PACKET_SIZE)
                            {
                                byte[] zeros=new byte[PACKET_SIZE];
                                fout.write(zeros, 0, PACKET_SIZE);
                            }
                            else
                            {
                                byte[] zeros=new byte[(int)(filesize-i)];
                                fout.write(zeros, 0, (int)(filesize-i));
                            }
                        }
	                }
	                
					// Recover packets with TCP connection
					Socket sock=new Socket(host, 6565);
					sock.setSoTimeout(30000); // 30 seconds allowed between receiving packets before exception
					OutputStream sout=sock.getOutputStream();
	                InputStream sin=sock.getInputStream();	                	                
	                
	                sout.write(ServerConnectionThread.RECOVER_PACKETS);
	                sout.write(Util.intToBytes(unreceivedpackets.size()));
	                
	                fout.seek(0);
	                Enumeration<Long> en=unreceivedpackets.keys();
                    long i=0;
	                try
	                {
		                while (en.hasMoreElements())				
						{
							i=en.nextElement();
		                    sout.write(Util.longToBytes(i), 0, 8);                        
		                    buf=new byte[PACKET_SIZE];
		                    int length=sin.read(buf, 0, PACKET_SIZE);
		                    fout.seek(i);    
		                    fout.write(buf, 0, length);					
						}
		                sock.close();
		                fout.close();
		                PluginAction.notify(StandardPlugin.NOTIFICATION_INFORMATION, "Received multicast successfully.");
	                }
		            catch (SocketTimeoutException e)
		            {
		            	// When fallback fails, file fails
		            	PluginAction.notify(StandardPlugin.NOTIFICATION_ERROR, "Error receiving multicast.");
		            }
		            s.setSoTimeout(0);
				}
				
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void stopListening()
	{
		stoprunning=true;
	}
		
}
