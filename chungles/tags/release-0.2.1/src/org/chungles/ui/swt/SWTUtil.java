package org.chungles.ui.swt;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

// Singleton
public class SWTUtil implements SelectionListener
{
	private Tree tree;
	private Shell shell;
	private boolean isActive;	
	private static SWTUtil instance;
	private MenuItem getFileMenuItem, putFileMenuItem, newdirMenuItem, deleteMenuItem;
    private ToolBar toolBar;
    private ToolItem downloadToolItem, uploadToolItem, deleteToolItem, newdirToolItem, preferencesToolItem, quitToolItem;
    private TreeEditor treeeditor;
    private TrayItem trayitem;
    private boolean visible=true;
    
	private SWTUtil()
	{
		Display display = new Display();
	    shell = new Shell(display);	    
	    shell.setText("Chungles");	    
	    shell.setLayout(null);
	    InputStream in=ClassLoader.getSystemResourceAsStream("images/chungles.png");	  	    	   
	    shell.setImage(new Image(display, in));	    
	    Rectangle area=shell.getClientArea();		
	    
	    // Set system tray icon
	    in=ClassLoader.getSystemResourceAsStream("images/chungles-16.png");
	    Tray tray=display.getSystemTray();
	    trayitem=new TrayItem(tray, SWT.NONE);
	    trayitem.setImage(new Image(display, in));
	    trayitem.addSelectionListener(this);
	    trayitem.setToolTipText("Chungles");
	    
	    // Add Toolbar
	    toolBar = new ToolBar (shell, SWT.HORIZONTAL);
	    toolBar.setBounds(0, 0, area.width, 33);
        
	    // Download Button
	    downloadToolItem=new ToolItem(toolBar, SWT.PUSH);
	    in=ClassLoader.getSystemResourceAsStream("images/download.png");	    
	    downloadToolItem.setImage(new Image(display, in));
        downloadToolItem.setToolTipText("Download file");
        downloadToolItem.addSelectionListener(new TransferFromNode());
        downloadToolItem.setEnabled(false);
	    
	    // Upload Button
	    uploadToolItem=new ToolItem(toolBar, SWT.PUSH);
	    in=ClassLoader.getSystemResourceAsStream("images/upload.png");
	    uploadToolItem.setImage(new Image(display, in));
        uploadToolItem.setToolTipText("Upload file");
        uploadToolItem.addSelectionListener(new TransferToNode());
        uploadToolItem.setEnabled(false);
        
	    // Delete Button
	    deleteToolItem=new ToolItem(toolBar, SWT.PUSH);
	    in=ClassLoader.getSystemResourceAsStream("images/delete.png");
	    deleteToolItem.setImage(new Image(display, in));
        deleteToolItem.setToolTipText("Delete file");
        deleteToolItem.addSelectionListener(new SWTDeleteSelection());
        deleteToolItem.setEnabled(false);
        
	    // New Directory Button
	    newdirToolItem=new ToolItem(toolBar, SWT.PUSH);
	    in=ClassLoader.getSystemResourceAsStream("images/newdirectory.png");
	    newdirToolItem.setImage(new Image(display, in));
        newdirToolItem.setToolTipText("Create new directory");
        newdirToolItem.addSelectionListener(new SWTNewDirectory());        
	    newdirToolItem.setEnabled(false);
        
	    // Spacer
	    ToolItem toolitem=new ToolItem(toolBar, SWT.SEPARATOR);
	    toolitem.setWidth(20);
	    
	    // Preferences Button
	    preferencesToolItem=new ToolItem(toolBar, SWT.PUSH);	    
	    in=ClassLoader.getSystemResourceAsStream("images/preferences.png");
	    preferencesToolItem.setImage(new Image(display, in));
        preferencesToolItem.addSelectionListener(this);
        preferencesToolItem.setToolTipText("Preferences");
	    
	    // Quit Button    	    	
	    quitToolItem=new ToolItem(toolBar, SWT.PUSH);
	    in=ClassLoader.getSystemResourceAsStream("images/quit.png");
	    quitToolItem.setImage(new Image(display, in));
        quitToolItem.addSelectionListener(this);
        quitToolItem.setToolTipText("Quit");
	    
	    // Add tree
	    tree=new Tree(shell, SWT.MULTI | SWT.BORDER);
	    treeeditor=new TreeEditor(tree);
        treeeditor.horizontalAlignment = SWT.LEFT;
        treeeditor.grabHorizontal = true;

        
        tree.setFocus();
	    tree.setBounds(0, toolBar.getSize().y, area.width, area.height-toolBar.getSize().y);
	    tree.addListener(SWT.Expand, new ShareLister());
        tree.addListener(SWT.Selection, new Listener()
                {
                    public void handleEvent(Event e) // Adds pop-up menu items as needed
                    {                        
                        getFileMenuItem.setEnabled(false);
                        putFileMenuItem.setEnabled(false);
                        downloadToolItem.setEnabled(false);
                        uploadToolItem.setEnabled(false);
                        deleteToolItem.setEnabled(false);
                        newdirToolItem.setEnabled(false);
                        deleteMenuItem.setEnabled(false);
                    	newdirMenuItem.setEnabled(false);
                    	
                        TreeItem items[]=tree.getSelection();
                        int i;
                        for (i=0; i<items.length; i++)
                        {
                            if (items[i].getParentItem()!=null)
                            {
                                getFileMenuItem.setEnabled(true);
                                downloadToolItem.setEnabled(true);
                                deleteToolItem.setEnabled(true);
                                deleteMenuItem.setEnabled(true);
                                if (items[i].getItemCount()>0 && items.length==1)
                                {
                                    putFileMenuItem.setEnabled(true);
                                    uploadToolItem.setEnabled(true);
                                    newdirToolItem.setEnabled(true);
                                    newdirMenuItem.setEnabled(true);
                                }
                                break;
                            }
                        }
                    }
                });
	    
	    // Tree's drag n'dropables
	    DropTarget dt = new DropTarget(tree, DND.DROP_MOVE);
	    dt.setTransfer(new Transfer[] {FileTransfer.getInstance()});
	    dt.addDropListener(new TransferToNode());
	    
        // Pop-up menu
        Menu popup=new Menu(shell, SWT.POP_UP);
        tree.setMenu(popup);
        getFileMenuItem=new MenuItem(popup, SWT.PUSH);
        getFileMenuItem.setText("Get File(s)");
        getFileMenuItem.setEnabled(false);
        getFileMenuItem.addSelectionListener(new TransferFromNode());
        putFileMenuItem=new MenuItem(popup, SWT.PUSH);
        putFileMenuItem.setText("Send file(s) to...");
        putFileMenuItem.setEnabled(false);        
	    putFileMenuItem.addSelectionListener(new TransferToNode());
	    newdirMenuItem=new MenuItem(popup, SWT.PUSH);
	    newdirMenuItem.setText("Create new directory in...");
	    newdirMenuItem.setEnabled(false);
	    newdirMenuItem.addSelectionListener(new SWTNewDirectory());
	    deleteMenuItem=new MenuItem(popup, SWT.PUSH);
	    deleteMenuItem.setText("Delete file(s)");
	    deleteMenuItem.setEnabled(false);
	    deleteMenuItem.addSelectionListener(new SWTDeleteSelection());
        
	    // Create menu
	    Menu bar=new Menu(shell, SWT.BAR | SWT.LEFT_TO_RIGHT);
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
	    			public void widgetDefaultSelected(SelectionEvent e) {}
	    			
	    			public void widgetSelected(SelectionEvent e)
	    			{
	    				// By closing the main window, we initiate
	    				// the nice and clean shutdown sequence
	    				MenuItem item=(MenuItem)e.getSource();
	    				item.getParent().getShell().dispose();
	    			}
	    		});
	    
	    // Help menu
	    item=new MenuItem(bar, SWT.CASCADE);
	    item.setText("&Help");
	    Menu helpMenu=new Menu(shell, SWT.DROP_DOWN);
	    item.setMenu(helpMenu);
	    item=new MenuItem(helpMenu, SWT.PUSH);
	    item.setText("&About");
	    item.addSelectionListener(new SelectionListener()
	        {
	        		public void widgetDefaultSelected(SelectionEvent e) {}
	        		
	        		public void widgetSelected(SelectionEvent e)
	        		{
	        		    openAboutDialog();
	        		}
	        });
	    
	       	    	
	    shell.addListener(SWT.Resize, new Listener()
		{
			public void handleEvent(Event event)
			{
				Rectangle area=shell.getClientArea();
				toolBar.setBounds(0, 0, area.width, toolBar.getSize().y);
				tree.setBounds(0, toolBar.getSize().y, area.width, area.height-toolBar.getSize().y);
			}
		});
	    
	    shell.pack();	    
	    shell.setSize(640,480);
	    shell.open();
	    
	    isActive=true;
	}
	
	public static SWTUtil getInstance()
	{
		if (instance==null)
		{
			instance=new SWTUtil();
		}
		
		return instance;
	}
	
	public Tree getTree()
	{
		return tree;
	}
	
    public TreeEditor getTreeEditor()
    {
        return treeeditor;
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
	
	public void openAboutDialog()
	{
		SWTAboutDialog.getInstance(shell.getDisplay()).openDialog();
	}
    
	public void widgetDefaultSelected(SelectionEvent e) {}
    
    public void widgetSelected(SelectionEvent e)
    {
        if (e.getSource()==quitToolItem)
        {
            shell.dispose();
        }
        else if (e.getSource()==preferencesToolItem)
        {
            openPreferencesDialog();
        }
        else if (e.getSource()==trayitem)
        {
        	visible=!visible;
        	shell.setVisible(visible);
        }
    }
    
    public void deselectAllInTree()
    {
    	tree.deselectAll();
    	downloadToolItem.setEnabled(false);
    	uploadToolItem.setEnabled(false);
    	deleteToolItem.setEnabled(false);
    	newdirToolItem.setEnabled(false);
    	getFileMenuItem.setEnabled(false);
    	putFileMenuItem.setEnabled(false);
    	deleteMenuItem.setEnabled(false);
    	newdirMenuItem.setEnabled(false);
    }
}
