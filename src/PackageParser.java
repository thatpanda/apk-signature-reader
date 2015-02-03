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
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PackageParser {
    private final Object mSync = new Object();
    private WeakReference<byte[]> mReadBuffer;
    
    public class Certificates {
        public Exception error = null;
        public Certificate[] certs = null;
        
        public Certificates(Exception e) {
            error = e;
        }
        
        public Certificates(Certificate[] c) {
            certs = c;
        }
    }
    
    // based on core/java/android/content/pm/PackageParser.java
    public Certificates collectCertificates(String archiveSourcePath) {
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
                    jarFile.close();
                    RuntimeException e = new RuntimeException("Package has no certificates at entry "
                            + je.getName() + "; ignoring!");
                    System.err.println(e.getMessage());
                    return new Certificates(e);
                }
            }
            jarFile.close();

            synchronized (mSync) {
                mReadBuffer = readBufferRef;
            }

            if (certs != null && certs.length > 0) {
                return new Certificates(certs);
            } else {
                RuntimeException e = new RuntimeException("Package has no certificates; ignoring!");
                System.err.println(e.getMessage());
                return new Certificates(e);
            }
        } catch (IOException e) {
            System.err.println("Exception reading " + archiveSourcePath + "\n" + e.getMessage());
            return new Certificates(e);
        } catch (RuntimeException e) {
            System.err.println("Exception reading " + archiveSourcePath + "\n" + e.getMessage());
            return new Certificates(e);
        }
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
            System.err.println("Exception reading " + je.getName() + " in "
                    + jarFile.getName() + "\n" + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Exception reading " + je.getName() + " in "
                    + jarFile.getName() + "\n" + e.getMessage());
        }
        return null;
    }
}
