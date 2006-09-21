package org.chungles.ui;

import java.util.*;

public interface UI
{
	/**
	 * This is the UI's main loop. This method should be blocking. 
	 *
	 */
	public void takeover();
	
	/**
	 * Called when a new node is discovered. Sometimes sends duplicates so checking
	 * for so must be done.
	 *  
	 * @param IP IP address of node discovered
	 * @param compname Node discovered's name
	 * @param ips Hash table of IPs with computer name for keys
	 * @param compnames Hash table of computer names with IPs for keys
	 */
	public void addNode(String IP, String compname, Hashtable<String, String> ips, Hashtable<String, String> compnames);
	
	/**
	 * Called when a node is no longer on the network. Sometimes sends duplicates so
	 * checking for so must be done.
	 * 
	 * @param IP IP address of node offlined
	 * @param compname Offlined node's name
	 * @param ips Hash table of IPs with computer name for keys
	 * @param compnames Hash table of computer names with IPs for keys
	 */
	public void removeNode(String IP, String compname, Hashtable<String, String> ips, Hashtable<String, String> compnames);
	
	/**
	 * Should open a preferences dialog
	 *
	 */
	public void openPreferencesDialog();
}
