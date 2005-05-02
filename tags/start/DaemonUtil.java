
import com.martiansoftware.jsap.*;
import java.io.*;

/*
Handles console/daemon mode and program arguments
This might be seperated into two seperate classes if it gets too big!
*/
public class DaemonUtil extends Thread
{
    private static DaemonUtil instance;
    private JSAP jsap;
    private JSAPResult config;
    private boolean isActive;

    public DaemonUtil()
    {
		super("DaemonThread");
        jsap = new JSAP();
        config = null;
        isActive = false;
    }

    public void parseArgs(String[] args)
    {
        try {
        Switch sw1 = new Switch("daemon")
                         .setShortFlag('d')
                         .setLongFlag("daemon");
        jsap.registerParameter(sw1);
        config = jsap.parse(args);

        Configuration.parse();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public JSAPResult getConfig()
    {
        return config;
    }

    public void run()
    {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        isActive = true;
        System.out.println("Console started -- type 'die' to quit");
        while (isActive)
        {
            try {
            if ((line = console.readLine()) != null)
                process(line);
            } catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void process(String line)
    {
        if (line.equalsIgnoreCase("die"))
        {
            isActive = false;
            System.out.println("Quitting");
        }
        return;
    }

    public static DaemonUtil getInstance()
    {
        if (instance == null)
            instance = new DaemonUtil();
        return instance;
    }

    public boolean isActive()
    {
        return isActive;
    }
}
