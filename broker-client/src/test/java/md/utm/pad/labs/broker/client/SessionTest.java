package md.utm.pad.labs.broker.client;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import md.utm.pad.labs.broker.ClientChannel;
import md.utm.pad.labs.broker.Request;
import md.utm.pad.labs.broker.client.service.JsonService;

public class SessionTest {

	Connection connection = mock(Connection.class);
	JsonService jsonService = mock(JsonService.class);
	ClientChannel channel = mock(ClientChannel.class);
	
	@Before
	public void setUp() {
		when(connection.getClientChannel()).thenReturn(channel);
	}

	@Test
	public void canCreateNewQueues() {
		Session session = new Session(connection, jsonService);
		session.createQueue("EM_TEST.Q");
		verify(jsonService).toJson(new Request("createQueue", "EM_TEST.Q"));
		verify(channel).write(anyString());
	}
}
