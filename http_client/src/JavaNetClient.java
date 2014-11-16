

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Test;

public class JavaNetClient {

	private static final String URL = "http://localhost:8080/services/customers";
	@Test
	public void get() throws IOException {
		URL url = new URL(URL + "/1");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept", "application/xml");

		if (connection.getResponseCode() != 200) {
		  throw new RuntimeException("Operation failed: "
		                              + connection.getResponseCode());
		}

		System.out.println("Content-Type: " + connection.getContentType());

		BufferedReader reader = new BufferedReader(new
		              InputStreamReader(connection.getInputStream()));

		String line = reader.readLine();
		while (line != null) {
		   System.out.println(line);
		   line = reader.readLine();
		}
		connection.disconnect();
	}
	
	@Test
	public void post() throws IOException {
		URL url = new URL(URL);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setInstanceFollowRedirects(false);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/xml");
		OutputStream os = connection.getOutputStream();
		os.write("<customer id='333'/>".getBytes());
		os.flush();
		if (connection.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
		   throw new RuntimeException("Failed to create customer");
		}
		System.out.println("Location: " + connection.getHeaderField("Location"));
		connection.disconnect();
	}
}
