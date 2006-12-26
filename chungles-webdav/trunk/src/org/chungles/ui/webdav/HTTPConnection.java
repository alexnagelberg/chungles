package org.chungles.ui.webdav;

import java.net.*;
import java.io.*;
import java.util.*;
import org.chungles.plugin.*;

public class HTTPConnection extends Thread
{
    private Socket sock;
    
    public HTTPConnection(Socket socket)
    {
        super("HTTPConnection");
        sock=socket;
    }
    
    public void run()
    {
        try
        {
            BufferedReader read=new BufferedReader(new InputStreamReader(sock.getInputStream()));
            boolean firstline=true;
            String firstlin="";
            while (read.ready())
            {
                String line=read.readLine();
                if (firstline)
                    firstlin=line;                    
                firstline=false;
                
            }
            parseCommand(firstlin);
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void parseCommand(String line)
    {
        StringTokenizer tok=new StringTokenizer(line);
        String command=tok.nextToken();
        String path="";
        if (tok.hasMoreTokens())
            path=tok.nextToken();
        
        if (command.equals("GET"))
            GET(path);
        else if (command.equals("PUT"))
            PUT(path);
    }
    
    private void GET(String path)
    {
        // Test
        try
        {
            OutputStream out=sock.getOutputStream();
            
            FileSystem fs=new FileSystem();
            fs.changeDirectory(path);
            String[] list=fs.listPath();
            
            String html="<html><body>";
            for (int i=0; i<list.length; i++)
            {
                String name=list[i].substring(1);
                html+="<a href=\""+name+"/\">"+"("+list[i].substring(0, 1)+") "+name+"</a><br/>";
            }
            html+="</body></html>";
            
            String response="HTTP/1.x 200 OK\n" +
            "Content-Length: " + html.length() + "\n" +
            "Content-Type: text/html\n" +
            "\n"+html;
            out.write(response.getBytes());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void PUT(String path)
    {
        
    }
}
