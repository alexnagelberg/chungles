import org.eclipse.swt.dnd.*;
import org.eclipse.swt.widgets.*;

public class TransferFromNode extends DragSourceAdapter
{
    public void dragSetData(DragSourceEvent event)
    {        
        TreeItem[] items=SWTUtil.getInstance().getTree().getSelection();        
        int i;        
        for (i=0; i<items.length; i++)
        {
            Client client=new Client(ShareLister.getIP(items[i]));
            FileList list=client.recurseFiles(ShareLister.getPath(items[i]));            
            client.close();
            while (list!=null)
            {
                System.out.println(list.getRemotePath());
                list=list.getNext();
            }
        }
        
        // Array of temp file names
        //event.data=files;
        
    }
}
