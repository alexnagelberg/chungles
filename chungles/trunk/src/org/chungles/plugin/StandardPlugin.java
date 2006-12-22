package org.chungles.plugin;

public interface StandardPlugin
{
    public String getVersion();
    public String getName();
    public String getAuthor();
    
    public void init();
    public void shutdown();    
}
