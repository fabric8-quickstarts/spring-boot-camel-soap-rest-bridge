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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.ServiceStatus;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jboss.fuse.wsdl2rest.util.SpringCamelContextFactory;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.keycloak.OAuth2Constants;
import org.keycloak.util.BasicAuthHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static org.junit.Assert.fail;

public class JaxRsClientTest {

    public static Logger LOG = LoggerFactory.getLogger(JaxRsClientTest.class);
    static String JAXWS_URI = "http://localhost:8282/WeatherService";
    static QName SERVICE_QNAME = new QName("http://ibm.com/wdata", "weatherService");
    static String JAXRS_URL = "http://localhost:8080/cxf/jaxrs";

    @BeforeClass
    public static void beforeClass() {
        Object implementor = new WeatherPortImpl();
     
        EndpointImpl impl = (EndpointImpl)Endpoint.publish(JAXWS_URI, implementor);
        Map<String, Object> inProps = new HashMap<>();
        inProps.put("action", "Timestamp SAMLTokenSigned Signature");
        inProps.put("signatureVerificationPropFile", "bob.properties");
        

        impl.getInInterceptors().add(new WSS4JInInterceptor(inProps));
        
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

        Service service = Service.create(new URL(JAXWS_URI + "?wsdl"), SERVICE_QNAME);
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
            Assert.assertEquals("A security error was encountered when verifying the message", ex.getMessage());
        }
    }
    
    @Test
    public void testCamelClient() throws Exception {

        URL resourceUrl = getClass().getResource("/spring/camel-context.xml");
        CamelContext camelctx = SpringCamelContextFactory.createSingleCamelContext(resourceUrl, null);
        camelctx.start();
        try {
            Assert.assertEquals(ServiceStatus.Started, camelctx.getStatus());
            
            WeatherRequest request = new WeatherRequest();
            request.setZipcode("M3H 2J8");
            ProducerTemplate producer = camelctx.createProducerTemplate();
            
            WeatherResponse response = producer.requestBody("direct:weatherRequest", request, WeatherResponse.class);
            
            Assert.assertEquals("M3H 2J8", response.getZip());
            Assert.assertEquals("LA", response.getCity());
            Assert.assertEquals("CA", response.getState());
            Assert.assertEquals("95%", response.getHumidity());
            Assert.assertEquals("28", response.getTemperature());
            
        } finally {
            camelctx.stop();
        }
    }

    @Test
   
    public void testRestClient() throws Exception {
        
        String accessToken = fetchAccessToken();

        

        URL resourceUrl = getClass().getResource("/spring/camel-context.xml");
        CamelContext camelctx = SpringCamelContextFactory.createSingleCamelContext(resourceUrl, null);
        camelctx.start();
        try {
            Assert.assertEquals(ServiceStatus.Started, camelctx.getStatus());
            
            Client client = ClientBuilder.newClient().register(JacksonJsonProvider.class).register(LoggingFeature.class);
            
                        
                
            WeatherRequest request = new WeatherRequest();
            request.setZipcode("M3H 2J8");
            
            
            // POST @WeatherPortType#weatherRequest(WeatherRequest)
            String payload = new ObjectMapper().writeValueAsString(request);
            
            System.out.println("the payload is ================>" + payload);

            WeatherResponse response = client.target(JAXRS_URL + "/request").
                request().header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken).post(Entity.entity(payload, MediaType.APPLICATION_JSON), WeatherResponse.class);
            Assert.assertEquals("M3H 2J8", response.getZip());
            Assert.assertEquals("LA", response.getCity());
            Assert.assertEquals("CA", response.getState());
            Assert.assertEquals("95%", response.getHumidity());
            Assert.assertEquals("28", response.getTemperature());           

        } finally {
            camelctx.stop();
        }
    }


    private String fetchAccessToken()
        throws UnsupportedEncodingException, IOException, ClientProtocolException {
        String accessToken = null;

        try (CloseableHttpClient client = HttpClients.createMinimal()) {
            // "4.3.  Resource Owner Password Credentials Grant"
            // from https://tools.ietf.org/html/rfc6749#section-4.3
            // we use "resource owner" credentials directly to obtain the token
            HttpPost post = new HttpPost("http://localhost:8180/auth/realms/fuse7karaf/protocol/openid-connect/token");
            LinkedList<NameValuePair> params = new LinkedList<>();
            params.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, OAuth2Constants.PASSWORD));
            params.add(new BasicNameValuePair("username", "admin"));
            params.add(new BasicNameValuePair("password", "passw0rd"));
            UrlEncodedFormEntity postData = new UrlEncodedFormEntity(params);
            post.setEntity(postData);

            String basicAuth = BasicAuthHelper.createHeader("cxf", "f1ec716d-2262-434d-8e98-bf31b6b858d6");
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

    
    
    

    @Test
    public void helloEmbeddedAuthenticated() throws Exception {

        String accessToken = fetchAccessToken();

        if (accessToken != null) {
            try (CloseableHttpClient client = HttpClients.createMinimal()) {
                // "The OAuth 2.0 Authorization Framework: Bearer Token Usage"
                // https://tools.ietf.org/html/rfc6750
                HttpGet get = new HttpGet("http://localhost:8080/cxf/jaxrs/service/hello/hi");
                get.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
                CloseableHttpResponse response = client.execute(get);

                LOG.info("response: {}", EntityUtils.toString(response.getEntity()));
                response.close();
            }
        }
    }

}
