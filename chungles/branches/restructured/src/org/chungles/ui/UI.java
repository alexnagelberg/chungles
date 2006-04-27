package org.chungles.ui;

import java.util.*;

public interface UI
{
	public void takeover();
	
	public void addNode(String IP, String compname, Hashtable ips, Hashtable compnames);
	public void removeNode(String IP, String compname, Hashtable ips, Hashtable compnames);
	
	public void openPreferencesDialog();
}
