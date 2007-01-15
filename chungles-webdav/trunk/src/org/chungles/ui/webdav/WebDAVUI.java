package org.chungles.ui.webdav;

import org.chungles.plugin.*;
import org.mortbay.jetty.Server;

public class WebDAVUI implements UIPlugin
{
    private boolean done=false;
    private int port=6566;
    private Server server;
    
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
            server=new Server(port);
            server.setHandler(new HTTPConnection());
            server.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            done=true;
        }
    }

    public void shutdown()
    {
        done=true;
        try
        {
            server.stop();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
