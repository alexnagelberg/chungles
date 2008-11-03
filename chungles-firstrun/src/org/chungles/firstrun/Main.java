package org.chungles.firstrun;

import org.chungles.core.*;
import java.io.*;

import javax.xml.parsers.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.*;

public class Main
{
	private static final String THIS_VERSION="0.4";
	
	public static void main(String[] args)
	{	
		File file=new File(System.getProperty("user.home")+"/.chungles/config.xml");
		if (!file.exists()
			||	!isLatest(file))
		{				
			Configuration.init();
			new File(System.getProperty("user.home")+"/.chungles/").mkdir();
            new File(System.getProperty("user.home")+"/.chungles/mcast").mkdir();
            new File(System.getProperty("user.home")+"/.chungles/plugins").mkdir(); 
            new File(System.getProperty("user.home")+"/.chungles/plugins/lib").mkdir();
			ConfigurationParser.saveConfig();			
			try
			{
				// rewrite run script
				FileOutputStream fout=new FileOutputStream("run.sh");
				fout.write("#!/bin/sh\n".getBytes());
				fout.write("java -jar chungles.jar".getBytes());
				fout.close();
				new File("run.sh").setExecutable(true);
				
				// copy swt plugin to plugin dir
				FileInputStream in=new FileInputStream("chungles-swt.jar");
				FileOutputStream out=new FileOutputStream(System.getProperty("user.home")+"/.chungles/plugins/chungles-swt.jar");
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0)
				{
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
				
				in=new FileInputStream("swt.jar");
				out=new FileOutputStream(System.getProperty("user.home")+"/.chungles/plugins/lib/swt.jar");
				while ((len = in.read(buf)) > 0)
				{
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
				
				// run chungles
				org.chungles.application.Main.main(args);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
		}
		
	}
	
	private static boolean isLatest(File file)
	{
		try
		{			
			Document doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
			XPath xpath=XPathFactory.newInstance().newXPath();
			String version=(String)xpath.evaluate("/chungles/@version", doc, XPathConstants.STRING);			
			return version.equals(THIS_VERSION);
		}
		catch (Exception e)
		{
			return false;
		}		
	}
}
