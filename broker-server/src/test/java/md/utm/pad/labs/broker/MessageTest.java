package md.utm.pad.labs.broker;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

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

	@Test
	public void canGetMessagePayloadAsByteArray() throws UnsupportedEncodingException {
		message = new Message("PHBheWxvYWQ+");
		byte[] decodedPayload = message.decodePayload();
		assertArrayEquals("<payload>".getBytes("UTF-8"), decodedPayload);
	}

	@Test(expected = Message.InvalidPayloadException.class)
	public void whenTryingGettingTheUnencodedMessagePayloadAsByteArray_AnExceptionGetsThrown() {
		message = new Message("q");
		message.decodePayload();
	}
}
