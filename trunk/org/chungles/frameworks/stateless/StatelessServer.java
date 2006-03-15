package org.chungles.frameworks.stateless;

import java.io.*;

public class StatelessServer
{
    private final int LISTEN_API=0;
    private final int LISTEN_TCP=1;
    
    private StateDB db;
    private int listenType;
    
    /**
     * Constructor for serving via API
     *
     */
    public StatelessServer() throws IOException, ClassNotFoundException
    {
        String fileSeperator=System.getProperty("file.seperator");
        String dbpath=System.getProperty("user.home")+fileSeperator+"stateless.db";
                
        db=new StateDB(dbpath);
        listenType=LISTEN_API;
    }

    /**
     * Constructor for serving via TCP
     * 
     * @param port Port number to listen on
     */
    public StatelessServer(int port)
    {
        listenType=LISTEN_TCP;
    }
    
    /**
     * Shutdown routine. Save DB to disk.
     *
     */
    public void shutdown()
    {
        try
        {
            db.writeToDisk();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
