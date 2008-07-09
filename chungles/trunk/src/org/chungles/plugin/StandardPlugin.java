package org.chungles.plugin;

public interface StandardPlugin
{
	public final static int NOTIFICATION_START_TRANSFER=1;
	public final static int NOTIFICATION_FINISH_TRANSFER=2;
	public final static int NOTIFICATION_GENERAL=3;
	public final static int NOTIFICATION_ERROR=4;
	
    public String getVersion();
    public String getName();
    public String getAuthor();
    
    public void init();
    public void shutdown(); 
    public void notification(int type, String message);
}
