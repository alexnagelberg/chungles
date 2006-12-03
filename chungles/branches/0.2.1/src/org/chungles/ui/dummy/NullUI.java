package org.chungles.ui.dummy;

import java.util.*;
import org.chungles.ui.UI;

// We're not going to do anything in here. This is a null UI.
public class NullUI implements UI
{
	public boolean takeoverWaitsForInterfaces()
	{
		return false;
	}
	
	public void takeover()
	{	
		try
		{
			while (true) // main loop
				System.in.read();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void addNode(String IP, String compname, Hashtable ips, Hashtable compnames)
	{
		
	}
	
	public void removeNode(String IP, String compname, Hashtable ips, Hashtable compnames)
	{
		
	}
	
	public void openPreferencesDialog()
	{
		
	}
}
