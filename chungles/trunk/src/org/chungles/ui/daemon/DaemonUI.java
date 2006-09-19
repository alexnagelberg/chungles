package org.chungles.ui.daemon;

import java.util.*;
import org.chungles.ui.UI;

// We're not going to do anything in here. This is a null UI.
public class DaemonUI implements UI
{
	public void takeover()
	{	
		System.out.println("Press 'q' to quit.");
		try
		{
			while (System.in.read()!='q');
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
