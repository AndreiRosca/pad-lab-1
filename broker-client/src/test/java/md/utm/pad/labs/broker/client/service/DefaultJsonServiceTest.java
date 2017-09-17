package md.utm.pad.labs.broker.client.service;

import static org.junit.Assert.*;

import org.junit.Test;

import md.utm.pad.labs.broker.Request;

public class DefaultJsonServiceTest {

	private static final String JSON_REQUEST = "{\"command\":\"createQueue\",\"targetQueueName\":\"testQueue\",\"payload\":\"\"}";
	
	DefaultJsonService service = new DefaultJsonService();

	@Test
	public void canConvertObjectToJson() {
		Request request = new Request("createQueue", "testQueue");
		String resultingJson = service.toJson(request);
		assertEquals(JSON_REQUEST, resultingJson);
	}
	
	@Test
	public void canCreateObjectFromJson() {
		Request resultingObject = service.fromJson(JSON_REQUEST, Request.class);
		assertEquals(new Request("createQueue", "testQueue"), resultingObject);
	}
}
