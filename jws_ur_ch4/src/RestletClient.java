import org.restlet.Client;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

class RestletClient {
    public static void main(String[ ] args) {
        new RestletClient().send_requests();
    }

    private void send_requests() {
        try {
            // Setup the request.
            Request request = new Request();
            request.setResourceRef("http://localhost:7777/fib");

            // To begin, a POST to create some service data.
            List<Integer> nums = new ArrayList<Integer>();
            for (int i = 0; i < 12; i++) nums.add(i);
            
            Form http_form = new Form();
            http_form.add("nums", nums.toString());
            request.setMethod(Method.POST);
            request.setEntity(http_form.getWebRepresentation());

            // Generate a client and make the call.
            Client client = new Client(Protocol.HTTP);

            // POST request
            Response response = get_response(client, request);
            dump(response);

            // GET request to confirm POST
            request.setMethod(Method.GET);
            request.setEntity(null);
            response = get_response(client, request);
            dump(response);

            // DELETE request
            request.setMethod(Method.DELETE);
            request.setEntity(null);
            response = get_response(client, request);
            dump(response);

            // GET request to confirm DELETE
            request.setMethod(Method.GET);
            request.setEntity(null);
            response = get_response(client, request);
            dump(response);
        }
        catch(Exception e) { System.err.println(e); }
    }

    private Response get_response(Client client, Request request) {
        return client.handle(request);
    }

    private void dump(Response response) {
        try {
            if (response.getStatus().isSuccess())
                response.getEntity().write(System.out);
            else
                System.err.println(response.getStatus().getDescription());
        }
        catch(IOException e) { System.err.println(e); }
    }
}
