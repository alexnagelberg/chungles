package org.chungles.ui.swt;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import org.chungles.core.*;

public class SWTNewDirectory implements SelectionListener
{
	public void widgetDefaultSelected(SelectionEvent e)
    {
    }

    public void widgetSelected(SelectionEvent e)
    {
    	final Tree tree=SWTUtil.getInstance().getTree();
    	TreeItem[] items = tree.getSelection();

        final String IP=ShareLister.getIP(items[0]);
        final String path=ShareLister.getPath(items[0]);
        
    	final TreeItem child=new TreeItem(items[0], SWT.NONE);   
    	InputStream in=ClassLoader.getSystemClassLoader().getResourceAsStream("images/folder.gif");	  	    	   			    
		child.setImage(new Image(SWTUtil.getInstance().getShell().getDisplay(), in));
		new TreeItem(child, SWT.NONE);
        tree.setSelection(child);
		
		TreeEditor editor=SWTUtil.getInstance().getTreeEditor();
        
		// Clean up any previous editor control
        Control oldEditor = editor.getEditor();
        if (oldEditor != null)
            oldEditor.dispose();
        
        Text text=new Text(tree, SWT.BORDER);
        text.setFocus();
        text.selectAll();
        
        final FocusAdapter focusadapt=new FocusAdapter()
        {
            public void focusLost(FocusEvent e)
            {
                Text text = (Text)e.getSource();
                String name=text.getText();
                child.setText(name);
                text.dispose();
                if (!remoteCreateDirectory(IP, path,name))
                {
                	SWTUtil.getInstance().deselectAllInTree();
                    child.dispose();
                }
            }
        };
        
        text.addFocusListener(focusadapt);
        
        text.addKeyListener(new KeyListener()
        {
            public void keyPressed(KeyEvent e)
            {
            }

            public void keyReleased(KeyEvent e)
            {
                Text text = (Text) e.getSource();
                if (e.keyCode == '\r') // Enter
                {
                    text.removeFocusListener(focusadapt);
                    String name=text.getText();
                    child.setText(name);
                    text.dispose();
                    if (!remoteCreateDirectory(IP, path,name))
                    {
                    	SWTUtil.getInstance().deselectAllInTree();
                        child.dispose();
                    }
                }
                else if (e.keyCode == 27) // Escape
                {
                    text.removeFocusListener(focusadapt);
                    text.dispose();
                    child.dispose();
                    SWTUtil.getInstance().deselectAllInTree();
                }
            }
        });       
        editor.setEditor(text, child);
    }
    
    private boolean remoteCreateDirectory(String IP, String path, String directory)
    {
    	System.out.println("D: " + directory);
    	System.out.println("P: " + path);
        Client client=new Client(IP);
        boolean status=client.mkdir(path, directory);
        client.close();
        return status;
    }
}
