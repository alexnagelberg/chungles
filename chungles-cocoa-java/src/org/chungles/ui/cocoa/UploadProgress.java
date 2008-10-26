package org.chungles.ui.cocoa;

import org.chungles.core.*;

public class UploadProgress implements SendProgressListener
{	
	private long lastSent=0;
	public native void update(long increment);
	public void progressUpdate(long bytesSent)
	{		
		if (bytesSent<lastSent)
		{
			update(bytesSent);
		}
		else
			update(bytesSent-lastSent);
		lastSent=bytesSent;
	}

}
