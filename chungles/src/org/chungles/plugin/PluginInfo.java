package org.chungles.plugin;

public class PluginInfo<T>
{   
    private String mainClass, JARPath;
    private T plugin;
    private boolean enabled=false;
    private int type;
    
    public final static int UI=0;
    public final static int OTHER=1;
    
    public PluginInfo(String mainClass, String JARPath, T plugin, boolean enabled, int type)
    {
        this.mainClass=mainClass;
        this.JARPath=JARPath;
        this.plugin=plugin;
        this.enabled=enabled;
        this.type=type;
    }
    
    public String getMainClass()
    {
        return mainClass;
    }
    
    public void setMainClass(String mainClass)
    {
        this.mainClass=mainClass;
    }
    
    public String getJARPath()
    {
        return JARPath;
    }
    
    public void setJARPath(String JARPath)
    {
        this.JARPath=JARPath;
    }
    
    public T getPlugin()
    {
        return plugin;
    }
    
    public void setPlugin(T plugin)
    {
        this.plugin=plugin;
    }
    
    public boolean equals(Object o)
    {
        if (!(o instanceof PluginInfo))
            return false;
        
        PluginInfo p=(PluginInfo)o;
        if (p.JARPath.equals(JARPath) && p.mainClass.equals(mainClass))
            return true;
        else
            return false;
    }
    
    public void setEnabled(boolean isEnabled)
    {
        enabled=isEnabled;
    }
    
    public boolean isEnabled()
    {
        return enabled;
    }
    
    public int getType()
    {
    	return type;
    }
    
    public void setType(int type)
    {
    	this.type=type;
    }
}
