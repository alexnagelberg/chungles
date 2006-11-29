package org.chungles.core;

import java.util.*;

public class Configuration
{
	private static Hashtable<String, String> shares;
	private static String computerName;
	private static String mCastShare;
    private static String REV="131:132M";
	
	public static void init()
	{
		shares=new Hashtable<String, String>();
		computerName="Chungles Node";
        mCastShare=System.getProperty("user.home")+"/.chungles/mcast";
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
}
