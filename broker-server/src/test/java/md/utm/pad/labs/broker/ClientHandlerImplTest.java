package md.utm.pad.labs.broker;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import md.utm.pad.labs.broker.service.JsonService;

@RunWith(HierarchicalContextRunner.class)
public class ClientHandlerImplTest {
	private static final String sendMessageRequest = "{ 'command': 'send', 'payload': '<payload>' }";
	private static final String closeRequest = "{'command':'close','payload':''}";

	public class BasicClientHandlerTest {
		ClientChannel channel = mock(ClientChannel.class);
		JsonService jsonService = mock(JsonService.class);
		BrokerContext context = mock(BrokerContext.class);

		@Before
		public void setUp() {
			when(channel.readLine()).thenReturn(sendMessageRequest, "", closeRequest, (String) null);
			when(jsonService.fromJson(anyString(), anyObject())).thenReturn(new Request("send", "<payload>", "AAPL.Q"), new Request("close"));
		}

		@Test
		public void canReadJsonRequestFromChannel() {
			ClientHandlerImpl handler = new ClientHandlerImpl(channel, jsonService, context);
			handler.handleClient();
			verify(channel, times(4)).readLine();
			verify(jsonService, times(2)).fromJson(anyString(), anyObject());
		}
	}

	public class ClientHandlerUsesBrokerContext {
		ClientChannel channel = mock(ClientChannel.class);
		JsonService jsonService = mock(JsonService.class);
		BrokerContext context = mock(BrokerContext.class);
		ClientHandlerImpl handler;

		@Before
		public void setUp() {
			when(channel.readLine()).thenReturn(sendMessageRequest, "", closeRequest, (String) null);
			when(jsonService.fromJson(anyString(), anyObject())).thenReturn(new Request("send", "AAPL.Q", "<payload>"), new Request("close", ""));
			when(context.receiveMessage(anyString())).thenReturn(new Message("<payload>"));
			handler = new ClientHandlerImpl(channel, jsonService, context);
			handler.handleClient();
		}

		@Test
		public void canSendMessage() {
			verify(context).sendMessage("AAPL.Q", new Message("<payload>"));
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
