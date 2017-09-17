package md.utm.pad.labs.broker.client;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;

public class Connection {

	private Socket socket;
	private final URL url;

	public Connection(URL url) {
		this.url = url;
	}

	public void start() throws UnknownProtocolException {
		try {
			if (!url.getProtocol().equals("tcp"))
				throw new UnknownProtocolException("Expected the 'tcp://' protocol.");
			socket = new Socket(url.getHost(), url.getPort());
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
