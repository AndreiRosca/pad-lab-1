package md.utm.pad.labs.broker.client;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.junit.Test;

import md.utm.pad.labs.broker.ClientChannel;
import md.utm.pad.labs.broker.Request;
import md.utm.pad.labs.broker.service.JsonService;

public class ConnectionTest {

	@Test(expected = Connection.UnknownProtocolException.class)
	public void whenCreatingAConnectionWithAnyProtocolThatsDifferentFromTcp_AnExceptionGestThrown()
			throws URISyntaxException {
		new Connection(new URI("http://localhost:9999"));
	}

	@Test
	public void whenClosingTheConnection_ACloseRequestIsSentToTheServerAndTheClientChannelIsClosed()
			throws URISyntaxException {
		ClientChannel channel = mock(ClientChannel.class);
		Socket socket = mock(Socket.class);
		JsonService jsonService = mock(JsonService.class);
		Connection connection = new Connection(new URI("tcp://localhost:9999")) {
			protected ClientChannel createClientChannel(Socket s) {
				return channel;
			}

			protected Socket createSocket() throws UnknownHostException, IOException {
				return socket;
			}

			protected JsonService getJsonService() {
				return jsonService;
			}
		};
		connection.start();
		connection.close();
		verify(jsonService).toJson(new Request("close"));
		verify(channel, atLeastOnce()).write(anyString());
	}
}
