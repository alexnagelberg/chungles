package org.chungles.core;

import java.io.*;
import java.util.Iterator;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

public class ConfigurationParser extends DefaultHandler
{
	public static boolean parse()
	{
		Configuration.init();
		DefaultHandler handler=new ConfigurationParser();
		SAXParserFactory factory=SAXParserFactory.newInstance();
		try
		{
			SAXParser saxParser = factory.newSAXParser();
			File file=new File(System.getProperty("user.home")+"/.chungles/config.xml");
			if (!file.exists())
			{
				createConfig(file);
				return false;
			}
			else
				saxParser.parse(file, handler);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	public void startElement(String namespaceURI, String localName,
			String qualifiedName, Attributes attrs) throws SAXException
	{		
		if (qualifiedName.equals("share")) // A share
		{
			String name=attrs.getValue("name");
			String map=attrs.getValue("map");
			Configuration.addShare(name, map);
		}
		else if (qualifiedName.equals("computer")) // Computer name
		{
		    Configuration.setComputerName(attrs.getValue("name"));
		}
	}
	
	private static void createConfig(File file)
	{
		try
		{
			new File(System.getProperty("user.home")+"/.chungles/").mkdir();
			file.createNewFile();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void saveConfig()
	{
		String file=System.getProperty("user.home")+"/.chungles/config.xml";
		try
		{
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
			
			// Opening
			out.println("<?xml version='1.0'?>");
			out.println("<chungles>");
			
			// Shares			
			out.println("<shares>");
			Iterator iterator=Configuration.getSharesIterator();
			while (iterator.hasNext())
			{
				String share=(String)iterator.next();
				String map=Configuration.getSharePath(share);
				out.println("<share name=\"" + share + "\" map=\"" + map + "\"/>");
			}
			out.println("</shares>");
			
			// Computer Name
			out.println("<computer name=\"" + Configuration.getComputerName() + "\"/>");
			
			// Closing
			out.println("</chungles>");
			out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}		
	}
}
