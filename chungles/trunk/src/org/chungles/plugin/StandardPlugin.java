package org.chungles.plugin;

public interface StandardPlugin
{
	public static int NOTIFICATION_ERROR=1;
	public static int NOTIFICATION_INFORMATION=2;
	
	public String getVersion();
    public String getName();
    public String getAuthor();
    
    public void init();
    public void shutdown();
    public void notify(int type, String message);
}
