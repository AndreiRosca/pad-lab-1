package md.utm.pad.labs.broker;

import static org.junit.Assert.*;

import org.junit.Test;

public class JsonRequestResponseFactoryTest {
	
	private static final String jsonRequest = "{ \"command\": \"send\", \"payload\": \"<payload>\", " +
			"\"targetQueueName\": \"EM_TEST.Q\" }";

	JsonRequestResponseFactory factory = new JsonRequestResponseFactory();

	@Test
	public void canGetRequestObjectFromJson() {
		Request request = factory.makeRequest(jsonRequest);
		assertEquals(new Request("send", "EM_TEST.Q", "<payload>"), request);
	}
	
	@Test
	public void canGenerateJsonResponseFromSuccessfullResponse() {
		String jsonResponse = factory.makeResponse(new Response("response", "success"));
		assertEquals("{\"type\":\"response\",\"payload\":\"success\"}", jsonResponse);
	}
	
	@Test
	public void canGenerateJsonResponseFromReceiveMessageRequest() {
		String jsonResponse = factory.makeResponse(new ReceiveMessageResponse("response", "success", new Message("<funky>")));
		assertEquals("{\"type\":\"response\",\"payload\":\"success\",\"message\":{\"payload\":\"<funky>\",\"propertyNames\":[]}}", jsonResponse);
	}
}
