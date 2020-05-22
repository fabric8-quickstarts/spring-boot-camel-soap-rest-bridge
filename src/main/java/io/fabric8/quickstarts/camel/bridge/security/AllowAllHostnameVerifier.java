package io.fabric8.quickstarts.camel.bridge.security;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

/**
 * Since Openshift self-signed certificate can't have accurate
 * hostname of the service, we use this  AllowAllHostnameVerifier
 * in the quickstart, and shouldn't use this in production
 */
public class AllowAllHostnameVerifier implements javax.net.ssl.HostnameVerifier {

    @Override
    public boolean verify(String host, SSLSession session) {
        try {
            Certificate[] certs = session.getPeerCertificates();
            return certs != null && certs[0] instanceof X509Certificate;
        } catch (SSLException e) {
            return false;
        }
    }


}