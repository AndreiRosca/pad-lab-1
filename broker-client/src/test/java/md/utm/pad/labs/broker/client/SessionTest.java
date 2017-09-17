package md.utm.pad.labs.broker.client;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import md.utm.pad.labs.broker.ClientChannel;
import md.utm.pad.labs.broker.Message;
import md.utm.pad.labs.broker.Request;
import md.utm.pad.labs.broker.client.service.JsonService;

public class SessionTest {

	Connection connection = mock(Connection.class);
	JsonService jsonService = mock(JsonService.class);
	ClientChannel channel = mock(ClientChannel.class);
	Session session;

	@Before
	public void setUp() {
		when(connection.getClientChannel()).thenReturn(channel);
		session = new Session(connection, jsonService);
	}

	@Test
	public void canCreateNewQueues() {
		Queue queue = session.createQueue("EM_TEST.Q");
		assertEquals("EM_TEST.Q", queue.getName());
		verify(jsonService).toJson(new Request("createQueue", "EM_TEST.Q"));
		verify(channel).write(anyString());
	}

	@Test
	public void canSendMessagesToTheDefaultQueue() {
		Message message = session.createMessage("Hello");
		session.sendMessage(message);
		verify(jsonService).toJson(new Request("send", "", "Hello"));
		verify(channel).write(anyString());
	}

	@Test
	public void canSendMessagesToExistingQueues() {
		Message message = session.createMessage("Hello");
		Queue queue = new Queue("EM_TEST.Q");
		session.sendMessage(queue, message);
		verify(jsonService).toJson(new Request("send", queue.getName(), "Hello"));
		verify(channel).write(anyString());
	}
}
