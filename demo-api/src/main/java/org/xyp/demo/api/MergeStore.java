package org.xyp.demo.api;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;

public class MergeStore {

    /**
     * algo for combine stores:
     * <ul>
     *     <li>store node that are real store is leaf</li>
     *     <li>DFS? maybe not necessary , to find loop.</li>
     *     <li>current visiting path, a chain to record chain for reporting the loop.</li>
     *     <li>need check orphan.</li>
     *     <li>may be multiple root nodes</li>
     *     <li></li>
     * </ul>
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // Path to the first trust store file
        String trustStoreFile1 = "d:/certstore.p12";
        // Password for the first trust store
        String trustStorePassword1 = "123456";

        // Path to the second trust store file
        String trustStoreFile2 = "d:/google.p12";
        // Password for the second trust store
        String trustStorePassword2 = "234567";

        // Load trust store 1
        KeyStore trustStore1 = KeyStore.getInstance("PKCS12");
        FileInputStream trustStoreStream1 = new FileInputStream(trustStoreFile1);
        trustStore1.load(trustStoreStream1, trustStorePassword1.toCharArray());

        // Load trust store 2
        KeyStore trustStore2 = KeyStore.getInstance("PKCS12");
        FileInputStream trustStoreStream2 = new FileInputStream(trustStoreFile2);
        trustStore2.load(trustStoreStream2, trustStorePassword2.toCharArray());

        // Create a new empty trust store
        KeyStore mergedTrustStore = KeyStore.getInstance("PKCS12");
        mergedTrustStore.load(null, null);

        int idx = 0;
        // Merge certificates from trust store 1
        idx = mergeCertificates(trustStore1, mergedTrustStore, idx);

        // Merge certificates from trust store 2
        mergeCertificates(trustStore2, mergedTrustStore, idx);

        // Save the merged trust store to a new file
        String mergedTrustStoreFile = "d:/mergedTrustStore.p12";
        FileOutputStream mergedTrustStoreStream = new FileOutputStream(mergedTrustStoreFile);
        mergedTrustStore.store(mergedTrustStoreStream, trustStorePassword1.toCharArray());

    }


    private static int mergeCertificates(KeyStore source, KeyStore destination, int i) throws Exception {
        var ee = source.aliases();
        while (ee.hasMoreElements()) {
            String alias = ee.nextElement();
            var cert = source.getCertificate(alias);
            System.out.println(alias);
            destination.setCertificateEntry(alias + "-" + i, cert);
            i++;
        }
        return i;
    }

}
