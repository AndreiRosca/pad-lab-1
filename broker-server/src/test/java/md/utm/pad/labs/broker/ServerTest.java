package md.utm.pad.labs.broker;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ServerTest {

	private final ClientHandler clientHandler = mock(ClientHandler.class);
	private final ClientHandlerFactory factory = mock(ClientHandlerFactory.class);
	private final Server server = new Server(factory) {
		protected void serveClientInNewThread(ClientHandler handler) {
			handler.handleClient();
		}
	};

	public ServerTest() {
		when(factory.makeClient(anyObject())).thenReturn(clientHandler);
	}

	@Before
	public void setUp() {
		server.start();
	}

	@After
	public void tearDown() {
		if (server.isRunning())
			server.stop();
	}

	@Test
	public void serverCanStop() {
		server.stop();
		assertFalse(server.isRunning());
	}

	@Test
	@Ignore
	public void serverCanHandleClients() throws Exception {
		waitForServerToStart();
		Socket s = new Socket("localhost", 9999);
		verify(clientHandler).handleClient();
		s.close();
	}

	private void waitForServerToStart() {
		while (!server.isRunning()) {
		}
	}
}
