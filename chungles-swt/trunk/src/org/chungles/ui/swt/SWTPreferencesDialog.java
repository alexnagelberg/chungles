package org.chungles.ui.swt;

import java.io.InputStream;
import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import org.chungles.core.*;
import org.chungles.plugin.*;

// Singleton
public class SWTPreferencesDialog
{
    private Display display;
    private Shell shell;
    private TableEditor editor;
    private Table sharetable, pluginstable;
    private Text compname, mcastshare, mcastspeed;
    private Button mcastflow;    
    private LinkedList<PluginInfo> pluginsToRemove, pluginsToAdd, pluginsToDisable, pluginsToEnable;    
    private static SWTPreferencesDialog dialog;

    public static SWTPreferencesDialog getInstance(Display display)
    {
        if (dialog == null)
        {
            dialog = new SWTPreferencesDialog(display);            
        }

        return dialog;
    }

    private SWTPreferencesDialog(Display display)
    {
        this.display = display;
        pluginsToRemove=new LinkedList<PluginInfo>();
        pluginsToAdd=new LinkedList<PluginInfo>();
        pluginsToDisable=new LinkedList<PluginInfo>();
        pluginsToEnable=new LinkedList<PluginInfo>();
    }

    public void openDialog()
    {
        if (!(shell == null || shell.isDisposed()))
            return;

        ButtonListener listener = new ButtonListener();

        shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setText("Chungles Preferences");
        InputStream in=getClass().getClassLoader().getResourceAsStream("images/chungles.png");	
        shell.setImage(new Image(display, in));
        shell.setLayout(null);
        shell.setBounds(50, 50, 640, 480);
        CTabFolder folder = new CTabFolder(shell, SWT.FLAT | SWT.TOP);        

        //PRETTIES!
        folder.setSelectionBackground(new Color[]
        { display.getSystemColor(SWT.COLOR_LIST_BACKGROUND),
                display.getSystemColor(SWT.COLOR_LIST_BACKGROUND),
                folder.getBackground() }, new int[]
        { 10, 90 }, true);
        folder.setSimple(false);
        folder.setBounds(0, 0, 630, 400);
        folder.setBorderVisible(true);

        // Shares Tab
        CTabItem tabItem = new CTabItem(folder, SWT.NONE);
        tabItem.setText("Shares");

        Composite composite = new Composite(folder, SWT.NONE);
        tabItem.setControl(composite);
        composite.setLayout(null);

        sharetable = new Table(composite, SWT.BORDER | SWT.SINGLE);
        editor = new TableEditor(sharetable);
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;
        editor.minimumWidth = 50;
        sharetable.setHeaderVisible(true);
        sharetable.setLinesVisible(false);
        TableColumn column = new TableColumn(sharetable, SWT.LEFT);
        column.setText("Name");
        column.setWidth(100);
        column = new TableColumn(sharetable, SWT.LEFT);
        column.setText("Path");
        column.setWidth(380);
        sharetable.setBounds(5, 5, 485, 340);
        sharetable.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {

                //	Clean up any previous editor control
                Control oldEditor = editor.getEditor();
                if (oldEditor != null)
                    oldEditor.dispose();

                final TableItem item = (TableItem) e.item;
                if (item == null)
                    return;

                Text newEditor = new Text(sharetable, SWT.BORDER);
                newEditor.setText(item.getText(0));
                newEditor.addKeyListener(new KeyListener()
                {
                    public void keyPressed(KeyEvent e)
                    {
                    }

                    public void keyReleased(KeyEvent e)
                    {
                        Text text = (Text) e.getSource();
                        if (e.keyCode == '\r') // Enter
                        {

                            item.setText(0, text.getText());
                            text.dispose();
                        }
                        else if (e.keyCode == 27) // Escape
                        {
                            text.dispose();
                        }
                    }
                });
                newEditor.selectAll();
                newEditor.setFocus();
                editor.setEditor(newEditor, item, 0);
            }
        });
        populateList();
        
        Button addbutton = new Button(composite, SWT.PUSH | SWT.CENTER);
        addbutton.setText("&Add Share");
        addbutton.addSelectionListener(listener);
        addbutton.setBounds(500, 5, 120, 30);

        Button removeshare = new Button(composite, SWT.PUSH | SWT.CENTER);
        removeshare.setText("&Remove Share");
        removeshare.addSelectionListener(listener);
        removeshare.setBounds(500, 50, 120, 30);

        Label label=new Label(composite, SWT.NONE);
        label.setText("Computer name: ");
        label.setBounds(5, 355, 110, 20);
        
        compname = new Text(composite, SWT.BORDER);
        compname.setText(Configuration.getComputerName());
        compname.setBounds(115, 350, 375, 25);                

        // Multicast tab
        tabItem = new CTabItem(folder, SWT.NONE);
        tabItem.setText("Multicast");
        
        composite = new Composite(folder, SWT.NONE);
        tabItem.setControl(composite);
        composite.setLayout(null);
        
        label=new Label(composite, SWT.NONE);
        label.setText("Multicast Share: ");
        label.setBounds(5, 10, 110, 20);
        
        mcastshare=new Text(composite, SWT.BORDER);
        mcastshare.setText(Configuration.getMCastShare());
        mcastshare.setBounds(115, 5, 375, 25);
        
        Button browse=new Button(composite, SWT.PUSH | SWT.CENTER);        
        browse.setText("&Browse");
        browse.setBounds(495, 5, 100, 25);
        browse.addSelectionListener(listener);
        
        mcastflow=new Button(composite, SWT.CHECK);
        mcastflow.setText("Enable multicast flow control (limiting speed can greatly reduce packet loss)");
        mcastflow.setBounds(5, 35, 600, 25);
        mcastflow.addSelectionListener(listener);
        
        label=new Label(composite, SWT.NONE);
        label.setText("Speed: ");
        label.setBounds(15, 70, 50, 25);
        
        mcastspeed=new Text(composite, SWT.BORDER);
        mcastspeed.setText(""+Configuration.getMCastKBPSSpeed());
        mcastspeed.setBounds(65, 65, 60, 25);
        
        label=new Label(composite, SWT.NONE);
        label.setText("KB/s");
        label.setBounds(130, 70, 50, 25);
        
        if (Configuration.isMCastThrottled())
        {
            mcastflow.setSelection(true);
        }
        else
            mcastspeed.setEnabled(false);
        
        // Plugins Tab
        
        tabItem = new CTabItem(folder, SWT.NONE);
        tabItem.setText("Plugins");
        
        composite = new Composite(folder, SWT.NONE);
        tabItem.setControl(composite);
        composite.setLayout(null);
        
        pluginstable = new Table(composite, SWT.BORDER | SWT.SINGLE | SWT.CHECK);
        pluginstable.setHeaderVisible(true);
        pluginstable.setLinesVisible(false);
        column = new TableColumn(pluginstable, SWT.LEFT);
        column.setText("Name");
        column.setWidth(300);
        column = new TableColumn(pluginstable, SWT.LEFT);
        column.setText("Type");
        column.setWidth(180);
        pluginstable.setBounds(5, 5, 485, 340);
        populatePlugins();
        
        addbutton = new Button(composite, SWT.PUSH | SWT.CENTER);
        addbutton.setText("&Add Plugin");
        addbutton.addSelectionListener(listener);
        addbutton.setBounds(500, 5, 120, 30);

        removeshare = new Button(composite, SWT.PUSH | SWT.CENTER);
        removeshare.setText("&Remove Plugin");
        removeshare.addSelectionListener(listener);
        removeshare.setBounds(500, 50, 120, 30);
        
        // End of Tabs
        
        Button okbutton = new Button(shell, SWT.PUSH | SWT.CENTER);
        okbutton.setText("&Ok");
        okbutton.addSelectionListener(listener);
        okbutton.setBounds(520, 410, 100, 30);

        Button cancelbutton = new Button(shell, SWT.PUSH | SWT.CENTER);
        cancelbutton.setText("&Cancel");
        cancelbutton.addSelectionListener(listener);
        cancelbutton.setBounds(400, 410, 100, 30);
        shell.pack();
        shell.open();
        
        
    }

    private void populateList()
    {
        Iterator<String> iterator = Configuration.getSharesIterator();
        while (iterator.hasNext())
        {
            String share = iterator.next();
            String map = Configuration.getSharePath(share);
            TableItem item = new TableItem(sharetable, SWT.NONE);
            item.setText(new String[]
            { share, map });
        }
    }
    
    private void populatePlugins()
    {    	
        Iterator<PluginInfo<UIPlugin>> iter1=Configuration.UIplugins.iterator();
        while (iter1.hasNext())
        {        	
            PluginInfo<UIPlugin> p=iter1.next();
            TableItem item=new TableItem(pluginstable, SWT.NONE);
            item.setText(new String[] {p.getMainClass(), "UI"});
            item.setChecked(p.isEnabled());  
            if (p.getMainClass().equals("org.chungles.ui.swt.SWTUI"))
            {
            	item.setGrayed(true);
            }
        }
        
        Iterator<PluginInfo<StandardPlugin>> iter2=Configuration.otherplugins.iterator();
        while (iter2.hasNext())
        {
            PluginInfo<StandardPlugin> p=iter2.next();
            TableItem item=new TableItem(pluginstable, SWT.NONE);
            item.setText(new String[] {p.getMainClass(), "Other"});
            item.setChecked(p.isEnabled());
        }
        
        pluginstable.addSelectionListener(new SelectionListener()
        {
           public void widgetSelected(SelectionEvent e)
           {
               if (e.detail==SWT.CHECK)
               {
                   TableItem item=pluginstable.getSelection()[0];
                   PluginInfo p=PluginAction.findPlugin(item.getText());
                   if (item.getChecked())
                   {
                	   //PluginAction.initPlugin(item.getText());
                	   if (pluginsToDisable.contains(p))
                		   pluginsToDisable.remove(p);
                	   else
                		   pluginsToEnable.add(p);
                   }
	               else
	               {
	            	   // PluginAction.shutdownPlugin(item.getText());
	            	   if (pluginsToEnable.contains(p))
	            		   pluginsToEnable.remove(p);
	            	   else
	            		   pluginsToDisable.add(p);
	               }
               }
           }
           
           public void widgetDefaultSelected(SelectionEvent e)
           {               
           }
        });
    }

    class ButtonListener implements SelectionListener
    {
        public void widgetSelected(SelectionEvent e)
        {
            Button button = (Button) e.getSource();
            if (button.getText().equals("&Cancel"))
            {
            	pluginsToAdd.clear();
                pluginsToRemove.clear();
                pluginsToDisable.clear();
                pluginsToEnable.clear();
                shell.dispose();
            }

            else if (button.getText().equals("&Ok"))
            {
                TableItem[] items = sharetable.getItems();
                int i;

                Configuration.clearShares(); // clear share list

                for (i = 0; i < items.length; i++) // add shares
                {
                    String share = items[i].getText(0);
                    String path = items[i].getText(1);
                    Configuration.addShare(share, path);
                }
                
                // set computer's name
                Configuration.setComputerName(compname.getText());
                
                // set multicast share path
                Configuration.setMCastShare(mcastshare.getText());
                
                // set multicast flow control settings
                Configuration.setMCastThrottled(mcastflow.getSelection());
                Configuration.setMCastKBPSSpeed(Integer.parseInt(mcastspeed.getText()));
                                
                // Add, remove, enable, disable plugins                                 
                Iterator<PluginInfo> iter=pluginsToRemove.iterator();
                while (iter.hasNext())
                {
                	PluginInfo p=iter.next();
                	if (p.isEnabled())
                		PluginAction.shutdownPlugin(p.getMainClass());
                	PluginAction.removePlugin(p.getMainClass());
                }
                
                iter=pluginsToDisable.iterator();
                while (iter.hasNext())
                {
                	PluginInfo p=iter.next();
                	if (p!=null)
                		PluginAction.shutdownPlugin(p.getMainClass());
                }
                
                iter=pluginsToEnable.iterator();
                while (iter.hasNext())
                {
                	PluginInfo p=iter.next();
                	if (!pluginsToRemove.contains(p))
                		PluginAction.initPlugin(p.getMainClass());
                }
                
                pluginsToAdd.clear();
                pluginsToRemove.clear();
                pluginsToDisable.clear();
                pluginsToEnable.clear();
                
                // Save config and close
                ConfigurationParser.saveConfig();
                shell.dispose();
                try
                {                	
                    mDNSUtil.reloadInterfaces();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            else if (button.getText().equals("&Remove Share"))
            {
                int[] selection = sharetable.getSelectionIndices();

                editor.getEditor().dispose();
                sharetable.remove(selection);
            }
            else if (button.getText().equals("&Add Share"))
            {
                DirectoryDialog openDialog = new DirectoryDialog(shell);
                openDialog.setMessage("Select share path");                
                String path = openDialog.open(); 
                if (path!=null && !path.equals(""))
                {
                	TableItem item = new TableItem(sharetable, SWT.NONE);
                	item.setText(new String[]
                	                        { "changeme", path });
                }
            }
            else if (button.getText().equals("&Browse"))
            {
                DirectoryDialog openDialog = new DirectoryDialog(shell);
                openDialog.setMessage("Select multicast share path");                
                String path = openDialog.open(); 
                if (path!=null && !path.equals(""))
                {
                    mcastshare.setText(path);
                }                
            }
            else if (button.equals(mcastflow))
            {
                if (button.getSelection())
                    mcastspeed.setEnabled(true);
                else
                    mcastspeed.setEnabled(false);
            }
            else if (button.getText().equals("&Add Plugin"))
            {
            	FileDialog openDialog = new FileDialog(shell);
                openDialog.setText("Select Plugin");
                openDialog.setFilterExtensions(new String[] {"*.jar"});
                String path = openDialog.open(); 
                if (path!=null && !path.equals(""))
                {
                	PluginInfo p=PluginAction.PeekInJAR(path);
                	if (PluginAction.JARConflicts(path) && !pluginsToRemove.contains(p))
                	{
                		MessageBox box=new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                		box.setMessage("Plugin conflicts with existing plugin");
                		box.open();
                	}
                	else
                	{
                		if (pluginsToRemove.contains(p))
                			pluginsToRemove.remove(p);
                		else
                			pluginsToAdd.add(p);
                		TableItem item=new TableItem(pluginstable, SWT.NONE);
                		if (p.getType()==PluginInfo.UI)
                			item.setText(new String[] {p.getMainClass(), "UI"});
                		else
                			item.setText(new String[] {p.getMainClass(), "Other"});
                		item.setChecked(true);
                		PluginAction.loadPlugin(p.getJARPath(), false);
                		pluginsToEnable.add(p);                		
                	}
                }
            }
            else if (button.getText().equals("&Remove Plugin"))
            {
            	String mainClass=pluginstable.getSelection()[0].getText();            	
            	pluginstable.remove(pluginstable.getSelectionIndex());
            	
            	PluginInfo p=PluginAction.findPlugin(mainClass);
            	if (pluginsToAdd.contains(p))
            	{
            		pluginsToAdd.remove(p);
            		PluginAction.removePlugin(p.getMainClass());
            	}
            	else
            		pluginsToRemove.add(p);
            }
        }

        public void widgetDefaultSelected(SelectionEvent e)
        {
        }
    }
}