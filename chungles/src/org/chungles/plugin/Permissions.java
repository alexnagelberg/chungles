package org.chungles.plugin;

public interface Permissions extends StandardPlugin
{
	/**
	 * Determines whether a session can perform an operation
	 * @param session Client's session
	 * @param command Command sent from client
	 * @return true if it can perform operation, false otherwise
	 */
	boolean canPerform(Session session, int command);
	
	/**
	 * Determines whether a session can perform an operation on a specific path. For example
	 * uploading, deleting, downloading, etc.
	 * @param session Client's session
	 * @param command Command sent from client
	 * @param path Path operation wants to be performed on
	 * @return true if it can perform operation, false otherwise
	 */
	boolean canPerform(Session session, int command, String path);
}
