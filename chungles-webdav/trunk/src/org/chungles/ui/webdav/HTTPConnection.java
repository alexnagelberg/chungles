package org.chungles.ui.webdav;

import org.chungles.plugin.*;

import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.mortbay.jetty.*;
import org.mortbay.jetty.handler.AbstractHandler;

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
    
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException    
    {    
        
        try
        {
            String path=new URI(req.getRequestURI()).getPath();
            
            if (path.length()>=7 && path.substring(0, 7).equals("/images"))
            {
                InputStream in=getClass().getClassLoader().getResourceAsStream(path.substring(1));
                if (in==null)
                    NOTFOUND(req, res);
                else
                {
                    res.setContentType("image/gif");
                    OutputStream out=res.getOutputStream();
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
                    in.close();
                }
                return;
            }
            
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
                        html+="<img src='/images/folder.gif' alt='[DIR]'/>";
                    else
                        html+="<img src='/images/file.gif'/>";
                    String encoded=new URI(null, null, name, null).getRawPath();
                    html+="<a href=\""+encoded+"/\">"+name+"</a><br/>";
                }
                html+="</body></html>";
                res.setContentType("text/html");                   
                res.getWriter().write(html);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
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
        res.setHeader("DAV", "1");
        res.setHeader("Allow", "OPTIONS,GET,HEAD,POST,DELETE,TRACE,PROPFIND,PROPPATCH,COPY,MOVE,LOCK,UNLOCK");
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
            
            String xml="";
            
            FileSystem fs=new FileSystem();
            try
            {
                String path=new URI(req.getRequestURI()).getPath();
                fs.changeDirectory(path);            
                String workingdir=fs.getWorkingDirectory();                                
                
                
                res.setContentType("text/xml; charset=\"utf-8\"");
                
                xml+="<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
                xml+="<D:multistatus xmlns:D=\"DAV:\">\n";
                
                
                if (depth==1)
                {                    
                        String[] list=fs.listPath();
                        for (int i=0; i<list.length; i++)
                        {
                            xml+="<D:response xmlns:lp1=\"DAV:\">\n";
                            String name=list[i].substring(1);
                            String encoded=new URI(null, null, workingdir+name, null).getRawPath();
                            xml+="<D:href>\n"+encoded+"\n</D:href>\n";
                            xml+="<D:propstat>\n";
                            xml+="<D:prop>\n";                            
                            
                            if (list[i].substring(0,1).equals("D"))
                            {
                                xml+="<lp1:resourcetype>\n";
                                xml+="<D:collection/>\n";
                                xml+="</lp1:resourcetype>\n";
                            }
                            else
                            {
                                xml+="<lp1:resourcetype>\n";                            
                                xml+="application/unknown";
                                xml+="</lp1:resourcetype>\n";                                
                            }                            
                            xml+="</D:prop>\n";
                            xml+="<D:status>\n";
                            xml+="HTTP/1.1 200 OK\n";
                            xml+="</D:status>\n";
                            xml+="</D:propstat>\n";                                                       
                            
                            if (list[i].substring(0, 1).equals("D"))
                            {
                                xml+="<D:propstat>\n";
                                xml+="<D:prop>\n";
                                xml+="<D:getcontentlength/>";
                                xml+="</D:prop>\n";
                                xml+="<D:status>HTTP/1.1 404 Not Found</D:status>\n";
                                xml+="</D:propstat>\n";
                            }
                            else
                            {
                                xml+="<D:propstat>\n";
                                xml+="<D:prop>\n";
                                xml+="<lp1:getcontentlength>0</lp1:getcontentlength>\n";
                                xml+="</D:prop>\n";
                                xml+="<D:status>\n";
                                xml+="HTTP/1.1 200 OK\n";
                                xml+="</D:status>\n";
                                xml+="</D:propstat>\n";
                            }                                                       
                            
                            xml+="<D:propstat>\n";
                            xml+="<D:prop>\n";
                            xml+="<lp1:getlastmodified>Thu, 28 Dec 2006 00:00:00 GMT</lp1:getlastmodified>\n";
                            xml+="</D:prop>\n";
                            xml+="<D:status>\n";
                            xml+="HTTP/1.1 200 OK\n";
                            xml+="</D:status>\n";
                            xml+="</D:propstat>\n";
                            
                            xml+="<D:propstat>\n";
                            xml+="<D:prop>\n";
                            xml+="<lp1:quota/>";
                            xml+="</D:prop>\n";
                            xml+="<D:status>HTTP/1.1 404 Not Found</D:status>\n";
                            xml+="</D:propstat>\n";
                            
                            xml+="<D:propstat>\n";
                            xml+="<D:prop>\n";
                            xml+="<lp1:quotaused/>";
                            xml+="</D:prop>\n";
                            xml+="<D:status>HTTP/1.1 404 Not Found</D:status>\n";
                            xml+="</D:propstat>\n";
                            xml+="</D:response>\n";
                        }                
                }
                else if (depth==0)
                {
                    xml+="<D:response xmlns:lp1=\"DAV:\">\n";
                    String encoded=new URI(null, null, workingdir, null).getRawPath();
                    xml+="<D:href>\n"+encoded+"\n</D:href>\n";
                    
                    xml+="<D:propstat>\n";
                    xml+="<D:prop>\n";
                    xml+="<lp1:getlastmodified>Thu, 28 Dec 2006 00:00:00 GMT</lp1:getlastmodified>\n";
                    xml+="</D:prop>\n";
                    xml+="<D:status>\n";
                    xml+="HTTP/1.1 200 OK\n";
                    xml+="</D:status>\n";
                    xml+="</D:propstat>\n";
                    
                    xml+="<D:propstat>\n";
                    xml+="<D:prop>\n";
                    xml+="<lp1:resourcetype>\n";
                    xml+="<D:collection/>\n";
                    xml+="</lp1:resourcetype>\n";
                    xml+="</D:prop>\n";
                    xml+="<D:status>\n";
                    xml+="HTTP/1.1 200 OK\n";
                    xml+="</D:status>\n";
                    xml+="</D:propstat>\n";
                    
                    xml+="<D:propstat>\n";
                    xml+="<D:prop>\n";
                    xml+="<lp1:getcontentlength/>";
                    xml+="</D:prop>\n";
                    xml+="<D:status>HTTP/1.1 404 Not Found</D:status>\n";
                    xml+="</D:propstat>\n";
                    
                    xml+="<D:propstat>\n";
                    xml+="<D:prop>\n";
                    xml+="<lp1:quota/>";
                    xml+="</D:prop>\n";
                    xml+="<D:status>HTTP/1.1 404 Not Found</D:status>\n";
                    xml+="</D:propstat>\n";
                    
                    xml+="<D:propstat>\n";
                    xml+="<D:prop>\n";
                    xml+="<lp1:quotaused/>";
                    xml+="</D:prop>\n";
                    xml+="<D:status>HTTP/1.1 404 Not Found</D:status>\n";
                    xml+="</D:propstat>\n";
                    xml+="</D:response>\n";
                    
                }                
                xml+="</D:multistatus>\n";                
                res.setStatus(207);
                res.getWriter().write(xml);
            }
            catch (PathNotExistException e)
            {
                NOTFOUND(req, res);             
            }
                        
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public void handle(String target, HttpServletRequest req, HttpServletResponse res, int dispatch) throws IOException, ServletException
    {
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
