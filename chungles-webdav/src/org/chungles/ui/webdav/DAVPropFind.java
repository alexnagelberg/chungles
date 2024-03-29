package org.chungles.ui.webdav;

import java.net.URI;

import org.w3c.dom.*;
import org.chungles.core.FileList;
import org.chungles.plugin.*;

public class DAVPropFind
{
	public static String getAllProperties(String path, int depth)
	{
		String xml="<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n";
		try
		{
			FileSystem fs=new FileSystem();
			if (fs.isPathDirectory(path))
				fs.changeDirectory(path);
			else if (!fs.isPathFile(path))
				return null;
			
			xml+="<D:multistatus xmlns:D=\"DAV:\">\n";			
			if (depth==1)
			{				
				String[] templist=fs.listPath();
				String[] list=new String[templist.length+1];
				list[0]="D";
				System.arraycopy(templist, 0, list, 1, templist.length);
				
                for (int i=0; i<list.length; i++)
                {
                	xml+="<D:response>\n";
                	String name=list[i].substring(1);
                    String encoded=new URI(null, null, path+name, null).getRawPath();
                    xml+="<D:href>"+encoded+"</D:href>\n";
                    xml+="<D:propstat>\n";
                    xml+="<D:prop>\n";
                    
                    if (list[i].substring(0,1).equals("D"))
                    {
                        xml+="<D:resourcetype>\n";
                        xml+="<D:collection/>\n";
                        xml+="</D:resourcetype>\n";
                    }
                    else
                    {
                        xml+="<D:resourcetype>application/unknown</D:resourcetype>\n";                                
                    }
                    
                    if (!list[i].substring(0, 1).equals("D"))
                    {
                    	FileList file=fs.getFileInfo(path+list[i].substring(1));                    	
                    	xml+="<D:getcontentlength>"+file.getSize()+"</D:getcontentlength>\n";
                    }
                    
                    xml+="<D:getlastmodified>Thu, 28 Dec 2006 00:00:00 GMT</D:getlastmodified>\n";
                    xml+="</D:prop>\n";
                    xml+="<D:status>HTTP/1.1 200 OK</D:status>\n";
                    xml+="</D:propstat>\n";
                    xml+="</D:response>\n";
                }
			}
			else
			{
				xml+="<D:response>\n";
                String encoded=new URI(null, null, path, null).getRawPath();
                xml+="<D:href>"+encoded+"</D:href>\n";
                
                xml+="<D:propstat>\n";
                xml+="<D:prop>\n";
                xml+="<D:getlastmodified>Thu, 28 Dec 2006 00:00:00 GMT</D:getlastmodified>\n";
                xml+="<D:resourcetype>\n";
                if (fs.isPathDirectory(path))
	                xml+="<D:collection/>\n";
                else
                	xml+="application/unknown\n";
                xml+="</D:resourcetype>\n";
                xml+="</D:prop>\n";
                xml+="<D:status>HTTP/1.1 200 OK</D:status>\n";
                xml+="</D:propstat>\n";
                xml+="</D:response>\n";
			}
            xml+="</D:multistatus>\n";
		}
		catch (Exception e)
		{
			return null;
		}
		
		return xml;
	}
	
