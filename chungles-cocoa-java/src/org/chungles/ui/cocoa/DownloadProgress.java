package org.chungles.ui.cocoa;

import org.chungles.core.*;

public class DownloadProgress implements ReceiveProgressListener
{
	private long lastReceived=0;
	public native void update(long increment);
	public void progressUpdate(long bytesReceived)
	{
		if (bytesReceived<lastReceived)
		{
			update(bytesReceived);
		}
		else
			update(bytesReceived-lastReceived);
		lastReceived=bytesReceived;
	}

}
