package org.chungles.core;

import java.util.*;

public class Configuration
{
	private static Hashtable<String, String> shares;
	private static String computerName;
	
	public static void init()
	{
		shares=new Hashtable<String, String>();
		computerName="Chungles Node";
	}
	
	public static String getVersion()
	{
		return "0.2.1";
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
}
