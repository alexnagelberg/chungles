package org.chungles.plugin;

import java.util.*;

import org.chungles.core.*;
import org.chungles.application.*;

public class FileSystem
{
    private String workingdirectory="/";
    private Client client;
    
    public void changeDirectory(String path) throws PathNotExistException
    {
        if (path.substring(0,1).equals("/")) // Absolute path
        {
            changeAbsolutePath(path);
        }
        else // Relative path
        {       
            /* 
             * This piece of code was fun. It tokenizes the current working path
             * with / as the delimiting character and pushes each token in to a stack.
             * Then it tokenizes the relative path, and checks each token. If the token
             * is a "..", it pops a string off the stack. Otherwise it adds the token
             * to the stack. It then pops the whole stack to an absolute path name. ^_^
             */
            Stack<String> pathstack=new Stack<String>();
            StringTokenizer tok=new StringTokenizer(workingdirectory, "/");
            while (tok.hasMoreElements())
            {
                pathstack.push(tok.nextToken());
            }
            
            tok=new StringTokenizer(path, "/");
            while (tok.hasMoreElements())
            {
                String curTok=tok.nextToken();
                if (curTok.equals("..") && !pathstack.isEmpty())                    
                    pathstack.pop();
                else
                    pathstack.push(curTok);
            }
            
            path="";
            while (!pathstack.isEmpty())
            {
                path=pathstack.pop() + "/" + path;
            }
            
            path="/"+path;
            changeAbsolutePath(path);
        }        
    }
    
    private void changeAbsolutePath(String path) throws PathNotExistException
    {
        if (client!=null)
        {
            client.close();
            client=null;
        }                
        
        if (path.length()>1)
        {
            Enumeration<String> compnames=NodeDetect.compnames.elements();
            boolean sharematch=false;
            while (compnames.hasMoreElements())
            {
                String compname1=compnames.nextElement();
                StringTokenizer tok=new StringTokenizer(path, "/");            
                String compname2=tok.nextToken();
                if (compname1.equals(compname2))
                    sharematch=true;
            }
            
            if (!sharematch)
                throw new PathNotExistException();
            
            StringTokenizer tok=new StringTokenizer(path.substring(1),"/");                     
            String compname=tok.nextToken();                
            client=new Client(NodeDetect.ips.get(compname));
            if (!tok.hasMoreElements() || client.pathExists(path.substring(compname.length()+1)))
            {
                workingdirectory=path;
            }
            else
            {
                throw new PathNotExistException();
            }
        }
        else if (path.equals("/"))
            workingdirectory=path;
        else
            throw new PathNotExistException();
    }
    
    public String[] listPath()
    {        
        if (workingdirectory.equals("/"))
        {
            
            Enumeration<String> compenum=NodeDetect.compnames.elements();
            String[] listing=new String[NodeDetect.compnames.size()];
            int i=0;
            while(compenum.hasMoreElements())
            {               
                String name=compenum.nextElement();
                listing[i++]="D"+name;
            }
            return listing;
        }
        else
        {
            if (client!=null)
            {
                int start=workingdirectory.indexOf('/', 1);
                LinkedList<String> listing;
                if (start==-1)
                    listing=client.listDir("/");
                else
                {
                    String path=workingdirectory.substring(start);
                    if (path.charAt(path.length()-1)!='/')
                        path+="/";
                    listing=client.listDir(path);
                }
                
                return listing.toArray(new String[0]);
            }
            else
                return null;
        }
    }
    
    public String getWorkingDirectory()
    {
        return workingdirectory;
    }
    
}
