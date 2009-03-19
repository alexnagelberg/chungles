package org.chungles.plugin;

import java.util.*;
public class Session
{
	private static Hashtable<String,Session> sessions;	
	private Date expiration;
	private String IP, username;
	
	private Session()
	{
	}
	
	private Session(String IP, String username)
	{
		this.IP=IP;
		this.username=username;
	}
	
	public static Session createSession(String IP, String username)
	{
		Session session=sessions.get(IP);
		if (session!=null)
		{
			//if (session.getExpiration())  ==today, create new 
		}
		else
		{
			// Set expiration date
			session=new Session(IP, username);
			sessions.put(IP, session);
		}		
		
		return session;
	}
	
	public Date getExpiration()
	{
		return expiration;
	}
}
