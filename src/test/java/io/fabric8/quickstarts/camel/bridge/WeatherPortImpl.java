/**
 * Please modify this class to meet your needs
 * This class is not complete
 */

package io.fabric8.quickstarts.camel.bridge;

import java.util.logging.Logger;

import com.ibm.wdata.WeatherPortType;
import com.ibm.wdata.WeatherRequest;



@javax.jws.WebService(
                      serviceName = "weatherService",
                      portName = "WeatherPort",
                      targetNamespace = "http://ibm.com/wdata",
                      
                      endpointInterface = "com.ibm.wdata.WeatherPortType")

public class WeatherPortImpl implements WeatherPortType {

    private static final Logger LOG = Logger.getLogger(WeatherPortImpl.class.getName());

    public com.ibm.wdata.WeatherResponse weatherRequest(WeatherRequest weatherRequest) {
        LOG.info("Executing operation weatherRequest");
        
        try {
            com.ibm.wdata.WeatherResponse _return = new com.ibm.wdata.WeatherResponse();
            _return.setZip(weatherRequest.getZipcode());
            _return.setCity("LA");
            _return.setState("CA");
            _return.setHumidity("95%");
            _return.setTemperature("28");
            return _return;
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

}
