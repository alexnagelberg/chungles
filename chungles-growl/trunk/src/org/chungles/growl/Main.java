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
		Runtime.getRuntime().load("/Users/nalex/chungles-growl/build/Release/libchungles-growl.jnilib");
		try
		{
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

	public void notification(int type, String message)
	{
		String notification;
		if (type==StandardPlugin.NOTIFICATION_ERROR)
			notification="Error";
		else if (type==StandardPlugin.NOTIFICATION_GENERAL)
			notification="General";
		else if (type==StandardPlugin.NOTIFICATION_FINISH_TRANSFER)
			notification="Finished Transfer";
		else
			notification="Start Transfer";
		notify(notification,notification,message);
	}

	public void shutdown()
	{
		
	}

}