	public static String getProperties(NodeList list, String path, int depth)
	{		
		String xml="<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n";
		
		try
		{
			FileSystem fs=new FileSystem();
			if (fs.isPathDirectory(path))
				fs.changeDirectory(path);
			else if (!fs.isPathFile(path))
				return null;
            
			xml+="<D:multistatus xmlns:D=\"DAV:\">\n";
			String okprops="";
			String badprops="";
				
			for (int i=0; i<list.getLength(); i++)
			{
				Node item=list.item(i);
				if (item.getNodeType()==Node.ELEMENT_NODE)
				{
					if (item.getNodeName().indexOf("resourcetype")>=0)
					{
						okprops+="<D:resourcetype>\n";
						if (fs.isPathDirectory(path))
							okprops+="<D:collection/>\n";
						else
							okprops+="application/unknown\n";
						okprops+="</D:resourcetype>\n";
					}
					else if (item.getNodeName().indexOf("getlastmodified")>=0)
					{
						okprops+="<D:getlastmodified>Thu, 28 Dec 2006 00:00:00 GMT</D:getlastmodified>\n";
					}
					else
						badprops+="<"+item.getNodeName()+"/>\n";						
				}
			}
				
			if (okprops.length()>0)
			{
				okprops="<D:propstat>\n"+
				"<D:prop>\n"+
				okprops +
				"</D:prop>\n"+
				"<D:status>HTTP/1.1 200 OK</D:status>\n"+
				"</D:propstat>\n";
			}
			
			if (badprops.length()>0)
			{
				badprops="<D:propstat>\n"+
				"<D:prop>\n"+
				badprops +
				"</D:prop>\n"+
				"<D:status>HTTP/1.1 404 Not Found</D:status>\n"+
				"</D:propstat>\n";				
			}
				
			String encoded=new URI(null, null, path, null).getRawPath();
			xml+="<D:response>\n"+			
	        "<D:href>"+encoded+"</D:href>\n"+
			okprops +
			badprops +
			"</D:response>\n";			
			if (depth==1)
			{
				
				String[] templist=fs.listPath();
				String[] pathlist=new String[templist.length+1];
				pathlist[0]="D";
				System.arraycopy(templist, 0, pathlist, 1, templist.length);
                for (int j=0; j<pathlist.length; j++)
                {
					okprops="";
					badprops="";
					
					for (int i=0; i<list.getLength(); i++)
					{
						Node item=list.item(i);
						if (item.getNodeType()==Node.ELEMENT_NODE)
						{
							if (item.getNodeName().indexOf("resourcetype")>=0)
							{
								if (pathlist[j].substring(0, 1).equals("D"))
									okprops+="<D:resourcetype>\n"+
									"<D:collection/>\n"+
									"</D:resourcetype>\n";
								else
									okprops+="<D:resourcetype>application/unknown</D:resourcetype>\n";
							}
							else if (item.getNodeName().indexOf("getcontentlength")>=0
									&& !pathlist[j].substring(0,1).equals("D"))
							{
								FileList file=fs.getFileInfo(path+pathlist[j].substring(1));
								okprops+="<D:getcontentlength>"+file.getSize()+"</D:getcontentlength>\n";
							}
							else if (item.getNodeName().indexOf("getlastmodified")>=0)
							{
								okprops+="<D:getlastmodified>Thu, 28 Dec 2006 00:00:00 GMT</D:getlastmodified>\n";
							}
							else
								badprops+="<"+item.getNodeName()+"/>\n";						
						}
					}
					
					if (okprops.length()>0)
					{
						okprops="<D:propstat>\n"+
						"<D:prop>\n"+
						okprops +
						"</D:prop>\n"+
						"<D:status>HTTP/1.1 200 OK</D:status>\n"+
						"</D:propstat>\n";
					}
					
					if (badprops.length()>0)
					{
						badprops="<D:propstat>\n"+
						"<D:prop>\n"+
						badprops +
						"</D:prop>\n"+
						"<D:status>HTTP/1.1 404 Not Found</D:status>\n"+
						"</D:propstat>\n";				
					}
                
					String name=pathlist[j].substring(1);
					encoded=new URI(null, null, path+name, null).getRawPath();
					xml+="<D:response>\n"+			
		            "<D:href>"+encoded+"</D:href>\n"+
					okprops +
					badprops +
					"</D:response>\n";
					
                }
                
			}
			xml+="</D:multistatus>\n";
		}
		catch (Exception e)
		{
			return null;
		}
		
		return xml;
	}
	
	public static String getPropertyNames(NodeList list, String path, int depth)
	{
		String xml="<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n";
		return xml;
	}
}
