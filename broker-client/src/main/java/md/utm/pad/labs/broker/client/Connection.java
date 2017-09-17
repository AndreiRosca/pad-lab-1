package md.utm.pad.labs.broker.client;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;

import md.utm.pad.labs.broker.ClientChannel;
import md.utm.pad.labs.broker.SocketClientChannel;

public class Connection {

	private Socket socket;
	private final URL url;
	private ClientChannel channel;

	public Connection(URL url) {
		this.url = url;
	}

	protected ClientChannel createClientChannel(Socket s) {
		return new SocketClientChannel(s);
	}

	ClientChannel getClientChannel() {
		return channel;
	}

	public void start() throws UnknownProtocolException {
		try {
			if (!url.getProtocol().equals("tcp"))
				throw new UnknownProtocolException("Expected the 'tcp://' protocol.");
			socket = new Socket(url.getHost(), url.getPort());
			channel = createClientChannel(socket);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
