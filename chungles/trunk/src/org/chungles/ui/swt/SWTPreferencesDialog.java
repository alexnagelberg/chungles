package org.chungles.ui.swt;

import java.io.InputStream;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import org.chungles.core.*;

// Singleton
public class SWTPreferencesDialog
{
    private Display display;
    private Shell shell;
    private TableEditor editor;
    private Table table;
    private Text compname, tempdir;
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
    }

    public void openDialog()
    {
        if (!(shell == null || shell.isDisposed()))
            return;

        ButtonListener listener = new ButtonListener();

        shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setText("Chungles Preferences");
        InputStream in=ClassLoader.getSystemResourceAsStream("images/chungles.png");	
        shell.setImage(new Image(display, in));
        shell.setLayout(null);

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

        CTabItem tabItem = new CTabItem(folder, SWT.NONE);
        tabItem.setText("Shares");

        Composite composite = new Composite(folder, SWT.NONE);
        tabItem.setControl(composite);
        composite.setLayout(null);

        table = new Table(composite, SWT.BORDER | SWT.SINGLE);
        editor = new TableEditor(table);
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;
        editor.minimumWidth = 50;
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText("Name");
        column.setWidth(100);
        column = new TableColumn(table, SWT.LEFT);
        column.setText("Path");
        column.setWidth(380);
        table.setBounds(5, 5, 485, 345);
        table.addSelectionListener(new SelectionAdapter()
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

                Text newEditor = new Text(table, SWT.BORDER);
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
        addbutton.setBounds(500, 5, 115, 30);

        Button removeshare = new Button(composite, SWT.PUSH | SWT.CENTER);
        removeshare.setText("&Remove Share");
        removeshare.addSelectionListener(listener);
        removeshare.setBounds(500, 50, 115, 30);

        Label label=new Label(composite, SWT.NONE);
        label.setText("Computer name: ");
        label.setBounds(5, 355, 110, 20);
        
        compname = new Text(composite, SWT.BORDER);
        compname.setText(Configuration.getComputerName());
        compname.setBounds(115, 355, 375, 20);                
        
        Button okbutton = new Button(shell, SWT.PUSH | SWT.CENTER);
        okbutton.setText("&Ok");
        okbutton.addSelectionListener(listener);
        okbutton.setBounds(520, 410, 100, 30);

        Button cancelbutton = new Button(shell, SWT.PUSH | SWT.CENTER);
        cancelbutton.setText("&Cancel");
        cancelbutton.addSelectionListener(listener);
        cancelbutton.setBounds(400, 410, 100, 30);

        shell.open();
        shell.setBounds(50, 50, 640, 480);
    }

    private void populateList()
    {
        Iterator<String> iterator = Configuration.getSharesIterator();
        while (iterator.hasNext())
        {
            String share = iterator.next();
            String map = Configuration.getSharePath(share);
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[]
            { share, map });
        }
    }

    class ButtonListener implements SelectionListener
    {
        public void widgetSelected(SelectionEvent e)
        {
            Button button = (Button) e.getSource();
            if (button.getText().equals("&Cancel"))
            {
                shell.dispose();
            }

            else if (button.getText().equals("&Ok"))
            {
                TableItem[] items = table.getItems();
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
                
                ConfigurationParser.saveConfig();
                shell.dispose();
            }
            else if (button.getText().equals("&Remove Share"))
            {
                int[] selection = table.getSelectionIndices();

                editor.getEditor().dispose();
                table.remove(selection);
            }
            else if (button.getText().equals("&Add Share"))
            {
                DirectoryDialog openDialog = new DirectoryDialog(shell);
                openDialog.setMessage("Select share path");                
                String path = openDialog.open(); 
                if (path!=null && !path.equals(""))
                {
                	TableItem item = new TableItem(table, SWT.NONE);
                	item.setText(new String[]
                	                        { "changeme", path });
                }
            }
            else if (button.getText().equals("..."))
            {
                DirectoryDialog openDialog=new DirectoryDialog(shell);
                openDialog.setMessage("Select temp folder");
                openDialog.open();
                String path=openDialog.getFilterPath();
                tempdir.setText(path);
            }
        }

        public void widgetDefaultSelected(SelectionEvent e)
        {
        }
    }
}