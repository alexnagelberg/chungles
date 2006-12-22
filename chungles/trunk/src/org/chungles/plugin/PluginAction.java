package org.chungles.plugin;

import java.util.*;
import java.util.jar.*;
import java.io.*;
import java.lang.reflect.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

public class PluginAction
{
    private static LinkedList<UIPlugin> UIs;
    private static LinkedList<StandardPlugin> others;
    
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
            String classpath=main.replace('.', '/')+".class";
            in=jar.getInputStream(jar.getEntry(classpath));
            byte buf[]=new byte[100024];
            int read=in.read(buf, 0, 100024);
            in.close();
            
            // Hack around protected method
            Class[] parameters = new Class[]{String.class, byte[].class, int.class, int.class};
            Method method = ClassLoader.class.getDeclaredMethod("defineClass",parameters);
            method.setAccessible(true);
            Class c=(Class)method.invoke(ClassLoader.getSystemClassLoader(),new Object[]{ main, buf, 0, read });
            
            final StandardPlugin p=(StandardPlugin)c.newInstance();
            new Thread()
            {
                public void run()
                {
                    p.init();
                }
            }.start();
            
            if (UIs==null || others==null)
            {
                UIs=new LinkedList<UIPlugin>();
                others=new LinkedList<StandardPlugin>();
            }
            
            if (type.compareToIgnoreCase("ui")==0)
                UIs.add((UIPlugin)p);
            else
                others.add(p);                        
        }        
        catch (Exception e)
        {
            e.printStackTrace();
        }
    } 
    
    public static void unloadPlugins()
    {
        Iterator<UIPlugin> iter1=UIs.iterator();
        while (iter1.hasNext())
        {
            UIPlugin p=iter1.next();
            p.shutdown();
            UIs.remove(p);
        }
        
        Iterator<StandardPlugin> iter2=others.descendingIterator();
        while (iter2.hasNext())
        {
            StandardPlugin p=iter2.next();
            p.shutdown();
            others.remove(p);
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
        Iterator<UIPlugin> iter=UIs.iterator();
        while (iter.hasNext())
        {
            iter.next().openPreferencesDialog();
        }
    }
    
    public static void finishnotification(boolean success, String message)
    {
        Iterator<UIPlugin> iter=UIs.iterator();
        while (iter.hasNext())
        {
            iter.next().finishnotification(success, message);
        }
    }
    
    public static void mainloop()
    {
        boolean done=false;
        while (!done)
        {
            Iterator<UIPlugin> iter=UIs.iterator();
            while (iter.hasNext() && !done)
            {
                done=iter.next().isDone();
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
        Iterator<UIPlugin> iter=UIs.iterator();
        while (iter.hasNext())
        {
            iter.next().shutdown();
        }
    }
    
    public static void addNode(String IP, String compname)
    {
        Iterator<UIPlugin> iter=UIs.iterator();
        while (iter.hasNext())
        {
            iter.next().addNode(IP, compname);
        }
    }
    
    public static void removeNode(String IP, String compname)
    {
        Iterator<UIPlugin> iter=UIs.iterator();
        while (iter.hasNext())
        {
            iter.next().removeNode(IP, compname);
        }
    }
    
    public static void main(String[] args)
    {
        loadPlugin("/home/alex/test.jar");
    }
}
