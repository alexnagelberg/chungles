package org.chungles.ui.swt;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

public class SWTTransferDialog
{
	private static SWTTransferDialog instance;
	private Display display;
	private Shell shell;
	private Label currentFileLabel, fileRatioLabel;
	private ProgressBar fileProgressBar, filesProgressBar;
	private static int fileprogress, filesprogress;
	
	private SWTTransferDialog(Display display)
	{
		this.display=display;
	}
	
	public static SWTTransferDialog getInstance(Display display)
	{
		if (instance==null)
			instance=new SWTTransferDialog(display);
		
		return instance;
	}
	
	public void openDialog(final AbortListener listener)
	{
		if (!(shell==null || shell.isDisposed()))
			return;
		
		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

		shell.setLayout(null);
		shell.setText("File Transfer");
		InputStream in=getClass().getClassLoader().getResourceAsStream("images/chungles.png");	
		shell.setImage(new Image(display, in));
		shell.layout();
		shell.pack();
		shell.setSize(640, 265);
		
		currentFileLabel = new Label(shell, SWT.NONE);
		currentFileLabel.setBounds(0, 20, 630, 20);
		
		fileProgressBar = new ProgressBar(shell, SWT.NONE);
		fileProgressBar.setMinimum(0);
		fileProgressBar.setMinimum(100);		
		fileProgressBar.setBounds(0, 50, 630, 20);
		
		fileRatioLabel = new Label(shell, SWT.NONE);
		fileRatioLabel.setBounds(0, 110, 630, 20);
		
		filesProgressBar = new ProgressBar(shell, SWT.NONE);
		filesProgressBar.setMinimum(0);
		filesProgressBar.setMinimum(100);
		filesProgressBar.setBounds(0, 140, 630, 20);
		
		Button abortButton = new Button(shell, SWT.PUSH | SWT.CENTER);
		abortButton.setText("&Abort");
		abortButton.setBounds(525, 200, 100, 30);
		abortButton.addSelectionListener(new SelectionListener()
        {
            public void widgetSelected(SelectionEvent e)
            {
                if (listener.shouldCloseDialog())
                    closeDialog();
                listener.abort();
            }
            
            public void widgetDefaultSelected(SelectionEvent e) {}
        });
        
		shell.open();
	}
	
	public void closeDialog()
	{
		display.asyncExec(new Runnable()
		{
			public void run()
			{
				shell.dispose();
			}
		});		
	}
	
	public void updateProgress(long transferred, long fileSize, long totalTransferred, long totalSize)
	{
		fileprogress=(int)(((double)transferred/(double)fileSize)*100.);
		filesprogress=(int)(((double)totalTransferred/(double)totalSize)*100.);		
	}
	
	public void updateLables(final String filename, final int file, final int files)
	{
		display.syncExec(new Runnable()
				{
					public void run()
					{
						currentFileLabel.setText(filename);
						fileRatioLabel.setText("File " + file + " of " + files);
					}
				});
	}
	
	public void progressThread()
	{
	    new Thread()
	    {
	        public void run()
	        {	            
	            while (filesprogress<100)
	            {                            	                
	                display.syncExec(new Runnable()
	                {
	                    public void run()
	                    {
	                    	if (fileProgressBar.isDisposed() || filesProgressBar.isDisposed())
	                    		filesprogress=100;
	                    	else
	                    	{
	                    		fileProgressBar.setSelection(fileprogress);
	                    		filesProgressBar.setSelection(filesprogress);
	                    	}
                        }
                    });
                }
	            filesprogress=0;
	        }
	    }.start();
	}
}
