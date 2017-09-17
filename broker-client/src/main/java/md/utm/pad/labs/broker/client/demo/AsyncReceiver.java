package md.utm.pad.labs.broker.client.demo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import md.utm.pad.labs.broker.Message;
import md.utm.pad.labs.broker.client.Connection;
import md.utm.pad.labs.broker.client.MessageListener;
import md.utm.pad.labs.broker.client.Queue;
import md.utm.pad.labs.broker.client.Session;

public class AsyncReceiver implements MessageListener {
	private final Connection connection;
	private final Session session;

	public AsyncReceiver(String brokerUri) {
		try {
			connection = new Connection(new URI(brokerUri));
			connection.start();
			session = connection.createSession();
			Queue queue = session.createQueue("EM_FUNKY.Q");
			queue.setMessageListener(this);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public void onMessage(Message message) {
		System.out.println("Got message: " + message);
	}
	
	public void close() {
		session.close();
		connection.close();
	}

	public static void main(String[] args) throws IOException {
		AsyncReceiver receiver = new AsyncReceiver("tcp://localhost:9999");
		System.out.println("Press any key to stop the receiver");
		System.in.read();
		receiver.close();
	}
}
