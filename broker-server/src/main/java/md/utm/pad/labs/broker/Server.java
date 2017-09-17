package md.utm.pad.labs.broker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
	private static final int MAX_THREADS = 5;

	private final ClientHandlerFactory clientHandlerFactory;
	private final ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
	private volatile ServerSocket serverSocket;
	private volatile boolean stopRequested = false;
	private volatile boolean isRunning = false;

	public Server(ClientHandlerFactory clientHandlerFactory) {
		this.clientHandlerFactory = clientHandlerFactory;
	}

	public void start() {
		executorService.submit(this);
	}

	public void run() {
		isRunning = true;
		try {
			startServingClients();
		} catch (IOException e) {
			stop();
			throw new RuntimeException(e);
		}
	}

	private void startServingClients() throws IOException {
		serverSocket = new ServerSocket(9999);
		while (!stopRequested) {
			Socket socket = serverSocket.accept();
			ClientChannel channel = getClientChannel(socket);
			ClientHandler handler = clientHandlerFactory.makeClient(channel);
			serveClientInNewThread(handler);
		}
	}

	protected void serveClientInNewThread(ClientHandler handler) {
		executorService.submit(() -> handler.handleClient());
	}

	protected ClientChannel getClientChannel(Socket socket) {
		return new SocketClientChannel(socket);
	}

	public boolean isRunning() {
		return isRunning && !stopRequested;
	}

	public void stop() {
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

}
