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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PackageParser {
    private String lastError = "";
    
    private final Object mSync = new Object();
    private WeakReference<byte[]> mReadBuffer;
    
    public String getError() {
        return lastError;
    }
    
    public String[] getFacebookKeyHashes( String archiveSourcePath ) {
        Certificate[] certificates = collectCertificates(archiveSourcePath);
        if (certificates == null ) {
            return null;
        }
        
        try {
            final int N = certificates.length;
            String[] keyhashes = new String[N];
            for (int i=0; i<N; i++) {
                System.out.println(i + ".\n" +
                    "Type: " + certificates[i].getType() + "\n" +
                    "Public key: " + certificates[i].getPublicKey() + "\n");
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(certificates[i].getEncoded());
                keyhashes[i] = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                System.out.print("Facebook KeyHash: " + keyhashes[i]);
            }
            
            return keyhashes;
        } catch (CertificateEncodingException e) {
            lastError = "Exception reading " + archiveSourcePath + "\n" + e.getMessage();
            System.err.println(lastError);
        } catch (NoSuchAlgorithmException e) {
            lastError = e.getMessage();
            System.err.println(lastError);
        }
        
        return null;
    }
    
    // based on core/java/android/content/pm/PackageParser.java
    public Certificate[] collectCertificates(String archiveSourcePath) {
        WeakReference<byte[]> readBufferRef;
        byte[] readBuffer = null;
        synchronized (mSync) {
            readBufferRef = mReadBuffer;
            if (readBufferRef != null) {
                mReadBuffer = null;
                readBuffer = readBufferRef.get();
            }
            if (readBuffer == null) {
                readBuffer = new byte[8192];
                readBufferRef = new WeakReference<byte[]>(readBuffer);
            }
        }

        try {
            JarFile jarFile = new JarFile(archiveSourcePath);

            Certificate[] certs = null;

            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry je = entries.nextElement();
                if (je.isDirectory()) continue;

                final String name = je.getName();

                if (name.startsWith("META-INF/"))
                    continue;

                certs = loadCertificates(jarFile, je, readBuffer);
                if (certs == null) {
                    lastError = "Package has no certificates at entry "
                            + je.getName() + "; ignoring!";
                    System.err.println(lastError);
                    jarFile.close();
                    return null;
                }
            }
            jarFile.close();

            synchronized (mSync) {
                mReadBuffer = readBufferRef;
            }

            if (certs != null && certs.length > 0) {
                return certs;
            } else {
                lastError = "Package has no certificates; ignoring!";
                System.err.println(lastError);;
            }
        } catch (IOException e) {
            lastError = "Exception reading " + archiveSourcePath + "\n" + e.getMessage();
            System.err.println(lastError);
        } catch (RuntimeException e) {
            lastError = "Exception reading " + archiveSourcePath + "\n" + e.getMessage();
            System.err.println(lastError);
        }

        return null;
    }
    
    // based on core/java/android/content/pm/PackageParser.java
    public Certificate[] loadCertificates(JarFile jarFile, JarEntry je,
            byte[] readBuffer) {
        try {
            // We must read the stream for the JarEntry to retrieve
            // its certificates.
            InputStream is = new BufferedInputStream(jarFile.getInputStream(je));
            while (is.read(readBuffer, 0, readBuffer.length) != -1) {
                // not using
            }
            is.close();
            return je != null ? je.getCertificates() : null;
        } catch (IOException e) {
            lastError = "Exception reading " + je.getName() + " in "
                    + jarFile.getName() + "\n" + e.getMessage();
            System.err.println(lastError);
        } catch (RuntimeException e) {
            lastError = "Exception reading " + je.getName() + " in "
                    + jarFile.getName() + "\n" + e.getMessage();
            System.err.println(lastError);
        }
        return null;
    }
}
