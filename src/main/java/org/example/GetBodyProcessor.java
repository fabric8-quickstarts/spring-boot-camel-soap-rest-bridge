package org.example;

import com.ibm.wdata.WeatherRequest;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class GetBodyProcessor implements Processor  {
    
    public void process(Exchange exchange) throws Exception {
        WeatherRequest request = exchange.getIn().getBody(WeatherRequest.class);
        exchange.getIn().setBody(request);  
    }
}

