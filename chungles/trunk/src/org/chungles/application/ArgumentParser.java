package org.chungles.application;

import java.util.*;

public class ArgumentParser
{
	private String ui="swt";
	
	private ArgumentParser()
	{
	}
	
	public ArgumentParser(String[] args)
	{
		int i;
		for (i=0; i<args.length; i++)
		{
			if (args[i].length()<3)
			{
				System.out.println("Invalid argument [" + args[i] + "]");
			}
			else
			{
				StringTokenizer strtok=new StringTokenizer(args[i].substring(2), "=");				
				String argswitch=strtok.nextToken();
				String argvalue="";
				if (strtok.hasMoreTokens()) 
					argvalue=strtok.nextToken();
				
				// Begin cases
				if (argswitch.equals("ui"))
				{
					// There are three options: console, swt, and none (daemon)
					if (!argvalue.equals("console") && !argvalue.equals("swt")
							&& !argvalue.equals("none"))
						System.out.println("Invalid UI type");
					else
						ui=argvalue;
				}
				else if (argswitch.equals("help"))
				{
					System.out.println("Options are:");
					System.out.println("--ui=<ui type>      Sets UI where 'ui type' is either swt, console, or none(daemon mode).");
					System.out.println("--help              Shows this screen.");
				}
						
			}
		}
	}
	
	public String getUI()
	{
		return ui;
	}
}
