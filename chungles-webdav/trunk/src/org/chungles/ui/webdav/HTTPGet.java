package org.chungles.ui.webdav;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.servlet.http.*;

import org.chungles.core.FileList;
import org.chungles.plugin.*;

public class HTTPGet
{
    public boolean getImage(String path, HttpServletResponse res)
    {
        try
        {
            InputStream in=getClass().getClassLoader().getResourceAsStream(path);
            if (in==null)
                return false;
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
                return true;
            }
        }
        catch (Exception e)
        {
            res.setContentType("");
            e.printStackTrace();
        }
        return false;
    }
    
    public void listDirectory(String path, HttpServletResponse res) throws PathNotExistException
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
                    html+="<img src='/images/folder.gif' alt='[DIR]'/>";
                else
                    html+="<img src='/images/file.gif'/>";
                String encoded=new URI(null, null, name, null).getRawPath();
                html+="<a href=\""+encoded+"\">"+name+"</a><br/>";
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
    
    public void getFile(String path, HttpServletResponse res) throws PathNotExistException
    {
        FileSystem fs=new FileSystem();
        FileList file=fs.getFileInfo(path);        
        
        if (file==null)
            throw new PathNotExistException();
        
        InputStream in=fs.getFileStream(path);
        long filesize=file.getSize();
        res.setContentType("application/unknown");
        res.setContentLength((int)filesize);
        try
        {
            OutputStream out=res.getOutputStream();
            int totalread=0;
            while (totalread<filesize)
            {
                byte[] buffer=new byte[1024];
                int read;
                if (filesize-totalread<1024)
                {
                    //conversion of long to int ok, it's under 1024 ;)
                    read=in.read(buffer, 0, (int)(filesize-totalread));                    
                }
                else
                {
                    read=in.read(buffer);
                }
                out.write(buffer, 0, read);
                totalread+=read;            
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
