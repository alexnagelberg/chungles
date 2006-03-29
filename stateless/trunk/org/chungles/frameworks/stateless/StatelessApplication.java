package org.chungles.frameworks.stateless;

import java.io.Serializable;

public interface StatelessApplication extends Serializable
{
	// Interface for methods to call and communicate with app to freeze/thaw
	
	// Return application's ID
	public String getAppID();
	
    // Return string containing command to execute app (with args)
    public String getSystemCommand();
    
	// Cause application to freeze
	public State freeze();
	
	// Thaw application with state
	public void thaw(State state);
	
	// Check to see if there's enough memory for thawing
	public boolean hasEnoughMemory(long size);
}
