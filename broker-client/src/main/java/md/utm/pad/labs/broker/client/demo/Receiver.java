package md.utm.pad.labs.broker.client.demo;

import java.net.URI;
import java.net.URISyntaxException;

import md.utm.pad.labs.broker.Message;
import md.utm.pad.labs.broker.client.Connection;
import md.utm.pad.labs.broker.client.Queue;
import md.utm.pad.labs.broker.client.Session;

public class Receiver {
	public static void main(String[] args) {
		try (Connection connection = new Connection(new URI("tcp://localhost:9999"))) {
			connection.start();
			try (Session session = connection.createSession();) {
				Queue queue = session.createQueue("EM_FUNKY.Q");
				Message message = session.receiveMessage(queue);
				System.out.println("Received message: " + message);
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
