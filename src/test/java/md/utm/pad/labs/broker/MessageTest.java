package md.utm.pad.labs.broker;

import static org.junit.Assert.*;

import org.junit.Test;

public class MessageTest {

	private Message message = new Message();

	@Test
	public void canSetAndGetProperties() {
		message.setProperty("age", "22");
		assertTrue(message.hasProperty("age"));
		assertEquals("22", message.getProperty("age"));
	}

	@Test
	public void newlyCreatedMessageHasNoProperties() {
		assertTrue(message.getPropertyNames().isEmpty());
	}

	@Test
	public void newlyCreatedMessageHasEmptyPayload() {
		assertEquals("", message.getPayload());
	}
}
