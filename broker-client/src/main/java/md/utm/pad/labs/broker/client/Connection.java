package md.utm.pad.labs.broker.client;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;

import md.utm.pad.labs.broker.ClientChannel;
import md.utm.pad.labs.broker.Request;
import md.utm.pad.labs.broker.SocketClientChannel;
import md.utm.pad.labs.broker.client.service.DefaultJsonService;
import md.utm.pad.labs.broker.client.service.JsonService;

public class Connection implements AutoCloseable {

	private Socket socket;
	private final URI uri;
	private ClientChannel channel;

	public Connection(URI uri) {
		this.uri = uri;
	}

	protected ClientChannel createClientChannel(Socket s) {
		return new SocketClientChannel(s);
	}

	ClientChannel getClientChannel() {
		return channel;
	}

	public Session createSession() {
		return new Session(this, getJsonService());
	}

	protected JsonService getJsonService() {
		return new DefaultJsonService();
	}

	public void start() throws UnknownProtocolException {
		try {
			if (!uri.getScheme().equals("tcp"))
				throw new UnknownProtocolException("Expected the 'tcp://' protocol.");
			socket = new Socket(uri.getHost(), uri.getPort());
			channel = createClientChannel(socket);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		channel.write(getJsonService().toJson(new Request("close", "")));
		channel.close();
	}

	public static class UnknownProtocolException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public UnknownProtocolException() {
		}

		public UnknownProtocolException(String message) {
			super(message);
		}
	}
}
