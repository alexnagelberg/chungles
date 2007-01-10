package org.chungles.ui.webdav;

import org.chungles.plugin.*;
import com.sun.net.httpserver.*;
import java.io.*;

public class HTTPConnection implements HttpHandler 
{
    public void handle(HttpExchange exchange)
    {
    	String command=exchange.getRequestMethod();
    	if (command.equals("GET"))
    		GET(exchange);
    }
    
    private void GET(HttpExchange exchange)
    {
    	String path=exchange.getRequestURI().getPath();
    	if (path.indexOf(".gif")>0)
    	{
    		try
    		{    			
	    		InputStream in=getClass().getClassLoader().getResourceAsStream("images"+path);
	    		if (in==null)
	    			NOTFOUND(exchange);
	    		else
	    		{
	    			exchange.sendResponseHeaders(200, 0);
	    			OutputStream out=exchange.getResponseBody();	    			
	    			byte buf[]=new byte[1024];
	    			int read;
	    			do
	    			{
	    				read=in.read(buf, 0, 1024);
	    				if (read>0)
	    					out.write(buf, 0, read);
	    			}
	    			while (read>0);
	    			out.close();
	    		}
    		}
    		catch (Exception e)
    		{
    			e.printStackTrace();
    		}
    	}
    	else
    	{
	    	try
	    	{
		    	FileSystem fs=new FileSystem();
		    	fs.changeDirectory(path);
		    	String[] list=fs.listPath();
		    	
		    	try
		    	{
			    	String html="<html><body>";
			        for (int i=0; i<list.length; i++)
			        {
			            String name=list[i].substring(1);
			            if (list[i].substring(0,1).equals("D"))
			            	html+="<img src='/folder.gif' alt='[DIR]'/>";
			            else
			            	html+="<img src='/file.gif'/>";
			            html+="<a href=\""+name+"/\">"+name+"</a><br/>";
			        }
			        html+="</body></html>";
			        exchange.sendResponseHeaders(200, 0);
			        OutputStream out=exchange.getResponseBody();
			        out.write(html.getBytes());
			        out.close();
		    	}
		    	catch (Exception e)
		    	{
		    		e.printStackTrace();
		    	}	        
	    	}
	    	catch (PathNotExistException e)
	        {
	            NOTFOUND(exchange);
	        }
    	}
    }
    
    private void NOTFOUND(HttpExchange exchange)
    {
    	try
    	{
	    	exchange.sendResponseHeaders(404, 0);
	    	OutputStream out=exchange.getResponseBody();
	    	out.write(("<html><head><title>Not Found</title>"+
	    			"</head><body><font size=3>Not Found.</font></body></html>").getBytes());
	    	out.close();
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    }
}
