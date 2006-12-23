package org.chungles.core;

import java.util.*;

import org.chungles.plugin.PluginInfo;
import org.chungles.plugin.StandardPlugin;
import org.chungles.plugin.UIPlugin;

public class Configuration
{
	private static Hashtable<String, String> shares;
	private static String computerName;
	private static String mCastShare;
    private static boolean mCastThrottled;
    private static int mCastKBPSSpeed;
    public static LinkedList<PluginInfo<UIPlugin>> UIplugins=new LinkedList<PluginInfo<UIPlugin>>();
    public static LinkedList<PluginInfo<StandardPlugin>> otherplugins=new LinkedList<PluginInfo<StandardPlugin>>();
    private static String REV="188:190M";    
	
	public static void init()
	{
		shares=new Hashtable<String, String>();
		computerName="Chungles Node";
        mCastShare=System.getProperty("user.home")+"/.chungles/mcast";
        mCastThrottled=true;
        mCastKBPSSpeed=1000;        
	}
	
	public static String getVersion()
	{
		return "r"+REV;
	}
	
	public static String getSharePath(String name)
	{
		return shares.get(name);
	}
	
	public static Iterator<String> getSharesIterator()
	{
		return shares.keySet().iterator();
	}
	
	public static void addShare(String name, String path)
	{
		shares.put(name, path);
	}
	
	public static void clearShares()
	{
		shares.clear();
	}
	
	public static String getComputerName()
	{
	    return computerName;
	}
	
	public static void setComputerName(String name)
	{
	    computerName=name;
	}
    
    public static String getMCastShare()
    {
        return mCastShare;
    }
    
    public static void setMCastShare(String name)
    {
        mCastShare=name;
    }
    
    public static boolean isMCastThrottled()
    {
        return mCastThrottled;
    }
    
    public static void setMCastThrottled(boolean isThrottled)
    {
        mCastThrottled=isThrottled;
    }
    
    public static int getMCastKBPSSpeed()
    {
        return mCastKBPSSpeed;
    }
    
    public static void setMCastKBPSSpeed(int speed)
    {
        mCastKBPSSpeed=speed;
    }
}
