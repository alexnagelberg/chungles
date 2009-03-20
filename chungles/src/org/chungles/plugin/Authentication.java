package org.chungles.plugin;

import java.io.*;

public interface Authentication extends StandardPlugin
{	
	/**
	 * Validates credentials using socket's input/output streams
	 * @param in Input stream
	 * @param out Output stream
	 * @return Session if credentials are valid, null otherwise
	 */
	public Session validate(InputStream in, OutputStream out);
}
