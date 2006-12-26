package org.chungles.ui.webdav;

import java.io.IOException;
import java.net.ServerSocket;

import org.chungles.plugin.*;

public class WebDAVUI implements UIPlugin
{
    public boolean done=false;
    int port=6566;
    
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
        ServerSocket serverSocket = null;
        
        try
        {
            serverSocket = new ServerSocket(port);
            while (!done)
            {
                new HTTPConnection(serverSocket.accept()).start();
            }
            serverSocket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }   
    }

    public void shutdown()
    {
        done=true;
    }

}
