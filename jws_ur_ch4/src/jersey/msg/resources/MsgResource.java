package msg.resources; 

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;

// This is the base path, which can be extended at the method level.
@Path("/")
public class MsgResource {
    private static String msg = "Hello, world!";

    @GET
    @Produces("text/plain")
    public String read() {
        return msg + "\n";
    }

    @GET
    @Produces("text/plain")
    @Path("{extra}")
    public String personalized_read(@PathParam("extra") String cus) {
        return this.msg + ": " + cus + "\n";
    }

    @POST
    @Produces("text/xml")
    public String create(@FormParam("msg") String new_msg ) {
        this.msg = new_msg;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        XMLEncoder enc = new XMLEncoder(stream);
        enc.writeObject(new_msg);
        enc.close();
        return new String(stream.toByteArray()) + "\n";
    }

    @DELETE
    @Produces("text/plain")
    public String delete() {
        this.msg = null;
        return "Message deleted.\n";
    }
}

