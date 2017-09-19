package md.utm.pad.labs.broker.client;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;

import md.utm.pad.labs.broker.ClientChannel;
import md.utm.pad.labs.broker.Request;
import md.utm.pad.labs.broker.SocketClientChannel;
import md.utm.pad.labs.broker.service.DefaultJsonService;
import md.utm.pad.labs.broker.service.JsonService;

public class Connection implements AutoCloseable {

	private Socket socket;
	private final URI uri;
	private ClientChannel channel;

	public Connection(URI uri) throws UnknownProtocolException {
		if (!uri.getScheme().equals("tcp"))
			throw new UnknownProtocolException("Expected the 'tcp://' protocol.");
		this.uri = uri;
	}

	ClientChannel getClientChannel() {
		return channel;
	}

	public Session createSession() {
		return new Session(this, getJsonService());
	}

	protected ClientChannel createClientChannel(Socket s) {
		return new SocketClientChannel(s);
	}

	protected JsonService getJsonService() {
		return new DefaultJsonService();
	}

	protected Socket createSocket() throws UnknownHostException, IOException {
		return new Socket(uri.getHost(), uri.getPort());
	}

	public void start() {
		try {
			socket = createSocket();
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
