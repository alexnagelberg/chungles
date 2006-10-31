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
	private MenuItem getFile, putFile;
    private ToolBar toolBar;
    private ToolItem downloaditem, uploaditem, deleteitem, newdiritem, preferencesitem, quititem;
    private TreeEditor treeeditor;
    
	private SWTUtil()
	{
		Display display = new Display();
	    shell = new Shell(display);	    
	    shell.setText("Chungles");	    
	    shell.setLayout(null);
	    InputStream in=ClassLoader.getSystemResourceAsStream("images/chungles.png");	  	    	   
	    shell.setImage(new Image(display, in));	    
	    Rectangle area=shell.getClientArea();		
	    
	    // Add Toolbar
	    toolBar = new ToolBar (shell, SWT.HORIZONTAL);
	    toolBar.setBounds(0, 0, area.width, 33);
        
	    // Download Button
	    downloaditem=new ToolItem(toolBar, SWT.PUSH);
	    in=ClassLoader.getSystemResourceAsStream("images/download.png");	    
	    downloaditem.setImage(new Image(display, in));
        downloaditem.setToolTipText("Download file");
        downloaditem.addSelectionListener(new TransferFromNode());
        downloaditem.setEnabled(false);
	    
	    // Upload Button
	    uploaditem=new ToolItem(toolBar, SWT.PUSH);
	    in=ClassLoader.getSystemResourceAsStream("images/upload.png");
	    uploaditem.setImage(new Image(display, in));
        uploaditem.setToolTipText("Upload file");
        downloaditem.addSelectionListener(new TransferToNode());
        uploaditem.setEnabled(false);
        
	    // Delete Button
	    deleteitem=new ToolItem(toolBar, SWT.PUSH);
	    in=ClassLoader.getSystemResourceAsStream("images/delete.png");
	    deleteitem.setImage(new Image(display, in));
        deleteitem.setToolTipText("Delete file");
        deleteitem.addSelectionListener(new SWTDeleteSelection());
        deleteitem.setEnabled(false);
        
	    // New Directory Button
	    newdiritem=new ToolItem(toolBar, SWT.PUSH);
	    in=ClassLoader.getSystemResourceAsStream("images/newdirectory.png");
	    newdiritem.setImage(new Image(display, in));
        newdiritem.setToolTipText("Create new directory");
        newdiritem.addSelectionListener(new SWTNewDirectory());        
	    newdiritem.setEnabled(false);
        
	    // Spacer
	    ToolItem toolitem=new ToolItem(toolBar, SWT.SEPARATOR);
	    toolitem.setWidth(20);
	    
	    // Preferences Button
	    preferencesitem=new ToolItem(toolBar, SWT.PUSH);	    
	    in=ClassLoader.getSystemResourceAsStream("images/preferences.png");
	    preferencesitem.setImage(new Image(display, in));
        preferencesitem.addSelectionListener(this);
        preferencesitem.setToolTipText("Preferences");
	    
	    // Quit Button    	    	
	    quititem=new ToolItem(toolBar, SWT.PUSH);
	    in=ClassLoader.getSystemResourceAsStream("images/quit.png");
	    quititem.setImage(new Image(display, in));
        quititem.addSelectionListener(this);
        quititem.setToolTipText("Quit");
	    
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
                        getFile.setEnabled(false);
                        putFile.setEnabled(false);
                        downloaditem.setEnabled(false);
                        uploaditem.setEnabled(false);
                        deleteitem.setEnabled(false);
                        newdiritem.setEnabled(false);
                        
                        TreeItem items[]=tree.getSelection();
                        int i;
                        for (i=0; i<items.length; i++)
                        {
                            if (items[i].getParentItem()!=null)
                            {
                                getFile.setEnabled(true);
                                downloaditem.setEnabled(true);
                                deleteitem.setEnabled(true);
                                if (items[i].getItemCount()>0 && items.length==1)
                                {
                                    putFile.setEnabled(true);
                                    uploaditem.setEnabled(true);
                                    newdiritem.setEnabled(true);
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
        getFile=new MenuItem(popup, SWT.PUSH);
        getFile.setText("Get File(s)");
        getFile.setEnabled(false);
        getFile.addSelectionListener(new TransferFromNode());
        putFile=new MenuItem(popup, SWT.PUSH);
        putFile.setText("Send file(s) to...");
        putFile.setEnabled(false);        
	    putFile.addSelectionListener(new TransferToNode());
        
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
        if (e.getSource()==quititem)
        {
            shell.dispose();
        }
        else if (e.getSource()==preferencesitem)
        {
            openPreferencesDialog();
        }        
    }
}
