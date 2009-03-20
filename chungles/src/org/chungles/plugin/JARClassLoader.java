package org.chungles.plugin;

import java.io.File;
import java.net.*;

public class JARClassLoader extends URLClassLoader
{
	public JARClassLoader(String[] paths) throws MalformedURLException
	{
		super (JARClassLoader.StringsToURLs(paths));				
	}
	
	public JARClassLoader(URL[] urls, ClassLoader parent)
	{
		super(urls, parent);
	}
	
	public JARClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory)
	{
		super(urls, parent, factory);
	}
	
	public Class getClass(String classname) throws ClassNotFoundException
	{		
		return findClass(classname);
	}
	
	public void addJAR(String path) throws MalformedURLException
	{
		addURL(new File(path).toURI().toURL());
	}
	
	private static URL[] StringsToURLs(String[] strs) throws MalformedURLException
	{
		URL[] urls=new URL[strs.length];
		for (int i=0; i<strs.length; i++)
		{
			urls[i]=new File(strs[i]).toURI().toURL();			
		}
		return urls;
	}
}
