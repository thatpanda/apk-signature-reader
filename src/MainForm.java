/* 
 * © 2013 thatpanda
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileSystemView;

public class MainForm implements Controller.Callback, FileTransferHandler.Callback {
    private static String title = "APK Signature Reader v0.5";
    
    private JFrame frame = null;
    
    private JPanel topPanel = null;
    private JLabel fileLabel = null;
    private JButton browseButton = null;
    private TextArea textArea = null;
    
    private Controller controller;
    private Controller.Callback callback = this;
    
    public MainForm(Controller c) {
        controller = c;
        
        fileLabel = new JLabel();
        
        browseButton = new JButton("Select apk");
        browseButton.setPreferredSize(new Dimension(100, 30));
        browseButton.addActionListener(new BrowseButtonListener());
        
        topPanel = new JPanel();
        topPanel.add(fileLabel);
        topPanel.add(browseButton);
        
        textArea = new TextArea();
        textArea.setEditable(false);
        
        frame = new JFrame(title);
        frame.add(topPanel,BorderLayout.NORTH);
        frame.add(textArea,BorderLayout.CENTER);
        frame.setSize(500, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTransferHandler(new FileTransferHandler(this));
    }
    
    /**
     * Controller.Callback interface
     */
    @Override
    public void getKeyHashesComplete(Controller.KeyHashes result) {
        if (result.error != null) {
            textArea.setText(result.error.getMessage());
            return;
        }
        
        String message = "";
        message += "MD5: " + result.MD5 + "\n";
        message += "SHA1: " + result.SHA1 + "\n";
        message += "SHA256: " + result.SHA256 + "\n";
        message += "Facebook hash: " + result.FacebookHash;
        System.out.println(message);
        textArea.setText(message);
    }
    
    /**
     * FileTransferHandler.Callback interface
     */
    @Override
    public void handleFileDrop(File file) {
        getKeyHashes(file);
    }
    
    public void show() {
        frame.setVisible(true);
    }
    
    private class BrowseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            FileSystemView fileSystemView = FileSystemView.getFileSystemView();
            
            final JFileChooser fileChooser = new JFileChooser(fileSystemView.getHomeDirectory());
            fileChooser.setFileFilter(new ApkFilter());
            
            if (fileChooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            
            getKeyHashes(fileChooser.getSelectedFile());
        }
    }
    
    private void getKeyHashes(File file) {
        if (file == null) {
            return;
        }
        
        fileLabel.setText(file.getName());
        textArea.setText("loading...");
        
        ApkFilter fileFilter = new ApkFilter();
        if (!file.isFile() || !fileFilter.accept(file)) {
            textArea.setText("un-supported file type");
            return;
        }
        
        controller.getKeyHashes(file.getAbsolutePath(), callback);
    }
}
