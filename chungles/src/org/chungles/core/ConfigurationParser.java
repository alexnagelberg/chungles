package org.chungles.core;

import java.io.*;
import java.util.Iterator;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import org.chungles.plugin.*;

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
		if (qualifiedName.equals("chungles"))
		{
			Configuration.setVersion(attrs.getValue("version"));
		}
		else if (qualifiedName.equals("share")) // A share
		{
			String name=attrs.getValue("name");
			String map=attrs.getValue("map");
			Configuration.addShare(name, map);
		}
		else if (qualifiedName.equals("computer")) // Computer name
		{
		    Configuration.setComputerName(attrs.getValue("name"));
		}
        else if (qualifiedName.equals("mcastshare"))
        {
            Configuration.setMCastShare(attrs.getValue("name"));
        }
        else if (qualifiedName.equals("mcastflow"))
        {
            String enabled=attrs.getValue("enabled");
            String speed=attrs.getValue("speed");
            if (enabled.equalsIgnoreCase("true"))
            {
                Configuration.setMCastThrottled(true);
                if (!speed.equals(""))
                    Configuration.setMCastKBPSSpeed(Integer.parseInt(speed));
            }
            else
                Configuration.setMCastThrottled(false);                        
        }
        else if (qualifiedName.equals("plugin"))
        {
            String path=attrs.getValue("jar");
            String enabledstr=attrs.getValue("enabled");
            boolean enabled=false;
            if (enabledstr==null || enabledstr.compareToIgnoreCase("true")==0)
                enabled=true;            
            PluginAction.loadPlugin(path, enabled);
        }
	}
	
	private static void createConfig(File file)
	{
		try
		{
			new File(System.getProperty("user.home")+"/.chungles/").mkdir();
            new File(System.getProperty("user.home")+"/.chungles/mcast").mkdir();
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
			out.println("<chungles version='"+Configuration.getVersion()+"'>");
			
			// Shares			
			out.println("<shares>");
			Iterator<String> iterator=Configuration.getSharesIterator();
			while (iterator.hasNext())
			{
				String share=iterator.next();
				String map=Configuration.getSharePath(share);
				out.println("<share name=\"" + share + "\" map=\"" + map + "\"/>");
			}
			out.println("</shares>");
			
			// Computer Name
			out.println("<computer name=\"" + Configuration.getComputerName() + "\"/>");
			
            // Multicast Share
            out.println("<mcastshare name=\"" + Configuration.getMCastShare() + "\"/>");
            
            // Multicast Flow Control
            out.println("<mcastflow enabled=\"" + Configuration.isMCastThrottled() + "\" speed=\"" + 
                    Configuration.getMCastKBPSSpeed() + "\"/>");
            
            // Plugins
            Iterator<PluginInfo<UIPlugin>> iter1=Configuration.UIplugins.iterator();
            while (iter1.hasNext())
            {
                PluginInfo<UIPlugin> p=iter1.next();
                out.println("<plugin jar=\""+p.getJARPath()+"\" enabled=\""+p.isEnabled()+"\"/>");
            }
            
            Iterator<PluginInfo<StandardPlugin>> iter2=Configuration.otherplugins.iterator();
            while (iter2.hasNext())
            {
                PluginInfo<StandardPlugin> p=iter2.next();
                out.println("<plugin jar=\""+p.getJARPath()+"\" enabled=\""+p.isEnabled()+"\"/>");
            }
            
			// Closing
			out.println("</chungles>");
			out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}		
	}
    
    public void endDocument()
    {
        File mshare=new File(Configuration.getMCastShare());
        if (!mshare.exists())
            mshare.mkdir();
        saveConfig();
    }
    
    public void fatalError(SAXParseException e)
    {
        System.out.println("Fatal error parsing configuration file, overwriting file with settings.");
        saveConfig();
        
        File mshare=new File(Configuration.getMCastShare());
        if (!mshare.exists())
            mshare.mkdir();
    }
}
