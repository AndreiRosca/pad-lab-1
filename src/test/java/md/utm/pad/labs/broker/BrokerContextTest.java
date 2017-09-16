package md.utm.pad.labs.broker;

import static org.junit.Assert.*;

import org.junit.Test;

public class BrokerContextTest {

	BrokerContext context = new BrokerContext();

	@Test
	public void canCreateNewQueues() {
		context.createQueue("EM_FUNKY.Q");
		assertTrue(context.queueExists("EM_FUNKY.Q"));
	}

	@Test
	public void canSendMessages() {
		context.createQueue("EM_TEST.Q");
		context.sendMessage("EM_TEST.Q", new Message("Welcome!!!"));
		assertEquals(1, context.getQueueDepth("EM_TEST.Q"));
	}

	@Test
	public void canReceiveMessages() {
		context.createQueue("EM_TEST.Q");
		context.sendMessage("EM_TEST.Q", new Message("Welcome!!!"));
		Message message = context.receiveMessage("EM_TEST.Q");
		assertEquals(new Message("Welcome!!!"), message);
		assertEquals(0, context.getQueueDepth("EM_TEST.Q"));
	}
}
