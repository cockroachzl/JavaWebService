package ch04.dispatch;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import javax.xml.ws.Provider;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.annotation.Resource;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPException;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.http.HTTPBinding;
import java.io.ByteArrayInputStream;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

// The RabbitCounter service implemented as REST style rather than SOAP based.
@WebServiceProvider
@BindingType(value = HTTPBinding.HTTP_BINDING)

public class RabbitCounterProvider implements Provider<Source> {
    @Resource
    protected WebServiceContext ws_ctx;

    // stores previously computed values
    private Map<Integer, Integer> cache = 
       Collections.synchronizedMap(new HashMap<Integer, Integer>());

    private final String xml_start = "<fib:response xmlns:fib = 'urn:fib'>";
    private final String xml_stop = "</fib:response>";
    private final String uri = "urn:fib";

    public Source invoke(Source request) {
        // Filter on the HTTP request verb
        if (ws_ctx == null) throw new RuntimeException("DI failed on ws_ctx.");

        // Grab the message context and extract the request verb.
        MessageContext msg_ctx = ws_ctx.getMessageContext();
        String http_verb = (String) msg_ctx.get(MessageContext.HTTP_REQUEST_METHOD);
        http_verb = http_verb.trim().toUpperCase();

        // Act on the verb.
        if      (http_verb.equals("GET"))    return doGet();
        else if (http_verb.equals("DELETE")) return doDelete();
        else if (http_verb.equals("POST"))   return doPost(request);
        else throw new HTTPException(405);   // bad verb exception
    }

    private Source doPost(Source request) {
        if (request == null) throw new HTTPException(400); // bad request

        String nums = extract_request(request);
        // Extract the integers from a string such as: "[1, 2, 3]"
        nums = nums.replace('[', '\0');
        nums = nums.replace(']', '\0');
        String[ ] parts = nums.split(",");
        List<Integer> list = new ArrayList<Integer>();
        for (String next : parts) {
            int n = Integer.parseInt(next.trim());
            cache.put(n, countRabbits(n));
            list.add(cache.get(n));
        }
        String xml = xml_start + "POSTed: " + list.toString() + xml_stop;
        return make_stream_source(xml);
    }

    private Source doGet() {
        Collection<Integer> list = cache.values();
        String xml = xml_start + "GET: " + list.toString() + xml_stop;
        return make_stream_source(xml);
    }

    private Source doDelete() {
        cache.clear();
        String xml = xml_start + "DELETE: Map cleared." + xml_stop;
        return make_stream_source(xml);
    }

    private String extract_request(Source request) {
        String request_string = null;
        try {
            DOMResult dom_result = new DOMResult();
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.transform(request, dom_result);

            XPathFactory xpf = XPathFactory.newInstance();
            XPath xp = xpf.newXPath();
            xp.setNamespaceContext(new NSResolver("fib", uri));
            request_string = xp.evaluate("/fib:request", dom_result.getNode());
        }
        catch(TransformerConfigurationException e) { System.err.println(e); }
        catch(TransformerException e) { System.err.println(e); }
        catch(XPathExpressionException e) { System.err.println(e); }

        return request_string;
    }

    private StreamSource make_stream_source(String msg) {
        System.out.println(msg);
        ByteArrayInputStream stream = new ByteArrayInputStream(msg.getBytes());
        return new StreamSource(stream);
    }

    private int countRabbits(int n) {
        if (n < 0) throw new HTTPException(403); // forbidden

        // Easy cases.
        if (n < 2) return n;

        // Return cached values if present.
        if (cache.containsKey(n)) return cache.get(n);
        if (cache.containsKey(n - 1) &&
            cache.containsKey(n - 2)) {
          cache.put(n, cache.get(n - 1) + cache.get(n - 2));
          return cache.get(n);
        }

        // Otherwise, compute from scratch, cache, and return.
        int fib = 1, prev = 0;
        for (int i = 2; i <= n; i++) {
            int temp = fib;
            fib += prev;
            prev = temp;
        }
        cache.put(n, fib);
        return fib;
    }
}
