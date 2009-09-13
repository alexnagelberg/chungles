package org.chungles.growl;
import org.chungles.plugin.*;
import java.io.*;
import java.nio.*;

public class Main implements StandardPlugin
{
	public native void register(byte[] image);
	public native void notify(String notificationName, String title, String description);
	public String getAuthor()
	{
		return "Alex Nagelberg";
	}

	public String getName()
	{
		return "Growl Plugin"; 
	}

	public String getVersion()
	{
		return "0.1";
	}

	public void init()
	{
		try
		{
			// Load library from JAR to temp file, then load temp file as native lib
			InputStream fin=getClass().getClassLoader().getResource("libchungles-growl.jnilib").openStream();
			File tmp=File.createTempFile("libchunglesgrowl", "jnilib");
			FileOutputStream fout=new FileOutputStream(tmp);
			int lastread=0;
			byte[] buf=new byte[2046];
			while (lastread!=-1)
			{
				lastread=fin.read(buf, 0, 2046);
				if (lastread!=-1) fout.write(buf, 0, lastread);
			}
			fin.close();
			fout.close();
			
			Runtime.getRuntime().load(tmp.getAbsolutePath());
			InputStream in=getClass().getClassLoader().getResourceAsStream("images/chungles-32.png");
			int size=2009;
			byte[] image=new byte[size];
			int read=in.read(image, 0, size);
			while (read<size)
				read+=in.read(image,read,size-read);				
			in.close();						
			register(image);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}

	public void shutdown()
	{
		
	}
	public void notify(int type, String message)
	{
		String notification;
		if (type==StandardPlugin.NOTIFICATION_ERROR)
			notification="Error";
		else
			notification="General";
		notify(notification,notification,message);
	}

}
