package org.chungles.application;
import java.rmi.*;

public class RMIServer implements Remote
{
	public boolean running()
	{
		return true;
	}
}
