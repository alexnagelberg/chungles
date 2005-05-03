import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

public class SWTTransferDialog
{
	private static SWTTransferDialog instance;
	private Display display;
	private Shell shell;
	private Label currentFileLabel, fileRatioLabel;
	private ProgressBar fileProgressBar, filesProgressBar;
	
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
	
	public void openDialog()
	{
		if (!(shell==null || shell.isDisposed()))
			return;
		
		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

		shell.setLayout(null);
		shell.setText("File Send");
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
	
	public void updateProgress(long sent, long fileSize, long totalSent, long totalSize)
	{
		final int fileProgress=(int)(((double)sent/(double)fileSize)*100.);
		final int filesProgress=(int)(((double)totalSent/(double)totalSize)*100.);
		System.out.println(totalSent + "/" + totalSize);
		display.asyncExec(new Runnable()
		{
			public void run()
			{		
				fileProgressBar.setSelection(fileProgress);
				filesProgressBar.setSelection(filesProgress);
			}
		});
	}
	
	public void updateLables(final String filename, final int file, final int files)
	{
		display.asyncExec(new Runnable()
				{
					public void run()
					{
						currentFileLabel.setText(filename);
						fileRatioLabel.setText("File " + file + " of " + files);
					}
				});
	}
}
