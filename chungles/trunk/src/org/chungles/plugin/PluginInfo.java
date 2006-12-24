package org.chungles.plugin;

public class PluginInfo<T>
{   
    private String mainClass, JARPath;
    private T plugin;
    
    public PluginInfo(String mainClass, String JARPath, T plugin)
    {
        this.mainClass=mainClass;
        this.JARPath=JARPath;
        this.plugin=plugin;
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
}
