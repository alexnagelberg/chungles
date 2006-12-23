package org.chungles.plugin;

import java.util.*;
import java.util.jar.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.chungles.core.*;

public class PluginAction
{
    public static void loadPlugin(String path)
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
            
            PluginInfo pluginInfo=new PluginInfo<Object>(main, path, null);
            if (Configuration.UIplugins.contains(pluginInfo) || Configuration.otherplugins.contains(pluginInfo))
                return;
            
            Class[] parameters = new Class[]{URL.class};
            URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
            Class<URLClassLoader> sysclass = URLClassLoader.class;
            
            Method method = sysclass.getDeclaredMethod("addURL",parameters);
            method.setAccessible(true);
            method.invoke(sysloader,new Object[]{ new File(path).toURI().toURL() });
            
            final StandardPlugin p=(StandardPlugin)Class.forName(main).newInstance();
            new Thread()
            {
                public void run()
                {
                    p.init();
                }
            }.start();
                        
            
            if (type.compareToIgnoreCase("ui")==0)
            {
                Configuration.UIplugins.add(new PluginInfo<UIPlugin>(main, path, (UIPlugin)p));
            }
            else
                Configuration.otherplugins.add(new PluginInfo<StandardPlugin>(main, path, p));
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
            UIPlugin p=iter1.next().getPlugin();
            p.shutdown();
        }
        
        Iterator<PluginInfo<StandardPlugin>> iter2=Configuration.otherplugins.iterator();
        while (iter2.hasNext())
        {
            StandardPlugin p=iter2.next().getPlugin();
            p.shutdown();
        }
    }
    
    public static void loadPlugins()
    {
        File[] files=new File(System.getProperty("user.home")+"/.chungles/plugins").listFiles();
        for (int i=0; i<files.length; i++)
        {
            String name=files[i].getAbsolutePath();
            String ext=name.substring(name.length()-4);
            if (ext.compareToIgnoreCase(".jar")==0)
                loadPlugin(name);
        }
    }
    
    public static void openPreferencesDialog()
    {
        Iterator<PluginInfo<UIPlugin>> iter=Configuration.UIplugins.iterator();
        while (iter.hasNext())
        {
            iter.next().getPlugin().openPreferencesDialog();
        }
    }
    
    public static void finishnotification(boolean success, String message)
    {
        Iterator<PluginInfo<UIPlugin>> iter=Configuration.UIplugins.iterator();
        while (iter.hasNext())
        {
            iter.next().getPlugin().finishnotification(success, message);
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
                done=iter.next().getPlugin().isDone();
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
            iter.next().getPlugin().shutdown();
        }
    }
    
    public static void addNode(String IP, String compname)
    {
        Iterator<PluginInfo<UIPlugin>> iter=Configuration.UIplugins.iterator();
        while (iter.hasNext())
        {
            iter.next().getPlugin().addNode(IP, compname);
        }
    }
    
    public static void removeNode(String IP, String compname)
    {
        Iterator<PluginInfo<UIPlugin>> iter=Configuration.UIplugins.iterator();
        while (iter.hasNext())
        {
            iter.next().getPlugin().removeNode(IP, compname);
        }
    }
    
    public static void main(String[] args)
    {
        loadPlugin("/home/alex/test.jar");
    }
}
