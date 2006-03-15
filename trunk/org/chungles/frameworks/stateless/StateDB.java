package org.chungles.frameworks.stateless;

import java.io.*;
import java.util.*;

class StateDB
{
	private Hashtable db;
	private String path;
    
    private StateDB()
    {
    }
    
    /**
     * Constructor
     * 
     * @param path Path for DB storage.
     */
	public StateDB(String path) throws IOException, ClassNotFoundException
	{
        this.path=path;
		db=new Hashtable();
        
        try
        {
            FileInputStream in=new FileInputStream(path);
            ObjectInputStream oin=new ObjectInputStream(in);
            try
            {
                while (true)
                {      
                    StatelessApplication app=(StatelessApplication)oin.readObject();
                    addApp(app);
                }
            }
            catch (EOFException e)
            {
               oin.close();
               in.close();
            }            
        }
        catch (FileNotFoundException e)
        {
            // no worries
        }
        catch (ApplicationAlreadyRegisteredException e)
        {
            System.out.println("This should not happen! A duplicate application id was found in db.");
        }
	}
	
    /**
     * Register application with DB
     * 
     * @param app Application to register
     * @throws ApplicationAlreadyRegisteredException when an application with the same ID is in the DB
     */
	public void addApp(StatelessApplication app) throws ApplicationAlreadyRegisteredException
	{
		if (db.get(app.getAppID())!=null)
            throw new ApplicationAlreadyRegisteredException();
        else            
            db.put(app.getAppID(), app);
	}
	
    /**
     * Retrieve application from the DB
     * 
     * @param id Application's ID
     * @return The application
     * @throws NoSuchApplicationException when application is not in DB
     */
	public StatelessApplication getApp(String id) throws NoSuchApplicationException
	{
		try
        {
		    return (StatelessApplication)db.get(id);
        }
        catch (NullPointerException e)
        {
            throw new NoSuchApplicationException();
        }
	}
    
    /**
     * Writes the DB as a serialized object stream to disk.
     * 
     * @throws IOException
     */
    public void writeToDisk() throws IOException
    {
        FileOutputStream out=new FileOutputStream(path);
        ObjectOutputStream oout=new ObjectOutputStream(out);
        
        Enumeration elements=db.elements();
        while (elements.hasMoreElements())
        {
            oout.writeObject(elements.nextElement());
        }
        out.close();
    }
}
