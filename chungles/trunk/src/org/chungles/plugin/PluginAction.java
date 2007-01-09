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
    public static void loadPlugin(String path, boolean enabled)
    {
        try
        {
            JarFile jar=new JarFile(path);
            InputStream in=jar.getInputStream(jar.getJarEntry("config.xml"));
            Document doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
            XPath xpath=XPathFactory.newInstance().newXPath();
            String type=((Node)xpath.evaluate("/plugin/class/@type", doc, XPathConstants.NODE)).getNodeValue();
            String main=((Node)xpath.evaluate("/plugin/class/@main", doc, XPathConstants.NODE)).getNodeValue();
            NodeList classpaths=(NodeList)xpath.evaluate("/plugin/classpath", doc, XPathConstants.NODESET);
            in.close();            
            
            if (findPlugin(main)!=null)
                return;
           
            String[] classes=new String[classpaths.getLength()+1];
            classes[0]=path;
            for (int i=1;i<=classpaths.getLength(); i++)
            {
            	String classpath=((Node)xpath.evaluate("./@value", classpaths.item(i-1), XPathConstants.NODE)).getNodeValue();
            	// If required classpath is relative, and plugin is absolute, set to same directory
            	// as plugin
            	if (classpath.indexOf(File.pathSeparatorChar)<0)
            	{
            		int offset=path.lastIndexOf(File.pathSeparatorChar);
            		if (offset>=0)
            			classpath=path.substring(0,offset)+classpath;            			
            	}
            	classes[i]=classpath;           	
            }            
            JARClassLoader loader=new JARClassLoader(classes);
            
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
    
    public static void finishnotification(boolean success, String message)
    {
        Iterator<PluginInfo<UIPlugin>> iter=Configuration.UIplugins.iterator();
        while (iter.hasNext())
        {
            PluginInfo<UIPlugin> p=iter.next();
            if (p.isEnabled())
                p.getPlugin().finishnotification(success, message);
        }
    }
    
    public static void mainloop()
    {
        boolean done=false;
        while (!done)
        {
            Iterator<PluginInfo<UIPlugin>> iter=Configuration.UIplugins.iterator();
            while (iter.hasNext() && !done)
            {
                PluginInfo<UIPlugin> p=iter.next();
                if (p.isEnabled())
                    done=p.getPlugin().isDone();
            }
            try
            {
                Thread.sleep(50);
            }
            catch (Exception e)
            {
                
            }
        }
        
        // shutdown
        Iterator<PluginInfo<UIPlugin>> iter=Configuration.UIplugins.iterator();
        while (iter.hasNext())
        {
            PluginInfo<UIPlugin> p=iter.next();
            if (p.isEnabled())
                p.getPlugin().shutdown();
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
    
    /*private static void addClasspath(String path, JARClassLoader loader)
    {
        try
        {
            Class[] parameters = new Class[]{URL.class};
            URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
            Class<URLClassLoader> sysclass = URLClassLoader.class;
            
            Method method = sysclass.getDeclaredMethod("addURL",parameters);
            method.setAccessible(true);
            method.invoke(sysloader,new Object[]{ new File(path).toURI().toURL() });
        	
        	
        	
        }
        catch (Exception e)
        {
        }
    }*/
    
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
	        XPath xpath=XPathFactory.newInstance().newXPath();	        
	        String main=((Node)xpath.evaluate("/plugin/class/@main", doc, XPathConstants.NODE)).getNodeValue();	        
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
	        XPath xpath=XPathFactory.newInstance().newXPath();
	        String type=((Node)xpath.evaluate("/plugin/class/@type", doc, XPathConstants.NODE)).getNodeValue();
	        String main=((Node)xpath.evaluate("/plugin/class/@main", doc, XPathConstants.NODE)).getNodeValue();	        
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
}
