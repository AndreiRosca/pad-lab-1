package md.utm.pad.labs.broker;

import static org.junit.Assert.*;

import org.junit.Test;

public class JsonRequestFactoryTest {
	
	private static final String jsonRequest = "{ \"command\": \"send\", \"payload\": \"<payload>\" }";

	@Test
	public void canGetRequestObjectFromJson() {
		JsonRequestFactory factory = new JsonRequestFactory();
		Request request = factory.makeRequest(jsonRequest);
		assertEquals(new Request("send", "<payload>"), request);
	}
}
