package io.fabric8.quickstarts.camel.bridge;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.WebApplicationInitializer;

@ComponentScan   
@SpringBootApplication
@ImportResource({"classpath:spring/camel-context.xml"})
public class Application extends SpringBootServletInitializer implements WebApplicationInitializer {

   public static void main(String[] args) {
      System.setProperty("weather.service.host", System.getProperty("weather.service.host", "localhost"));
      System.setProperty("sso.server", System.getProperty("sso.server", "http://localhost:8180"));
      
      try {
        setTrustStore();
    } catch (IOException e) {
        e.printStackTrace();
    } 
      SpringApplication.run(Application.class, args);
   }
   
   private static void setTrustStore() throws IOException {
       InputStream is = Application.class.getClassLoader().getResourceAsStream("openshiftcerts");
       byte[] buffer = new byte[is.available()];
       is.read(buffer);
    
       File targetFile = new File("openshiftcerts");
       OutputStream outStream = new FileOutputStream(targetFile);
       outStream.write(buffer);
       outStream.close();
       System.setProperty("javax.net.ssl.trustStore", "openshiftcerts");
   }

   @Override
   protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
      return builder.sources(Application.class);
   }
   
   @Bean
   public ServletRegistrationBean camelServletRegistrationBean() {
     ServletRegistrationBean registration = new ServletRegistrationBean(new CamelHttpTransportServlet(), "/camelcxf/*");
     registration.setName("CamelServlet");
     return registration;
   }
}
