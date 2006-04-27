package org.chungles.ui.swt;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.*;
import java.io.*;

import org.chungles.core.*;

public class SWTAboutDialog
{
	private static SWTAboutDialog instance;
	private Display display;
	private Shell shell;
	
	private SWTAboutDialog(Display display)
	{
		this.display=display;
	}
	
	public static SWTAboutDialog getInstance(Display display)
	{
		if (instance==null)
			instance=new SWTAboutDialog(display);
		
		return instance;
	}
	
	public void openDialog()
	{
		if (!(shell==null || shell.isDisposed()))
			return;
		
		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		
		shell.setLayout(null);
		shell.setText("About");
		InputStream in=ClassLoader.getSystemResourceAsStream("images/chungles.png");	
		Image chunglesGif=new Image(display, in);
		shell.setImage(chunglesGif);
		shell.layout();
		shell.pack();
		shell.setSize(520,300);
		
		Label picture = new Label(shell, SWT.NONE);
		picture.setImage(chunglesGif);
		picture.setBounds(20,30,200,200);
		
		Label label = new Label(shell, SWT.NONE);
		Font initialFont = label.getFont();
		FontData[] fontData = initialFont.getFontData();
		for (int i = 0; i < fontData.length; i++)
		{
			fontData[i].setHeight(32);
		}
		Font newFont = new Font(display, fontData);
		label.setFont(newFont);
		label.setText("Chungles " + Configuration.getVersion());
		label.setBounds(245, 115, 300, 50);
		
		label=new Label(shell, SWT.CENTER);
		label.setText("Tastes like chicken.");
		label.setBounds(245, 165, 210, 50);
		
		Button okbutton=new Button(shell, SWT.NONE);
		okbutton.setText("&Ok");
		okbutton.setBounds(380, 230, 120, 30);
		okbutton.addSelectionListener(new SelectionListener()
				{
					public void widgetDefaultSelected(SelectionEvent e) {}
					
					public void widgetSelected(SelectionEvent e)
					{
						shell.dispose();
					}
				});
		
		shell.open();
	}
}
