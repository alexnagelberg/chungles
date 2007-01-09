package org.chungles.ui.swt;

import java.io.InputStream;
import java.net.InetAddress;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import org.chungles.application.*;
public class SWTAddNodeDialog
{
    private static SWTAddNodeDialog dialog;
    private Display display;
    private Shell shell;
    private Text addytextbox;
    
    private SWTAddNodeDialog(Display display)
    {
        this.display = display;
    }
    
    public static SWTAddNodeDialog getInstance(Display display)
    {
        if (dialog == null)
        {
            dialog = new SWTAddNodeDialog(display);
        }

        return dialog;
    }
    
    public void openDialog()
    {
        if (!(shell == null || shell.isDisposed()))
            return;     

        shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        shell.setText("Add Chungles Node");
        InputStream in=getClass().getClassLoader().getResourceAsStream("images/chungles.png");	
        shell.setImage(new Image(display, in));
        shell.setLayout(null);
        
        Label label=new Label(shell, SWT.NONE);
        label.setText("Host or IP: ");
        label.setBounds(0, 15, 65, 15);
        
        addytextbox=new Text(shell, SWT.NONE);
        addytextbox.setBounds(70, 12, 300, 20);        
        
        Button okbutton=new Button(shell, SWT.NONE);
        okbutton.setText("Ok");
        okbutton.setBounds(290, 50, 80, 30);
        okbutton.addSelectionListener(new SelectionListener()
                {
            		public void widgetDefaultSelected(SelectionEvent e) {}
            		
            		public void widgetSelected(SelectionEvent e)
            		{
            		    addNode(addytextbox.getText());
            		}
                });
        
        shell.open();
        shell.setBounds(50, 50, 390, 110);
    }
    
    private void addNode(String addy)
    {
        try
        {
            String ip=InetAddress.getByName(addy).getHostAddress();
            NodeDetect.addNode(ip, "Chungles Node [" + ip + "]");
            shell.dispose();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
