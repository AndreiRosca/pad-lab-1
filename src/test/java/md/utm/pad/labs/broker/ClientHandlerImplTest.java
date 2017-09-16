package md.utm.pad.labs.broker;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;

public class ClientHandlerImplTest {
	
	private static final String jsonRequest = "{ \"command\": \"<send|read>\", \"payload\": \"<payload>\" }";
	
	private static ClientChannel channel = mock(ClientChannel.class);
	private static RequestFactory factory = mock(RequestFactory.class);
	
	@BeforeClass
	public static void setUp() {
		when(channel.readLine()).thenReturn(jsonRequest, System.lineSeparator());
	}

	@Test
	public void canReadJsonRequestFromChannel() {
		ClientHandlerImpl handler = new ClientHandlerImpl(channel, factory);
		handler.handleClient();
		verify(channel, times(2)).readLine();
		verify(factory).makeRequest(anyString());
	}
}
