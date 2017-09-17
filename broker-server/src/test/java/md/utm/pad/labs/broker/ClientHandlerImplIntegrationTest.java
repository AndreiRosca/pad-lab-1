package md.utm.pad.labs.broker;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

public class ClientHandlerImplIntegrationTest {
	private static final String sendMessageRequest = "{ \"command\": \"send\", \"payload\": \"<payload>\", "
			+ "\"targetQueueName\": \"EM_TEST.Q\" }";

	private ClientChannel channel = mock(ClientChannel.class);
	private RequestResponseFactory factory = new JsonRequestResponseFactory();
	private BrokerContext context = new BrokerContext();
	private ClientHandlerImpl handler;

	@Before
	public void setUp() {
		when(channel.readLine()).thenReturn(sendMessageRequest, System.lineSeparator());
		handler = new ClientHandlerImpl(channel, factory, context);
		context.createQueue("EM_TEST.Q");
	}

	@Test
	public void test() {
		handler.handleClient();
		verify(channel).write("{\"type\":\"response\",\"payload\":\"success\"}");
	}
}
