
package com.ibm.wdata;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.ibm.wdata package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _WeatherRequest_QNAME = new QName("http://ibm.com/wdata", "WeatherRequest");
    private final static QName _WeatherResponse_QNAME = new QName("http://ibm.com/wdata", "WeatherResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.ibm.wdata
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link WeatherRequest }
     * 
     */
    public WeatherRequest createWeatherRequest() {
        return new WeatherRequest();
    }

    /**
     * Create an instance of {@link WeatherResponse }
     * 
     */
    public WeatherResponse createWeatherResponse() {
        return new WeatherResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WeatherRequest }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link WeatherRequest }{@code >}
     */
    @XmlElementDecl(namespace = "http://ibm.com/wdata", name = "WeatherRequest")
    public JAXBElement<WeatherRequest> createWeatherRequest(WeatherRequest value) {
        return new JAXBElement<WeatherRequest>(_WeatherRequest_QNAME, WeatherRequest.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link WeatherResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link WeatherResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://ibm.com/wdata", name = "WeatherResponse")
    public JAXBElement<WeatherResponse> createWeatherResponse(WeatherResponse value) {
        return new JAXBElement<WeatherResponse>(_WeatherResponse_QNAME, WeatherResponse.class, null, value);
    }

}
