package md.utm.pad.labs.broker;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class ClientHandlerImplTest {

	public class BasicClientHandlerTest {
		private static final String jsonRequest = "{ \"command\": \"send\", \"payload\": \"<payload>\" }";
		private static final String sendCloseRequest = "{\"command\":\"close\",\"payload\":\"\"}";

		private ClientChannel channel = mock(ClientChannel.class);
		private RequestResponseFactory factory = mock(RequestResponseFactory.class);
		private BrokerContext context = mock(BrokerContext.class);

		@Before
		public void setUp() {
			when(channel.readLine()).thenReturn(jsonRequest, "", sendCloseRequest, (String) null);
			when(factory.makeRequest(anyString())).thenReturn(new Request("send", "<payload>", "EM_TEST.Q"), new Request("close", ""));
		}

		@Test
		public void canReadJsonRequestFromChannel() {
			ClientHandlerImpl handler = new ClientHandlerImpl(channel, factory, context);
			handler.handleClient();
			verify(channel, times(4)).readLine();
			verify(factory, times(2)).makeRequest(anyString());
		}
	}

	public class ClientHandlerUsesBrokerContext {
		private static final String sendMessageRequest = "{ \"command\": \"send\", \"payload\": \"<payload>\", "
				+ "\"targetQueueName\": \"EM_TEST.Q\" }";
		private static final String sendCloseRequest = "{\"command\":\"close\",\"payload\":\"\"}";

		private ClientChannel channel = mock(ClientChannel.class);
		private RequestResponseFactory factory = mock(RequestResponseFactory.class);
		private BrokerContext context = mock(BrokerContext.class);
		private ClientHandlerImpl handler;

		@Before
		public void setUp() {
			when(channel.readLine()).thenReturn(sendMessageRequest, "", sendCloseRequest, (String) null);
			when(factory.makeRequest(anyString())).thenReturn(new Request("send", "EM_TEST.Q", "<payload>"), new Request("close", ""));
			when(context.receiveMessage(anyString())).thenReturn(new Message("<payload>"));
			handler = new ClientHandlerImpl(channel, factory, context);
			handler.handleClient();
		}

		@Test
		public void canSendMessage() {
			verify(context).sendMessage("EM_TEST.Q", new Message("<payload>"));
			verify(channel).write(anyString());
		}

		@Test
		public void canReceiveMessage() {
			Message message = context.receiveMessage("EM_TEST.Q");
			assertEquals(new Message("<payload>"), message);
			verify(channel).write(anyString());
		}
	}
}
