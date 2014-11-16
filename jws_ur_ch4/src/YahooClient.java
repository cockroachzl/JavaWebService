import java.net.URI;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.Dispatch;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.ws.handler.MessageContext;
import org.w3c.dom.NodeList;
import yahoo.NSResolver;

// A client against the Yahoo RESTful news summary service.
class YahooClient {
    public static void main(String[ ] args) throws Exception {
        if (args.length < 1) {
            System.err.println("YahooClient <your AppID>");
            return;
        }
        String app_id = "appid=" + args[0];
        
        // Create a name for a service port.
        URI ns_URI = new URI("urn:yahoo:yn");
        QName serviceName = new QName("yahoo", ns_URI.toString());
        QName portName = new QName("yahoo_port", ns_URI.toString());

        // Now create a service proxy
        Service s = Service.create(serviceName);

        String qs = app_id + "&type=all&results=10&" +
                    "sort=date&language=en&query=quantum mechanics";

        // Endpoint address
        URI address = new URI("http",                  // HTTP scheme
                              null,                    // user info
                              "api.search.yahoo.com",  // host
                              80,                      // port
                              "/NewsSearchService/V1/newsSearch", // path
                              qs,                      // query string
                              null);                   // fragment

        // Add the appropriate port
        s.addPort(portName, HTTPBinding.HTTP_BINDING, address.toString());

        // From the service, generate a Dispatcher
        Dispatch<Source> d =
            s.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
        Map<String, Object> request_context = d.getRequestContext();
        request_context.put(MessageContext.HTTP_REQUEST_METHOD, "GET");

        // Invoke
        Source result = d.invoke(null);
        DOMResult dom_result = new DOMResult();
        Transformer trans = TransformerFactory.newInstance().newTransformer();
        trans.transform(result, dom_result);

        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();
        xp.setNamespaceContext(new NSResolver("yn", ns_URI.toString()));

        NodeList resultList = (NodeList)
            xp.evaluate("/yn:ResultSet/yn:Result",
                        dom_result.getNode(),
                        XPathConstants.NODESET);

        int len = resultList.getLength();
        for (int i = 1; i <= len; i++) {
            String title1 = 
                xp.evaluate("/yn:ResultSet/yn:Result",
                            dom_result.getNode());
	    String title2 =
		xp.evaluate("/yn:Title", dom_result.getNode());
            System.out.printf("[%d] %s\n", i, title1 + title2);
        }
    }
}
