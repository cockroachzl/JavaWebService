import java.util.Map;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.ByteArrayInputStream;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.Dispatch;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.ws.handler.MessageContext;
import ch04.dispatch.NSResolver;

// Dispatch client against the SOAP-based TimeServer service
class DispatchClientTS {
    public static void main(String[ ] args) throws Exception {
        new DispatchClientTS().send_and_receive_SOAP();
    }

    private void send_and_receive_SOAP() {
        // Create identifying names for service and port.
        URI ns_URI = null;
        try {
            ns_URI = new URI("http://ts.ch01/");      // from WSDL
        }
        catch(URISyntaxException e) { System.err.println(e); }

        QName service_name = new QName("tns", ns_URI.toString());
        QName port = new QName("tsPort", ns_URI.toString());
        String endpoint = "http://localhost:9876/ts"; // from WSDL

        // Now create a service proxy or dispatcher.
        Service service = Service.create(service_name);
        service.addPort(port, HTTPBinding.HTTP_BINDING, endpoint);
        Dispatch<Source> dispatch =
            service.createDispatch(port, Source.class, Service.Mode.PAYLOAD);

        // Send a request.
        String soap_request =
            "<?xml version='1.0' encoding='UTF-8'?> " +
            "<soap:Envelope " +
               "soap:encodingStyle='http://schemas.xmlsoap.org/soap/encoding/' " +
               "xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/' " +
               "xmlns:soapenc='http://schemas.xmlsoap.org/soap/encoding/' " +
               "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +
               "xmlns:tns='http://ts.ch01/' " +
               "xmlns:xsd='http://www.w3.org/2001/XMLSchema'> " +
            "<soap:Body>" +
            "<tns:getTimeAsElapsed xsi:nil='true'/>" +
            "</soap:Body>" +
            "</soap:Envelope>";

        Map<String, Object> request_context = dispatch.getRequestContext();
        request_context.put(MessageContext.HTTP_REQUEST_METHOD, "POST");
        StreamSource source = make_stream_source(soap_request);
        Source result = dispatch.invoke(source);
        display_result(result, ns_URI.toString());
    }

    private void display_result(Source result, String uri) {
        DOMResult dom_result = new DOMResult();
        try {
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.transform(result, dom_result);

            XPathFactory xpf = XPathFactory.newInstance();
            XPath xp = xpf.newXPath();
            xp.setNamespaceContext(new NSResolver("tns", uri));
            String result_string =
               xp.evaluate("//time_response", dom_result.getNode());
            System.out.println(result_string);
        }
        catch(TransformerConfigurationException e) { System.err.println(e); }
        catch(TransformerException e) { System.err.println(e); }
        catch(XPathExpressionException e) { System.err.println(e); }
    }

    private StreamSource make_stream_source(String msg) {
        ByteArrayInputStream stream = new ByteArrayInputStream(msg.getBytes());
        return new StreamSource(stream);
    }
}
