/*
 *  Copyright 2005-2018 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package org.jboss.fuse.quickstarts.security.keycloak.cxf;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.ibm.wdata.WeatherPortImpl;
import com.ibm.wdata.WeatherPortType;
import com.ibm.wdata.WeatherRequest;
import com.ibm.wdata.WeatherResponse;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.client.spec.ClientImpl.WebTargetImpl;
import org.apache.cxf.jaxrs.impl.ResponseImpl;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.keycloak.OAuth2Constants;
import org.keycloak.util.BasicAuthHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JaxRsClientTest {

    public static Logger LOG = LoggerFactory.getLogger(JaxRsClientTest.class);
    static String WEATHER_HOST = System.getProperty("weather.service.host", "192.168.0.11");
    static String JAXWS_URI_STS = "http://" + WEATHER_HOST + ":8283/WeatherService";
   
    static QName SERVICE_QNAME = new QName("http://ibm.com/wdata", "weatherService");
    static String CAMEL_ROUTE_HOST = System
        .getProperty("camel.route.host", "http://camel-bridge-springboot-xml-openshift.192.168.64.33.nip.io");
    // static String CAMEL_ROUTE_HOST = "http://localhost:8080";
    static String JAXRS_URL = CAMEL_ROUTE_HOST + "/camelcxf/jaxrs";
    static String SSO_URL = System.getProperty("sso.server");
    CloseableHttpClient httpClient;
    SSLContext sslContext;

    @BeforeClass
    public static void beforeClass() {
        Object implementor = new WeatherPortImpl();

      

        EndpointImpl impl = (EndpointImpl)Endpoint.publish(JAXWS_URI_STS, implementor);

        Map<String, Object> inProps = new HashMap<>();
        inProps.put("action", "Timestamp SAMLTokenSigned");
        inProps.put("signatureVerificationPropFile", "bob.properties");
        impl.getProperties().put("ws-security.saml2.validator", "org.example.Saml2Validator");

        impl.getInInterceptors().add(new WSS4JInInterceptor(inProps));
        impl.getInInterceptors().add(new LoggingInInterceptor());
        impl.getOutInterceptors().add(new LoggingOutInterceptor());
    }

    @BeforeClass
    public static void initLogging() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @AfterClass
    public static void cleanupLogging() {
        SLF4JBridgeHandler.uninstall();
    }

    @Test
    public void testJavaClient() throws Exception {

        Service service = Service.create(new URL(JAXWS_URI_STS + "?wsdl"), SERVICE_QNAME);
        WeatherPortType port = service.getPort(WeatherPortType.class);
        Assert.assertNotNull("Address not null", port);
        try {

            WeatherRequest request = new WeatherRequest();
            request.setZipcode("M3H 2J8");
            WeatherResponse response = port.weatherRequest(request);
            fail("should fail caz no security");
            Assert.assertEquals("M3H 2J8", response.getZip());
            Assert.assertEquals("LA", response.getCity());
            Assert.assertEquals("CA", response.getState());
            Assert.assertEquals("95%", response.getHumidity());
            Assert.assertEquals("28", response.getTemperature());
        } catch (Exception ex) {
            Assert.assertEquals("A security error was encountered when verifying the message",
                                ex.getMessage());
        }
    }

    

    private void trustOpenshiftSelfSignedCert(WebTargetImpl target) {
        target.request();
        HTTPConduit conduit = (HTTPConduit)WebClient.getConfig(target.getWebClient()).getConduit();
        TLSClientParameters params = conduit.getTlsClientParameters();

        if (params == null) {
            params = new TLSClientParameters();
            conduit.setTlsClientParameters(params);
        }
        

        params.setTrustManagers(new TrustManager[] { new X509TrustManager() {
            
            public void checkClientTrusted(X509Certificate[] chain,
                    String authType) throws java.security.cert.CertificateException {
            }

            
            public void checkServerTrusted(X509Certificate[] chain,
                    String authType) throws java.security.cert.CertificateException {
            }

            
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }});
        
        params.setDisableCNCheck(true);
    }

    @Test

    public void testRestClientWithInvalidPayload() throws Exception {

        String accessToken = fetchAccessToken();

        Client client = ClientBuilder.newClient().register(JacksonJsonProvider.class)
            .register(LoggingFeature.class);

        WeatherRequest request = new WeatherRequest();
        request.setZipcode("M3H 2J8");

        // POST @WeatherPortType#weatherRequest(WeatherRequest)
        String payload = new ObjectMapper().writeValueAsString(request);
        try {
            WebTargetImpl target = (WebTargetImpl)client.target(JAXRS_URL + "/request");
            this.trustOpenshiftSelfSignedCert(target);
            target.request().accept(MediaType.APPLICATION_XML)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header("user_key", "9f37d93b27f7b552f30116919cc59048")
                .post(Entity.entity(payload, MediaType.APPLICATION_JSON), WeatherResponse.class);
            fail("we have enabled clientRequestValidation for camel rest dsl, but the request accept header can't match the produces definition in camel rest dsl, hence expect http 406 NotAcceptableException");
        } catch (javax.ws.rs.NotAcceptableException ex) {
            assertTrue(ex.getMessage().contains("HTTP 406 Not Acceptable"));
        }

    }

    @Test

    public void testRestClientWithSTS() throws Exception {

        String accessToken = fetchAccessToken();

        Client client = ClientBuilder.newClient().register(JacksonJsonProvider.class)
            .register(LoggingFeature.class);

        WeatherRequest request = new WeatherRequest();
        request.setZipcode("M3H 2H8");

        // POST @WeatherPortType#weatherRequest(WeatherRequest)
        String payload = new ObjectMapper().writeValueAsString(request);
        WebTargetImpl target = (WebTargetImpl)client.target(JAXRS_URL + "/request");
        trustOpenshiftSelfSignedCert(target);
        WeatherResponse response = target.request().header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .header("user_key", "9f37d93b27f7b552f30116919cc59048")
            .post(Entity.entity(payload, MediaType.APPLICATION_JSON), WeatherResponse.class);
        Assert.assertEquals("M3H 2H8", response.getZip());
        Assert.assertEquals("LA", response.getCity());
        Assert.assertEquals("CA", response.getState());
        Assert.assertEquals("95%", response.getHumidity());
        Assert.assertEquals("28", response.getTemperature());

    }

    @Test

    public void testRestClientWithSTSInvalidZipCode() throws Exception {

        String accessToken = fetchAccessToken();

        Client client = ClientBuilder.newClient().register(JacksonJsonProvider.class)
            .register(LoggingFeature.class);

        WeatherRequest request = new WeatherRequest();
        request.setZipcode("M3H 278");

        // POST @WeatherPortType#weatherRequest(WeatherRequest)
        String payload = new ObjectMapper().writeValueAsString(request);

        try {
            WebTargetImpl target = (WebTargetImpl)client.target(JAXRS_URL + "/request");
            trustOpenshiftSelfSignedCert(target);
            
            target.request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header("user_key", "9f37d93b27f7b552f30116919cc59048")
                .post(Entity.entity(payload, MediaType.APPLICATION_JSON), WeatherResponse.class);
            fail("should throw schema validation exception since \"M3H 278\" isn't a valid zip code");
        } catch (javax.ws.rs.WebApplicationException ex) {
            org.apache.cxf.jaxrs.impl.ResponseImpl resp = (ResponseImpl)ex.getResponse();

            InputStream is = (InputStream)resp.getEntity();
            if (is != null) {
                CachedOutputStream bos = new CachedOutputStream();
                try {
                    IOUtils.copy(is, bos);

                    bos.flush();
                    is.close();
                    bos.close();
                    String faultMessage = new String(bos.getBytes());
                    assertTrue(faultMessage
                        .contains("org.apache.cxf.interceptor.Fault: Marshalling Error: cvc-pattern-valid: Value 'M3H 278' is not facet-valid with respect to pattern '[A-Z][0-9][A-Z] [0-9][A-Z][0-9]' for type 'zipType'."));
                } catch (IOException e) {
                    throw new Fault(e);
                }
            }

        }

    }

    private String fetchAccessToken()
        throws UnsupportedEncodingException, IOException, ClientProtocolException {
        String accessToken = null;

        try (CloseableHttpClient client = getCloseableHttpClient()) {
            // "4.3. Resource Owner Password Credentials Grant"
            // from https://tools.ietf.org/html/rfc6749#section-4.3
            // we use "resource owner" credentials directly to obtain the token
            HttpPost post = new HttpPost(SSO_URL
                                         + "/auth/realms/camel-soap-rest-bridge/protocol/openid-connect/token");
            // HttpPost post = new
            // HttpPost("https://192.168.0.11:8543/auth/realms/fuse7karaf/protocol/openid-connect/token");
            LinkedList<NameValuePair> params = new LinkedList<>();
            params.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, OAuth2Constants.PASSWORD));
            params.add(new BasicNameValuePair("username", "admin"));
            params.add(new BasicNameValuePair("password", "passw0rd"));
            UrlEncodedFormEntity postData = new UrlEncodedFormEntity(params);
            post.setEntity(postData);

            String basicAuth = BasicAuthHelper.createHeader("camel-bridge",
                                                            "f1ec716d-2262-434d-8e98-bf31b6b858d6");
            post.setHeader("Authorization", basicAuth);
            CloseableHttpResponse response = client.execute(post);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.getEntity().getContent());
            if (json.get("error") == null) {
                accessToken = json.get("access_token").asText();
                LOG.info("token: {}", accessToken);
            } else {
                LOG.warn("error: {}, description: {}", json.get("error"), json.get("error_description"));
                fail();
            }
            response.close();
        }
        return accessToken;
    }

    

    private CloseableHttpClient getCloseableHttpClient() {
        if (httpClient != null) {
            return httpClient;
        }
        try {
            httpClient = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                    public boolean isTrusted(X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                        return true;
                    }
                }).build()).build();
        } catch (KeyManagementException e) {
            LOG.error("KeyManagementException in creating http client instance", e);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("NoSuchAlgorithmException in creating http client instance", e);
        } catch (KeyStoreException e) {
            LOG.error("KeyStoreException in creating http client instance", e);
        }
        return httpClient;
    }

    
}
