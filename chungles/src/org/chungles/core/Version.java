package org.chungles.core;

import java.util.*;

public class Version
{
	private int major;
	private int minor;
	private int revision;
	
	public Version(int major, int minor, int revision)
	{
		this.major=major;
		this.minor=minor;
		this.revision=revision;
	}
	
	public Version(int major, int minor)
	{
		this.major=major;
		this.minor=minor;
		this.revision=0;
	}
	
	public Version(int major)
	{
		this.major=major;
		this.minor=this.revision=0;
	}
	
	public Version()
	{
		this.major=this.minor=this.revision=0;
	}
	
	public Version(String version)
	{
		StringTokenizer tok=new StringTokenizer(version, ".");
		major=Integer.parseInt(tok.nextToken());
		if (tok.hasMoreTokens()) minor=Integer.parseInt(tok.nextToken());
		if (tok.hasMoreTokens()) revision=Integer.parseInt(tok.nextToken());
	}
	
	public int getMajor()
	{
		return major;
	}
	
	public int getMinor()
	{
		return minor;
	}
	
	public int getRevision()
	{
		return revision;
	}
	
	public void setMajor(int major)
	{
		this.major=major;
	}
	
	public void setMinor(int minor)
	{
		this.minor=minor;
	}
	
	public void setRevision(int revision)
	{
		this.revision=revision;
	}
	
	public String toString()
	{
		return major + "." + minor + "." + revision;
	}
}
