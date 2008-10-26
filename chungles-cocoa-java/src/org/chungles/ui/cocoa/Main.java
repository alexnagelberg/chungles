package org.chungles.ui.cocoa;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

import org.w3c.dom.*;
import org.chungles.plugin.*;

public class Main implements UIPlugin 
{	
	public native static void cocoa_init(Object inst);
	public native static void cocoa_addNode(String IP, String compName);
	public native static void cocoa_removeNode(String IP, String compName);
	public native static void cocoa_openprefs();
	
	public String getVersion()
	{
		return "0.1";
	}
    
	public String getName()
	{	
		return "Cocoa UI";
	}
    
    public String getAuthor()
    {
    	return "Alex Nagelberg";
    }
    
    public void init()
    {    	
    	System.out.println("Java: Init");
    	try
    	{
    		ClassLoader.getSystemClassLoader().loadClass("org.chungles.ui.cocoa.Main").
    		getMethod("cocoa_init", new Class[]{Object.class}).invoke(null, new Object[]{this});
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    }
    
    public void shutdown()
    {
    	PluginAction.shutdown();
    }
    
	public void addNode(String IP, String compname)
	{
		cocoa_addNode(IP, compname);		
	}
    
	public void removeNode(String IP, String compname)
    {
    	cocoa_removeNode(IP, compname);
    }
    
    public void openPreferencesDialog()
    {
    	cocoa_openprefs();
    }
    
    public static void updateCocoaPath(String oldpath, String newpath)
    {
    	try
    	{
    		Document doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().
    			parse(System.getProperty("user.home")+"/.chungles/config.xml");
    		NodeList nodes=doc.getFirstChild().getChildNodes();
    		for (int i=0; i<nodes.getLength(); i++)
    		{
    			Node n=nodes.item(i);
    			
    			if (n.getNodeName().equals("plugin"))
    			{
    				NamedNodeMap nnm=n.getAttributes();
    				for (int j=0; j<nnm.getLength(); j++)
    				{
    					Node attr=nnm.item(j);
    					if (attr.getNodeValue().equals(oldpath))
    					{
    						attr.setNodeValue(newpath);
    						j=nnm.getLength();
    						i=nodes.getLength();
    					}
    				}
    			}
    		}
    		
    		Source source = new DOMSource(doc);    	    
            File file = new File(System.getProperty("user.home")+"/.chungles/config.xml");
            Result result = new StreamResult(file);
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    }
}
