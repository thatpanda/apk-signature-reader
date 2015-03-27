import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;

import javax.swing.TransferHandler;


@SuppressWarnings("serial")
public class FileTransferHandler extends TransferHandler {

    public interface Callback {
        void handleFileDrop(File f);
    }
    
    private Callback listener;
    
    public FileTransferHandler(Callback c) {
        listener = c;
    }
    
    public boolean canImport(TransferHandler.TransferSupport support) {
        return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        
        Transferable transferable = support.getTransferable();
        try {
            @SuppressWarnings("unchecked")
            java.util.List<File> files = (java.util.List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
            if (files.size() != 1) {
                return false;
            }
            listener.handleFileDrop(files.get(0));
            return true;
        } catch (UnsupportedFlavorException e) {
        } catch (IOException e) {
        }
        return false;
    }
}
