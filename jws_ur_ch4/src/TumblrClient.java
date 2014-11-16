import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.ByteArrayInputStream;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

class TumblrClient {
    public static void main(String[ ] args) {
        if (args.length < 2) {
            System.err.println("Usage: TumblrClient <email> <passwd>");
            return;
        }
        new TumblrClient().tumble(args[0], args[1]);
    }

    private void tumble(String email, String password) {
        try {
            HttpURLConnection conn = null;

            // GET request.
            String url = "http://mgk-cdm.tumblr.com/api/read";
            conn = get_connection(url, "GET");
            conn.setRequestProperty("accept", "text/xml");
            conn.connect();
            String xml = get_response(conn);
            if (xml.length() > 0) {
                System.out.println("Raw XML:\n" + xml);
                parse(xml, "\nSki photo captions:", "//photo-caption");
            }

            // POST request
            url = "http://www.tumblr.com/api/write";
            conn = get_connection(url, "POST");
            String title = "Summer thoughts up north";
            String body = "Craigieburn Ski Area, NZ";
            String payload =
                URLEncoder.encode("email", "UTF-8") + "=" +
                URLEncoder.encode(email, "UTF-8") + "&" +
                URLEncoder.encode("password", "UTF-8") + "=" +
                URLEncoder.encode(password, "UTF-8") + "&" +
                URLEncoder.encode("type", "UTF-8") + "=" +
                URLEncoder.encode("regular", "UTF-8") + "&" +
                URLEncoder.encode("title", "UTF-8") + "=" +
                URLEncoder.encode(title, "UTF-8") + "&" +
                URLEncoder.encode("body", "UTF-8") + "=" +
                URLEncoder.encode(body, "UTF-8");
            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            out.writeBytes(payload);
            out.flush();
            String response = get_response(conn);
            System.out.println("Confirmation code: " + response);
        }
        catch(IOException e) { System.err.println(e); }
        catch(NullPointerException e) { System.err.println(e); }
    }

    private HttpURLConnection get_connection(String url_s, String verb) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(url_s);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(verb);
            conn.setDoInput(true);
            conn.setDoOutput(true);
        }
        catch(MalformedURLException e) { System.err.println(e); }
        catch(IOException e) { System.err.println(e); }
        return conn;
    }

    private String get_response(HttpURLConnection conn) {
        String xml = "";
        try {
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String next = null;
            while ((next = reader.readLine()) != null) xml += next;
        }
        catch(IOException e) { System.err.println(e); }
        return xml;
    }

    private void parse(String xml, String msg, String pattern) {
        StreamSource source =
            new StreamSource(new ByteArrayInputStream(xml.getBytes()));
        DOMResult dom_result = new DOMResult();
        System.out.println(msg);
        try {
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.transform(source, dom_result);
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xp = xpf.newXPath();
            NodeList list = (NodeList)
                xp.evaluate(pattern, dom_result.getNode(), XPathConstants.NODESET);
            int len = list.getLength();
            for (int i = 0; i < len; i++) {
                Node node = list.item(i);
                if (node != null) 
                  System.out.println(node.getFirstChild().getNodeValue());
            }
        }
        catch(TransformerConfigurationException e) { System.err.println(e); }
        catch(TransformerException e) { System.err.println(e); }
        catch(XPathExpressionException e) { System.err.println(e); }
    }
}
