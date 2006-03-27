package org.chungles.frameworks.stateless;

import com.simontuffs.onejar.*;
import java.io.*;
import java.util.jar.*;

public class StatelessLibraryLoader
{
    /**
     * Loads stateless app
     * 
     * @param path Path to stateless Jar
     * @return new StatelessApplication object containing app
     */
	public static StatelessApplication addApplication(String path)
		throws BadApplicationException
	{
		try
		{
			FileInputStream fin=new FileInputStream(path);
			JarInputStream jin=new JarInputStream(fin);
			Manifest m=jin.getManifest();
			Attributes attr=m.getMainAttributes();
			String mainClass=attr.getValue("Stateless-Class");
			jin.close();
			JarClassLoader loader=new JarClassLoader(ClassLoader.getSystemClassLoader());
			loader.load(mainClass, path);
			StatelessApplication app=(StatelessApplication)(loader.loadClass("Test").newInstance());
			return app;
		}
		catch (Exception e)
		{
			throw (BadApplicationException)e;
		}
	}
}
