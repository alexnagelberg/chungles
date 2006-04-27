package org.chungles.application;

import java.util.*;

public class Configuration
{
	private static Hashtable shares;
	private static String computerName;
	
	public Configuration()
	{
		shares=new Hashtable();
		computerName="Chungles Node";
	}
	
	public static String getVersion()
	{
		return "0.1";
	}
	
	public static String getSharePath(String name)
	{
		return (String)shares.get(name);
	}
	
	public static Iterator getSharesIterator()
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
