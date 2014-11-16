import java.util.Map;
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
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import ch04.dispatch.NSResolver;

class AmazonClientREST {
    private final static String uri =
        "http://webservices.amazon.com/AWSECommerceService/2005-03-23";

    public static void main(String[ ] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: AmazonClientREST <access key>");
            return;
        }
        new AmazonClientREST().item_search(args[0].trim());
    }

    private void item_search(String access_key) {
        QName service_name = new QName("awsREST", uri);
        QName port = new QName("awsPort", uri);

        String base_url = "http://ecs.amazonaws.com/onca/xml";
        String qs = "?Service=AWSECommerceService&" +
            "Version=2005-03-23&" +
            "Operation=ItemSearch&" +
            "ContentType=text%2Fxml&" +
            "AWSAccessKeyId=" +  access_key + "&" +
            "SearchIndex=Books&" +
            "Keywords=Fibonacci";
        String endpoint = base_url + qs;

        // Now create a service proxy dispatcher.
        Service service = Service.create(service_name);
        service.addPort(port, HTTPBinding.HTTP_BINDING, endpoint);
        Dispatch<Source> dispatch =
            service.createDispatch(port, Source.class, Service.Mode.PAYLOAD);

        // Set HTTP verb.
        Map<String, Object> request_context = dispatch.getRequestContext();
        request_context.put(MessageContext.HTTP_REQUEST_METHOD, "GET");

        Source result = dispatch.invoke(null); // null payload: GET request
        display_result(result);
    }

    private void display_result(Source result) {
        DOMResult dom_result = new DOMResult();
        try {
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.transform(result, dom_result);
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xp = xpf.newXPath();
            xp.setNamespaceContext(new NSResolver("aws", uri));

            NodeList authors = (NodeList)
                xp.evaluate("//aws:ItemAttributes/aws:Author",
                            dom_result.getNode(),
                            XPathConstants.NODESET);

            NodeList titles = (NodeList)
                xp.evaluate("//aws:ItemAttributes/aws:Title",
                            dom_result.getNode(),
                            XPathConstants.NODESET);

            int len = authors.getLength();
            for (int i = 0; i < len; i++) {
                Node author = authors.item(i);
                Node title  = titles.item(i);
                if (author != null && title != null) {
                    String a_name = author.getFirstChild().getNodeValue();
                    String t_name = title.getFirstChild().getNodeValue();
                    System.out.printf("%s: %s\n", a_name, t_name);
                }
            }
        }
        catch(TransformerConfigurationException e) { System.err.println(e); }
        catch(TransformerException e) { System.err.println(e); }
        catch(XPathExpressionException e) { System.err.println(e); }
    }
}      
