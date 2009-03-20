package org.chungles.plugin;

public interface NotificationPlugin
{
	public final static int NOTIFICATION_START_TRANSFER=1;
	public final static int NOTIFICATION_FINISH_TRANSFER=2;
	public final static int NOTIFICATION_GENERAL=3;
	public final static int NOTIFICATION_ERROR=4;
	
	public void notification(int type, String message);
}
