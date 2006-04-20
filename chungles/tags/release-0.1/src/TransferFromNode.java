import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

public class TransferFromNode implements SelectionListener
{
    private static long totalReceived;

    public void widgetDefaultSelected(SelectionEvent e)
    {
    }

    public void widgetSelected(SelectionEvent e)
    {
        int i;
        Shell shell = SWTUtil.getInstance().getShell();
        TreeItem[] items = SWTUtil.getInstance().getTree().getSelection();
        FileList lists[] = new FileList[items.length];
        String IPs[] = new String[items.length];
        int numFiles = 0;
        long totalSize = 0;

        // Save dialog
        DirectoryDialog directoryDialog = new DirectoryDialog(shell);
        directoryDialog.setMessage("Select save path");
        directoryDialog.open();
        String savepath = directoryDialog.getFilterPath();

        // If they cancel
        if (savepath.equals(""))
            return;

        // Ask client(s) to recurse the path(s)
        for (i = 0; i < items.length; i++)
        {
            IPs[i] = ShareLister.getIP(items[i]);
            Client client = new Client(IPs[i]);
            lists[i] = client.recurseFiles(ShareLister.getPath(items[i]));
            numFiles += FileList.getNumFiles();
            totalSize += FileList.getTotalSize();
            client.close();
        }

        // Open retrieve dialog
        Display display = SWTUtil.getInstance().getShell().getDisplay();
        SWTTransferDialog dialog = SWTTransferDialog.getInstance(display);
        dialog.openDialog();

        getFiles(lists, IPs, numFiles, totalSize, savepath);
    }

    private void getFiles(final FileList[] lists, final String[] IPs,
            final int numFiles, final long totalSize, final String savepath)
    {
        totalReceived = 0;
        Thread thread = new Thread()
        {
            public void run()
            {
                int curFile = 0;
                Display display = SWTUtil.getInstance().getShell().getDisplay();
                final SWTTransferDialog dialog = SWTTransferDialog
                        .getInstance(display);
                int i;
                dialog.progressThread();
                for (i = 0; i < lists.length; i++)
                {
                    FileList list = lists[i];
                    Client client = new Client(IPs[i]);
                    int offset = list.getRemotePath().lastIndexOf('/') + 1;
                    String separator = System.getProperty("file.separator");
                    while (list.getRemotePath() != null)
                    {
                        String outputfile = savepath + separator
                                + list.getRemotePath().substring(offset);
                        if (client.requestRetrieveFile(list, outputfile))
                        {
                            final long fileSize = list.getSize();
                            dialog.updateLables(list.getRemotePath(),
                                    curFile + 1, numFiles);
                            client.retrieveFile(outputfile, list,
                                    new ReceiveProgressListener()
                                    {
                                        private long lastReceived = 0;

                                        public void progressUpdate(
                                                long bytesReceived)
                                        {
                                            totalReceived += bytesReceived
                                                    - lastReceived;
                                            lastReceived = bytesReceived;
                                            dialog.updateProgress(
                                                    bytesReceived, fileSize,
                                                    totalReceived, totalSize);
                                        }
                                    });
                        }
                        else
                        {
                            // Error, cannot get, abort
                            System.out.println("ERR RETRIEVING");
                            dialog.closeDialog();
                            return;
                        }
                        list = list.getNext();
                    }
                    client.close();
                }
                dialog.closeDialog();
            }
        };
        thread.start();
    }
}