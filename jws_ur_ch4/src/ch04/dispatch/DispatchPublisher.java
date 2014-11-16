package ch04.dispatch;

import javax.xml.ws.Endpoint;

class DispatchPublisher {
    public static void main(String[ ] args) {
	int port = 8888;
	String url = "http://localhost:" + port + "/rc";
	System.out.println("Restfully publishing: " + url);
	Endpoint.publish(url, new RabbitCounterProvider());
    }
}
