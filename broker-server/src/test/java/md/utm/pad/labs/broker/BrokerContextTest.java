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
		final String queueName = "EM_TEST.Q";
		context.createQueue(queueName);
		context.sendMessage(queueName, new Message("Welcome!!!"));
		Message message = context.receiveMessage(queueName);
		assertEquals(new Message("Welcome!!!"), message);
		assertEquals(0, context.getQueueDepth(queueName));
	}

	@Test
	public void canSendMessagesToDefaultQueue() {
		context.sendMessage(new Message("Welcome!!!"));
		assertEquals(1, context.getQueueDepth());
	}

	@Test
	public void canReceiveMessageFromDefaultQuue() {
		Message messageToSend = new Message("Welcome!!!");
		context.sendMessage(messageToSend);
		Message message = context.receiveMessage();
		assertEquals(messageToSend, message);
		assertEquals(0, context.getQueueDepth());
	}

	@Test(expected = BrokerContext.InvalidQueueNameException.class)
	public void whenCreatingQueueWithInvalidNameAnExceptionGetsThrown() {
		context.createQueue("");
	}
}
