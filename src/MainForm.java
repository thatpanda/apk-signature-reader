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
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileSystemView;

public class MainForm {
    private static String title = "Facebook Key Hash Reader v0.2";
    
    private JFrame frame = null;
    
    private JPanel topPanel = null;
    private JLabel fileLabel = null;
    private JButton browseButton = null;
    private TextArea textArea = null;
    
    public MainForm() {
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
    }
    
    public void show()
    {
        frame.setVisible(true);
    }
    
    private class BrowseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            FileSystemView fileSystemView = FileSystemView.getFileSystemView();
            
            final JFileChooser fileChooser = new JFileChooser(fileSystemView.getHomeDirectory());
            fileChooser.setFileFilter(new ApkFilter());
            
            if( fileChooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION ) {
                return;
            }
            
            File file = fileChooser.getSelectedFile();
            if (file != null) {
                fileLabel.setText(file.getName());
                textArea.setText("loading...");
                
                new ParseFileTask(file.getAbsolutePath()).execute();
            }
        }
    }
    
    private class ParseFileTask extends SwingWorker<String, Object> {
        private String filepath;
        
        public ParseFileTask(String path) {
            filepath = path;
        }
        
        @Override
        public String doInBackground() {
            PackageParser parser = new PackageParser();
            String[] keyhashes = parser.getFacebookKeyHashes(filepath);
            if (keyhashes != null ) {
                String str = "";
                for (String keyhash : keyhashes) {
                    if (!str.isEmpty()) {
                        str += "\n";
                    }
                    str += keyhash;
                }
                return str;
            } else {
                return parser.getError();
            }
        }

        @Override
        protected void done() {
            try { 
                textArea.setText(get());
            } catch (Exception e) {
                textArea.setText(e.getMessage());
            }
        }
    }
}
