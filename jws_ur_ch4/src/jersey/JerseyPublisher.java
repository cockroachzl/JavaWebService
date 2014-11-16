import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import java.util.Map;
import java.util.HashMap;

class JerseyPublisher {
    public static void main(String[ ] args) {
        final String base_url = "http://localhost:9876/";
        final Map<String, String> config = new HashMap<String, String>();

        config.put("com.sun.jersey.config.property.packages",
                   "msg.resources"); // package with resource classes

        System.out.println("Grizzly starting on port 9876.\n" +
                           "Kill with Control-C.\n");
        try {
            GrizzlyWebContainerFactory.create(base_url, config);
        }
        catch(Exception e) { System.err.println(e); }
    }
}
