package org.example;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import com.ibm.wdata.WeatherRequest;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class GetBodyProcessor implements Processor  {
    
    public void process(Exchange exchange) throws Exception {
        
        HttpServletRequest servletRequest = exchange.getIn().getBody(HttpServletRequest.class);
        Principal userPrincipal = servletRequest.getUserPrincipal();
        System.out.println("=========>" + userPrincipal.getName());
        System.out.println("=========> class " + userPrincipal.getClass().getName());
        WeatherRequest request = exchange.getIn().getBody(WeatherRequest.class);
        exchange.getIn().setBody(request);  
    }
}

