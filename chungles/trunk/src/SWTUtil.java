import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

// Singleton
public class SWTUtil
{
	private Tree tree;
	private Shell shell;
	private boolean isActive;	
	private static SWTUtil instance;
	
	private SWTUtil()
	{
		Display display = new Display();
	    shell = new Shell(display);	    
	    shell.setText("Chungles");	    	    
	    shell.setLayout(new FillLayout());
	    InputStream in=ClassLoader.getSystemClassLoader().getResourceAsStream("images/chungles.gif");	  	    	   
	    shell.setImage(new Image(display, in));
	    
	    // Add tree
	    tree=new Tree(shell, SWT.SINGLE);	    
	    tree.addListener(SWT.Expand, new ShareLister());
	    
	    // Tree's drag n'dropables
	    DropTarget dt = new DropTarget(tree, DND.DROP_MOVE);
	    dt.setTransfer(new Transfer[] {FileTransfer.getInstance()});
	    dt.addDropListener(new TransferToNode());
	    
	    DragSource ds = new DragSource(tree, DND.DROP_MOVE);
	    ds.setTransfer(new Transfer[] {FileTransfer.getInstance()});
	    ds.addDragListener(new TransferFromNode());
	    
	    // Create menu
	    Menu bar=new Menu(shell, SWT.BAR);
	    shell.setMenuBar(bar);
	    MenuItem item=new MenuItem(bar, SWT.CASCADE);
	    item.setText("&File");
	    Menu fileMenu=new Menu(shell, SWT.DROP_DOWN);
	    item.setMenu(fileMenu);
	    item=new MenuItem(fileMenu, SWT.PUSH);
	    item.setText("&Add undetected node");	    
	    item.addSelectionListener(new SelectionListener()
	            {
	        		public void widgetDefaultSelected(SelectionEvent e) {}
	        		
	        		public void widgetSelected(SelectionEvent e)
	        		{
	        		    openAddNodeDialog();
	        		}
	            });
	    
	    item=new MenuItem(fileMenu, SWT.PUSH);	    
	    item.setText("&Preferences");
	    item.addSelectionListener(new SelectionListener()
	    		{
	    			public void widgetDefaultSelected(SelectionEvent e) {}
	    			
	    			public void widgetSelected(SelectionEvent e)
	    			{
	    				openPreferencesDialog();
	    			}
	    		});
	    
	    item=new MenuItem(fileMenu, SWT.SEPARATOR);
	    item=new MenuItem(fileMenu, SWT.PUSH);
	    item.setText("E&xit");
	    item.addSelectionListener(new SelectionListener()
	    		{
	    			public void widgetDefaultSelected(SelectionEvent e)
	    			{
	    				
	    			}
	    			
	    			public void widgetSelected(SelectionEvent e)
	    			{
	    				// By closing the main window, we initiate
	    				// the nice and clean shutdown sequence
	    				MenuItem item=(MenuItem)e.getSource();
	    				item.getParent().getShell().dispose();
	    			}
	    		});
	    
	    shell.pack();	    
	    shell.setSize(640,480);
	    shell.open();
	    
	    isActive=true;
	}
	
	public static SWTUtil getInstance()
	{
		if (instance==null && !DaemonUtil.getInstance().getConfig().getBoolean("daemon"))
		{
			instance=new SWTUtil();
		}
		
		return instance;
	}
	
	public Tree getTree()
	{
		return tree;
	}
	
	public Shell getShell()
	{
		return shell;
	}
	
	public void mainLoop()
	{
		Display display=shell.getDisplay();
		while (!shell.isDisposed())
	    {
			if (!display.readAndDispatch())
	    			display.sleep();
	    }
	    display.dispose();
	    
	    isActive=false;
	}
	
	public boolean isActive()
	{
		return isActive;
	}
	
	public void openPreferencesDialog()
	{
		SWTPreferencesDialog.getInstance(shell.getDisplay()).openDialog();
	}
	
	public void openAddNodeDialog()
	{
	    SWTAddNodeDialog.getInstance(shell.getDisplay()).openDialog();
	}
}
