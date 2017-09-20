package md.utm.pad.labs.broker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
	private static final int MAX_THREADS = 5;

	private final ClientHandlerFactory clientHandlerFactory;
	private final ExecutorService executorService = createExecutorService();
	private volatile ServerSocket serverSocket;
	protected volatile boolean stopRequested = false;
	protected volatile boolean serverStarted = false;

	public Server(ClientHandlerFactory clientHandlerFactory) {
		this.clientHandlerFactory = clientHandlerFactory;
	}

	protected ExecutorService createExecutorService() {
		return Executors.newFixedThreadPool(MAX_THREADS);
	}

	public void start() {
		executorService.submit(this);
		serverStarted = true;
	}

	public void run() {
		try {
			startServingClients();
		} catch (IOException e) {
			stop();
			throw new RuntimeException(e);
		}
	}

	private void startServingClients() throws IOException {
		serverSocket = createServerSocket();
		while (!stopRequested) {
			Socket socket = serverSocket.accept();
			ClientChannel channel = getClientChannel(socket);
			ClientHandler handler = clientHandlerFactory.makeClient(channel);
			serveClientInNewThread(handler);
		}
	}

	protected ServerSocket createServerSocket() throws IOException {
		return new ServerSocket(9999);
	}

	protected void serveClientInNewThread(ClientHandler handler) {
		executorService.submit(() -> handler.handleClient());
	}

	protected ClientChannel getClientChannel(Socket socket) {
		return new SocketClientChannel(socket);
	}

	public void stop() throws IllegalServerStateException {
		if (!serverStarted)
			throw new IllegalServerStateException("Must start the server before trying to stop it.");
		stopRequested = true;
		executorService.shutdownNow();
		closeServerSocketIfNeeded();
	}

	private void closeServerSocketIfNeeded() {
		try {
			if (serverSocket != null && !serverSocket.isClosed())
				serverSocket.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static class IllegalServerStateException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public IllegalServerStateException(String message) {
			super(message);
		}
	}
}
