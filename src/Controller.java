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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;


public class Controller
{
    public interface Callback {
        void getKeyHashesComplete(KeyHashes result);
    }
    
    public class KeyHashes {
        public Exception error = null;
        
        public String MD5 = null;
        public String SHA1 = null;
        public String SHA256 = null;
        public String FacebookHash = null;
    }
    
    public void getKeyHashes(String archiveSourcePath, Callback callback) {
        BackgroundTask task = new BackgroundTask(archiveSourcePath, callback);
        task.execute();
    }
    
    private class BackgroundTask extends SwingWorker<KeyHashes, Void> {
        private String filepath;
        private Callback callback;
        
        public BackgroundTask(String filepath, Callback callback) {
            this.filepath = filepath;
            this.callback = callback;
        }
        
        @Override
        public KeyHashes doInBackground() {
            return getKeyHashes(filepath);
        }
        
        @Override
        protected void done() {
            try {
                callback.getKeyHashesComplete(get());
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            } catch (ExecutionException e) {
                System.err.println(e.getMessage());
            }
        }
    }
    
    private KeyHashes getKeyHashes(String archiveSourcePath) {
        PackageParser parser = new PackageParser();
        PackageParser.Certificates certificates = parser.collectCertificates(archiveSourcePath);
        if (certificates.error != null) {
            KeyHashes result = new KeyHashes();
            result.error = certificates.error;
            return result;
        }
        
        KeyTool keytool = new KeyTool();
        KeyHashes result = new KeyHashes();
        Certificate cert = certificates.certs[0];
        try {
            result.MD5 = keytool.getCertFingerPrint("MD5", cert);
            result.SHA1 = keytool.getCertFingerPrint("SHA1", cert);
            result.SHA256 = keytool.getCertFingerPrint("SHA-256", cert);
        } catch(Exception e) {
            System.err.println(e.getMessage());
            result.error = e;
        }
        
        try {
            result.FacebookHash = getFacebookKeyHash(cert);
        } catch(CertificateEncodingException e) {
            System.err.println(e.getMessage());
            result.error = e;
        } catch(NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
            result.error = e;
        }
        return result;
    }
    
    // based on https://developers.facebook.com/docs/getting-started/facebook-sdk-for-android/3.0/#sig
    private String getFacebookKeyHash(Certificate cert) throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA");
        md.update(cert.getEncoded());
        return Base64.encodeToString(md.digest(), Base64.DEFAULT);
    }
}
