package org.chungles.plugin;

import java.util.*;
import java.util.jar.*;
import java.io.*;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.chungles.core.*;

public class PluginAction
{
	private static Thread main_thread;
	//TODO: add Notification type plugin
	
    public static void loadPlugin(String path, boolean enabled)
    {
        try
        {
            JarFile jar=new JarFile(path);
            InputStream in=jar.getInputStream(jar.getJarEntry("config.xml"));
            Document doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);            
            String main="",type="";
    		NodeList nodes=doc.getFirstChild().getChildNodes();
    		LinkedList<String> classes=new LinkedList<String>();
    		classes.add(path);
    		for (int i=0; i<nodes.getLength(); i++)
    		{
    			Node node=nodes.item(i);
    			if (node.getNodeName().equals("class"))
    			{
    				NamedNodeMap nnm=node.getAttributes();
                	for (int j=0; j<nnm.getLength(); j++)
                	{
                		Node n=nnm.item(j);
                		if (n.getNodeName().equals("main"))
                		{
                			main=n.getNodeValue();
                			if (findPlugin(main)!=null)
                                return;
                		}
                		else if (n.getNodeName().equals("type"))
                			type=n.getNodeValue();
                	}
    			}
    			else if (node.getNodeName().equals("classpath"))
    			{
    				NamedNodeMap nnm=node.getAttributes();
    				for (int j=0; j<nnm.getLength(); j++)
    				{
    					Node n=nnm.item(j);
    					if (n.getNodeName().equals("value"))
    					{
    						String classpath=n.getNodeValue();
    						if (!new File(classpath).exists())
    		            	{
    		            		int offset=path.lastIndexOf(File.separatorChar);
    		            		if (offset>=0)
    		            			classpath=path.substring(0,offset+1)+classpath;            			
    		            	}
    						classes.add(classpath);
    					}
    						
    				}
    			}	
    		}            
            in.close();      
            JARClassLoader loader=new JARClassLoader(classes.toArray(new String[classes.size()]));
            
            final StandardPlugin p=(StandardPlugin)loader.getClass(main).newInstance();
            if (enabled)
            {
                new Thread()
                {
                    public void run()
                    {
                        p.init();
                    }
                }.start();
            }
                        
            
            if (type.compareToIgnoreCase("ui")==0)
            {
                Configuration.UIplugins.add(new PluginInfo<UIPlugin>(main, path, (UIPlugin)p, enabled, PluginInfo.UI));
            }
            else
                Configuration.otherplugins.add(new PluginInfo<StandardPlugin>(main, path, p, enabled, PluginInfo.OTHER));
        }        
        catch (Exception e)
        {
            e.printStackTrace();
        }
    } 
    
    public static void unloadPlugins()
    {
        Iterator<PluginInfo<UIPlugin>> iter1=Configuration.UIplugins.iterator();
        while (iter1.hasNext())
        {
            PluginInfo<UIPlugin> p=iter1.next();
            if (p.isEnabled())
                p.getPlugin().shutdown();
        }
        
        Iterator<PluginInfo<StandardPlugin>> iter2=Configuration.otherplugins.iterator();
        while (iter2.hasNext())
        {
            PluginInfo <StandardPlugin> p=iter2.next();
            if (p.isEnabled())
                p.getPlugin().shutdown();
        }
    }
    
    public static void loadPlugins()
    {
    	File pluginsdir=new File(System.getProperty("user.home")+"/.chungles/plugins");
    	if (!pluginsdir.exists())
    		pluginsdir.mkdir();
    	
        File[] files=pluginsdir.listFiles();        
        for (int i=0; i<files.length; i++)
        {
            String name=files[i].getAbsolutePath();
            String ext=name.substring(name.length()-4);
            if (ext.compareToIgnoreCase(".jar")==0)
                loadPlugin(name, true);
        }
    }
    
    public static void openPreferencesDialog()
    {
        Iterator<PluginInfo<UIPlugin>> iter=Configuration.UIplugins.iterator();
        while (iter.hasNext())
        {
            PluginInfo<UIPlugin> p=iter.next();
            if (p.isEnabled())
                p.getPlugin().openPreferencesDialog();
        }
    }
    
    /*public static void finishnotification(boolean success, String message)
    {
        Iterator<PluginInfo<UIPlugin>> iter=Configuration.UIplugins.iterator();
        while (iter.hasNext())
        {
            PluginInfo<UIPlugin> p=iter.next();
            if (p.isEnabled())
                p.getPlugin().finishnotification(success, message);
        }
    }*/
    
    public static void mainloop()
    {
        main_thread=Thread.currentThread();
    	try
    	{
    		synchronized (main_thread)
    		{
    			main_thread.wait();
    		}
    	}
    	catch (InterruptedException e)
    	{
    		// shutdown
    		Iterator<PluginInfo<UIPlugin>> iter=Configuration.UIplugins.iterator();
    		while (iter.hasNext())
    		{
    			PluginInfo<UIPlugin> p=iter.next();
    			if (p.isEnabled())
    				p.getPlugin().shutdown();
    		}
    	}
    }
    
    public static void addNode(String IP, String compname)
    {
        Iterator<PluginInfo<UIPlugin>> iter=Configuration.UIplugins.iterator();
        while (iter.hasNext())
        {
            PluginInfo<UIPlugin> p=iter.next();
            if (p.isEnabled())
                p.getPlugin().addNode(IP, compname);
        }
    }
    
    public static void removeNode(String IP, String compname)
    {
        Iterator<PluginInfo<UIPlugin>> iter=Configuration.UIplugins.iterator();
        while (iter.hasNext())
        {
            PluginInfo<UIPlugin> p=iter.next();
            if (p.isEnabled())
                p.getPlugin().removeNode(IP, compname);
        }
    }
    
    public static void shutdown()
    {
    	main_thread.interrupt();
    }
    
    public static void initPlugin(String main)
    {        
        final PluginInfo p=findPlugin(main);
        if (p!=null)
        {
            p.setEnabled(true);
            new Thread()
            {
                public void run()
                {
                    ((StandardPlugin)p.getPlugin()).init();
                }
            }.start();
            
        }
    }
    
    public static void shutdownPlugin(String main)
    {
        PluginInfo p=findPlugin(main);
        if (p!=null)
        {
            p.setEnabled(false);
            ((StandardPlugin)p.getPlugin()).shutdown();
        }
    }
    
    public static void removePlugin(String main)
    {
    	PluginInfo p=findPlugin(main);        
        
        if (Configuration.UIplugins.contains(p))
        	Configuration.UIplugins.remove(p);
        else
        	Configuration.otherplugins.remove(p);
    }
    
    public static PluginInfo findPlugin(String main)
    {
        Iterator iter=Configuration.UIplugins.iterator();
        while (iter.hasNext())
        {
            PluginInfo p=(PluginInfo)iter.next();
            if (p.getMainClass().equals(main))
                return p;
        }
        iter=Configuration.otherplugins.iterator();
        while (iter.hasNext())
        {
            PluginInfo p=(PluginInfo)iter.next();
            if (p.getMainClass().equals(main))
                return p;
        }
        
        return null;
    }
    
    public static boolean JARConflicts(String path)
    {
    	try
    	{
	    	JarFile jar=new JarFile(path);
	        InputStream in=jar.getInputStream(jar.getJarEntry("config.xml"));
	        Document doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
	        Node node=doc.getFirstChild().getFirstChild();
            while (!node.getNodeName().equals("class"))
                    node=node.getNextSibling();
            NamedNodeMap nnm=node.getAttributes();
            String main="";
            for (int i=0; i<nnm.getLength(); i++)
            {
                    Node n=nnm.item(i);
                    if (n.getNodeName().equals("main"))
                    	main=n.getNodeValue();
            }	        
	        in.close();            
        
	        return findPlugin(main)!=null;
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	return false;
    }
    
    public static PluginInfo PeekInJAR(String path)
    {
    	try
    	{
	    	JarFile jar=new JarFile(path);
	        InputStream in=jar.getInputStream(jar.getJarEntry("config.xml"));
	        Document doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
	        Node node=doc.getFirstChild().getFirstChild();
            while (!node.getNodeName().equals("class"))
                    node=node.getNextSibling();
            NamedNodeMap nnm=node.getAttributes();
            String main="",type="";
            for (int i=0; i<nnm.getLength(); i++)
            {
                    Node n=nnm.item(i);
                    if (n.getNodeName().equals("main"))
                    	main=n.getNodeValue();
                    else if (n.getNodeName().equals("type"))
                    	type=n.getNodeValue();
            }	        
	        in.close();            
	        
	        if (type.compareToIgnoreCase("ui")==0)	        
	        	return new PluginInfo<Object>(main, path, null, false, PluginInfo.UI);
	        else
	        	return new PluginInfo<Object>(main, path, null, false, PluginInfo.OTHER);
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    	return null;
    }
    
    public static void notify(int type, String message)
    {
    	Iterator<PluginInfo<UIPlugin>> iter=Configuration.UIplugins.iterator();
    	while (iter.hasNext())
    	{
    		PluginInfo<UIPlugin> p=iter.next();
    		if (p.isEnabled())
    			p.getPlugin().notify(type, message);
    	}
    	
        Iterator<PluginInfo<StandardPlugin>> iter2=Configuration.otherplugins.iterator();
        while (iter2.hasNext())
        {
            PluginInfo<StandardPlugin> p=iter2.next();
            if (p.isEnabled())
                p.getPlugin().notify(type, message);
        }
    }
}
