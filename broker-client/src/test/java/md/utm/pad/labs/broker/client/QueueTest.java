package md.utm.pad.labs.broker.client;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class QueueTest {

	Session session = mock(Session.class);

	@Test
	public void canGetTheNameOfTheNewlyCreatedQueue() {
		Queue queue = new Queue(session, "EM_TEST.Q");
		assertEquals("EM_TEST.Q", queue.getName());
	}

	@Test
	public void whenRegisteringAListenerThroughAQueue_TheListenerIsSentToSession() {
		Queue queue = new Queue(session, "EM_TEST.Q");
		MessageListener listener = mock(MessageListener.class);
		queue.setMessageListener(listener);
		verify(session).registerSubscriber("EM_TEST.Q", listener);
	}
}
