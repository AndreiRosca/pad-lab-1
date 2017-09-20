package md.utm.pad.labs.broker;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class ServerTest {

	public class ClientHandlerTests {
		ClientHandlerFactory factory = mock(ClientHandlerFactory.class);
		ClientChannel clientChannel = mock(ClientChannel.class);
		ClientHandler clientHandler = mock(ClientHandler.class);
		ExecutorService executorService = new NonThreadedExecutorServiceStub();
		Server server;
		boolean clientHandlerCalled = false;

		@Before
		public void setUp() {
			when(factory.makeClient(anyObject())).thenReturn(clientHandler);
			ServerSocket serverSocket = mock(ServerSocket.class);
			setUpServer(serverSocket);
			setUpServerSocketMock(serverSocket);
			setUpClientHandlerMock();
		}

		protected void setUpServer(ServerSocket serverSocket) {
			server = new Server(factory) {
				protected ExecutorService createExecutorService() {
					return executorService;
				}

				protected ServerSocket createServerSocket() throws IOException {
					return serverSocket;
				}

				protected ClientChannel getClientChannel(Socket socket) {
					return clientChannel;
				}
			};
		}

		protected void setUpClientHandlerMock() {
			doAnswer(new Answer<Void>() {
				public Void answer(InvocationOnMock invocation) throws Throwable {
					server.stopRequested = true;
					clientHandlerCalled = true;
					return null;
				}
			}).when(clientHandler).handleClient();
		}

		protected void setUpServerSocketMock(ServerSocket serverSocket) {
			Socket socket = mock(Socket.class);
			try {
				when(serverSocket.accept()).thenReturn(socket);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Test
		public void afterStartingTheServerAndAClienyConnects_AClientHandlerIsCreatedAndIsInvokedInAThread() {
			server.start();
			assertTrue(clientHandlerCalled);
		}
	}

	public class BasicTests {

		ClientHandlerFactory factory = mock(ClientHandlerFactory.class);
		ExecutorService executorService = mock(ExecutorService.class);
		Server server;

		@Before
		public void setUp() {
			server = new Server(factory) {
				protected ExecutorService createExecutorService() {
					return executorService;
				}
			};
		}

		@Test(expected = Server.IllegalServerStateException.class)
		public void whenTryingToStopTheServerWithoutStartingItBeforehand_AnExceptionIsThrown() {
			server.stop();
		}

		@Test
		public void afterStoppingTheServer_ItsExecutorServiceShouldShutDown() {
			server.start();
			server.stop();
			assertTrue(server.stopRequested);
			verify(executorService).shutdownNow();
		}
	}
}
