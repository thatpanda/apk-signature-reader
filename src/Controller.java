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


public class Controller
{
    private String lastError = "";
    
    public String getError()
    {
        return lastError;
    }
    
    public String getKeyHashes( String archiveSourcePath )
    {
        PackageParser parser = new PackageParser();
        Certificate[] certificates = parser.collectCertificates(archiveSourcePath);
        if (certificates == null )
        {
            return null;
        }
        
        KeyTool keytool = new KeyTool();
        String keyhashes = "";
        for (Certificate cert : certificates)
        {
            try
            {
                String MD5 = keytool.getCertFingerPrint( "MD5", cert );
                String SHA1 = keytool.getCertFingerPrint( "SHA1", cert );
                String FacebookHash = getFacebookKeyHash( cert );
                
                keyhashes += "MD5: " + MD5 + "\n";
                keyhashes += "SHA1: " + SHA1 + "\n";
                keyhashes += "Facebook hash: " + FacebookHash + "\n\n";
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        System.out.println(keyhashes);
        
        return keyhashes;
    }
    
    // based on https://developers.facebook.com/docs/getting-started/facebook-sdk-for-android/3.0/#sig
    private String getFacebookKeyHash( Certificate cert )
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(cert.getEncoded());
            return Base64.encodeToString(md.digest(), Base64.DEFAULT);
        } catch (CertificateEncodingException e) {
            lastError = e.getMessage();
            System.err.println(lastError);
        } catch (NoSuchAlgorithmException e) {
            lastError = e.getMessage();
            System.err.println(lastError);
        }
        
        return null;
    }
}
