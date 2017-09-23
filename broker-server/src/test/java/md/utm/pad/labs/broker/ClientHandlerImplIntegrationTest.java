package md.utm.pad.labs.broker;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import md.utm.pad.labs.broker.repository.MessageRepository;
import md.utm.pad.labs.broker.service.DefaultJsonService;
import md.utm.pad.labs.broker.service.JsonService;

public class ClientHandlerImplIntegrationTest {
	private static final String sendMessageRequest = "{ \"command\": \"send\", \"payload\": \"<payload>\", "
			+ "\"targetQueueName\": \"EM_TEST.Q\" }";
	private static final String sendCloseRequest = "{\"command\":\"close\",\"payload\":\"\"}";

	ClientChannel channel = mock(ClientChannel.class);
	JsonService jsonService = new DefaultJsonService();
	MessageRepository repository = mock(MessageRepository.class);
	BrokerContext context = new BrokerContext(repository);
	ClientHandlerImpl handler;

	@Before
	public void setUp() {
		when(channel.readLine()).thenReturn(sendMessageRequest, "", sendCloseRequest, (String) null);
		handler = new ClientHandlerImpl(channel, jsonService, context);
		context.createQueue("EM_TEST.Q");
	}

	@Test
	public void canHandleClients() {
		handler.handleClient();
		verify(channel).write("{\"type\":\"response\",\"payload\":\"success\"}");
	}
}
