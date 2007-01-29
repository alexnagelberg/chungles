package org.chungles.ui.webdav;

import org.chungles.plugin.*;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.mortbay.jetty.*;
import org.mortbay.jetty.handler.AbstractHandler;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

public class HTTPConnection extends AbstractHandler
{
    private void NOTFOUND(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException
    {
        res.setStatus(404);
        res.getWriter().write("Not Found.");
    }
    
    private void FORBIDDEN(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        res.setStatus(403);        
    }
    
    private void MOVED(HttpServletRequest req, HttpServletResponse res, String location) throws ServletException, IOException
    {
    	res.setStatus(301);
    	res.setHeader("Location", location);
    	res.setContentType("text/html");
    	res.getWriter().write("<html><body>Document has moved to <a href=\""+location+"\">"+location+"</a></body></html>");
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException    
    {        
        try
        {
            String path=new URI(req.getRequestURI()).getPath();
            HTTPGet httpget=new HTTPGet();
            if (path.length()>=7 && path.substring(0, 7).equals("/images"))
            {
                if (!httpget.getImage(path.substring(1), res))
                    NOTFOUND(req, res);
                return;
            }
            
            FileSystem fs=new FileSystem();
            if (fs.isPathDirectory(path) && !path.substring(path.length()-1).equals("/"))
            {
            	MOVED(req, res, req.getRequestURI()+"/");
            	return;
            }
            
            if (fs.isPathDirectory(path))
                httpget.listDirectory(path, res);
            else if (fs.isPathFile(path))
                httpget.getFile(path, res);
            else
                NOTFOUND(req, res);
            
        }
        catch (PathNotExistException e)
        {
            NOTFOUND(req, res);
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }
    
    public void doOptions(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException
    {
        res.setHeader("DAV", "1, 2");
        res.setHeader("Allow", "OPTIONS,GET,PROPFIND");
    }
    
    public void doPropFind(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException
    {
    	try
    	{
    		int depth=Integer.parseInt(req.getHeader("Depth"));
            if (depth!=0 && depth!=1)
            {
                FORBIDDEN(req, res);
                return;
            }
    		
            String path=new URI(req.getRequestURI()).getPath();
            FileSystem fs=new FileSystem();
            if (fs.isPathDirectory(path) && !path.substring(path.length()-1).equals("/"))
            {
            	MOVED(req, res, req.getRequestURI()+"/");
            	return;
            }
            
	    	String responseBody="";
	    	
	    	if (req.getContentLength()<=0)
	    	{
	    		responseBody=DAVPropFind.getAllProperties(path, depth);	    		
	    	}
	    	else
	    	{
	    		InputStream in=req.getInputStream();
	    		Document doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
				XPath xpath=XPathFactory.newInstance().newXPath();
				Node papa=((Node)xpath.evaluate("/propfind", doc, XPathConstants.NODE));
				if (papa==null)
				{
					responseBody=DAVPropFind.getAllProperties(path, depth);					
				}
				else
				{
					NodeList list=papa.getChildNodes();
					for (int i=0; i<list.getLength(); i++)
					{
						Node item=list.item(i);
						if (item.getNodeType()==Node.ELEMENT_NODE)
						{
							if (item.getNodeName().indexOf("prop")>=0)
								responseBody=DAVPropFind.getProperties(item.getChildNodes(), path, depth);
							else if (item.getNodeName().indexOf("allprop")>=0)
								responseBody=DAVPropFind.getAllProperties(path, depth);
							else if (item.getNodeName().indexOf("propname")>=0)
								responseBody=DAVPropFind.getPropertyNames(item.getChildNodes(), path, depth);
							i=list.getLength();
						}						
					}
				}								
	    	}
	    	if (responseBody==null || responseBody.equals(""))
	    		NOTFOUND(req, res);
	    	else
	    	{
	    		res.setStatus(207);
				res.setContentType("text/xml");
				res.getWriter().write(responseBody);
	    	}
    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    }
    
    public void handle(String target, HttpServletRequest req, HttpServletResponse res, int dispatch) throws IOException, ServletException
    {
        res.setDateHeader("Date", new Date().getTime());
        if (req.getMethod().equals("GET"))
            doGet(req, res);
        else if (req.getMethod().equals("OPTIONS"))
            doOptions(req, res);
        else if (req.getMethod().equals("PROPFIND"))
            doPropFind(req, res);
        else
            res.setStatus(405);
        ((Request)req).setHandled(true);
    }
    
    
}
