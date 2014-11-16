import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

public class ApacheHttpClient {
	
	private static final String URL = "http://localhost:8080/services/customers";

	public static void main(String[] args) throws Exception {

	}
	@Test
	public void get() throws ClientProtocolException, IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(URL + "/1");
		get.addHeader("accept", "application/xml");

		HttpResponse response = client.execute(get);
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Operation failed: "
					+ response.getStatusLine().getStatusCode());
		}

		System.out.println("Content-Type: "
				+ response.getEntity().getContentType().getValue());

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				response.getEntity().getContent()));

		String line = reader.readLine();
		while (line != null) {
			System.out.println(line);
			line = reader.readLine();
		}
		client.getConnectionManager().shutdown();
	}
	@Test
	public void post() throws IllegalStateException, IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(URL);
		StringEntity entity = new StringEntity("<customer id='333'/>");
		entity.setContentType("application/xml");
		post.setEntity(entity);
		HttpClientParams.setRedirecting(post.getParams(), false);
		HttpResponse response = client.execute(post);
		if (response.getStatusLine().getStatusCode() != 201) {
			throw new RuntimeException("Operation failed: "
					+ response.getStatusLine().getStatusCode());
		}

		String location = response.getLastHeader("Location").getValue();

		System.out.println("Object created at: " + location);
		System.out.println("Content-Type: "
				+ response.getEntity().getContentType().getValue());

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				response.getEntity().getContent()));

		String line = reader.readLine();
		while (line != null) {
			System.out.println(line);
			line = reader.readLine();
		}
		client.getConnectionManager().shutdown();
	}
}
