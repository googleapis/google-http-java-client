package com.google.api.client.http.javanet;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateFactory;

public class SSLSocketFactoryExtension {
    private final javax.net.ssl.SSLSocketFactory socketFactory;

    SSLSocketFactoryExtension(SSLContext sslContext)
            throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException {
        socketFactory = sslContext.getSocketFactory();
    }

    public static SSLSocketFactory getSslSocketFactory(KeyStore keyStore, String password) throws GeneralSecurityException {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        return sslContext.getSocketFactory();
    }

    public static KeyStore loadKeyStore(InputStream inputStream, String password) throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(inputStream, password.toCharArray());
        return keyStore;
    }

    public static java.security.cert.X509Certificate loadX509Certificate(InputStream inputStream) throws GeneralSecurityException, IOException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (java.security.cert.X509Certificate) certificateFactory.generateCertificate(inputStream);
    }

}
