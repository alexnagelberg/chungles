import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

public class Configuration extends DefaultHandler
{
	private static Hashtable shares;
	
	public Configuration()
	{
		shares=new Hashtable();
	}
	
	public static void parse()
	{
		DefaultHandler handler=new Configuration();
		SAXParserFactory factory=SAXParserFactory.newInstance();
		try
		{
			SAXParser saxParser = factory.newSAXParser();
			File file=new File(System.getProperty("user.home")+"/.chungles/config.xml");
			if (!file.exists())
				createConfig(file);
			else
				saxParser.parse(file, handler);			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}	
	}
	
	public void startElement(String namespaceURI, String localName,
			String qualifiedName, Attributes attrs) throws SAXException
	{
		// A share
		if (qualifiedName.equals("share"))
		{
			String name=attrs.getValue("name");
			String map=attrs.getValue("map");
			shares.put(name, map);
		}
	}
	
	public static String getSharePath(String name)
	{
		return (String)shares.get(name);
	}
	
	public static Iterator getSharesIterator()
	{
		return shares.keySet().iterator();
	}
	
	private static void createConfig(File file)
	{
		try
		{
			new File(System.getProperty("user.home")+"/.chungles/").mkdir();
			file.createNewFile();
			// Get the user to add shares
			SWTPreferencesDialog.getInstance(
					SWTUtil.getInstance().getShell().getDisplay()
					).openDialog();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void saveConfig()
	{
		String file=System.getProperty("user.home")+"/.chungles/config.xml";
		try
		{
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
			
			// Opening
			out.println("<?xml version='1.0'?>");
			out.println("<chungles>");
			
			// Shares			
			out.println("<shares>");
			Iterator iterator=getSharesIterator();
			while (iterator.hasNext())
			{
				String share=(String)iterator.next();
				String map=getSharePath(share);
				out.println("<share name=\"" + share + "\" map=\"" + map + "\"/>");
			}
			out.println("</shares>");
			
			// Closing
			out.println("</chungles>");
			out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}		
	}
	
	public static void addShare(String name, String path)
	{
		shares.put(name, path);
	}
	
	public static void clearShares()
	{
		shares.clear();
	}
}
