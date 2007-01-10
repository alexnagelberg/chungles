package org.chungles.ui.webdav;

import java.net.InetSocketAddress;

import com.sun.net.httpserver.*;
import org.chungles.plugin.*;

public class WebDAVUI implements UIPlugin
{
    public boolean done=false;
    int port=6566;
    HttpServer server;
    
    public void addNode(String IP, String compname)
    {
        
    }

    public void finishnotification(boolean success, String message)
    {
        
    }

    public boolean isDone()
    {
        return done;
    }

    public void openPreferencesDialog()
    {
        
    }

    public void removeNode(String IP, String compname)
    {
        
    }

    public String getAuthor()
    {
        return "Alex Nagelberg";
    }

    public String getName()
    {
        return "WebDAV UI";
    }

    public String getVersion()
    {
        return "0.1";
    }

    public void init()
    {
    	try
    	{    		
    		server=HttpServer.create(new InetSocketAddress(port), 10);
    		server.createContext("/", new HTTPConnection());
    		server.start();
    		System.out.println("Chungles-DAV: Listening on port: " + port);
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();    		
    	}
        
    }

    public void shutdown()
    {
        done=true;
        server.stop(0);
    }

}
