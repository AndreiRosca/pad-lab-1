package md.utm.pad.labs.broker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import md.utm.pad.labs.broker.repository.MessageRepository;
import md.utm.pad.labs.broker.subscriber.Subscriber;

@RunWith(HierarchicalContextRunner.class)
public class BrokerContextTest {

	public class BasicTests {
		MessageRepository repository = mock(MessageRepository.class);
		BrokerContext context = new BrokerContext(repository);

		@Before
		public void setUp() {
			context.createQueue("AAPL.Q");
			context.createQueue("Amazon.Q");
		}

		@Test
		public void canCreateNewQueues() {
			assertTrue(context.queueExists("AAPL.Q"));
			assertTrue(context.queueExists("Amazon.Q"));
		}

		@Test
		public void whenSendingAMessage_QueueDepthIncreases() {
			context.sendMessage("AAPL.Q", new Message("<payload>"));
			assertEquals(1, context.getQueueDepth("AAPL.Q"));
		}

		@Test
		public void canReceiveMessages() {
			context.sendMessage("AAPL.Q", new Message("<payload>"));
			Message message = context.receiveMessage("AAPL.Q");
			assertEquals(new Message("<payload>"), message);
			assertEquals(0, context.getQueueDepth("AAPL.Q"));
		}

		@Test
		public void whenSendingAMessageWithoutSpecifyingTheQueueName_ItGetsSentToTheDefaultQueue() {
			context.sendMessage(new Message("<payload>"));
			assertEquals(1, context.getQueueDepth());
		}

		@Test(expected = BrokerContext.NullMessageException.class)
		public void whenSendingANullMessage_AnExceptionGetsThrown() {
			context.sendMessage(null);
		}

		@Test
		public void whenReceivingAMessageWithoutSpecifyingTheQueueName_ItIsReceivedFromTheDefaultQueue() {
			Message messageToSend = new Message("<payload>");
			context.sendMessage(messageToSend);
			Message message = context.receiveMessage();
			assertEquals(messageToSend, message);
			assertEquals(0, context.getQueueDepth());
		}

		@Test(expected = BrokerContext.InvalidQueueNameException.class)
		public void whenCreatingAQueueWithAnEmptyName_AnExceptionGetsThrown() {
			context.createQueue("");
		}

		@Test(expected = BrokerContext.InvalidQueueNameException.class)
		public void whenCreatingAQueueWithANullName_AnExceptionGetsThrown() {
			context.createQueue(null);
		}

		@Test(expected = BrokerContext.UnknownQueueException.class)
		public void whenSendingAMessageToAnUnexistingQueue_AnExceptionGetsThrown() {
			context.sendMessage("<UnexistingQueue>", new Message("<payload>"));
		}

		@Test(expected = BrokerContext.UnknownQueueException.class)
		public void whenReceivingAMessageFromAnUnexistingQueue_AnExceptionGetsThrown() {
			context.sendMessage(new Message("<payload>"));
			context.receiveMessage("<UnexistingQueue>");
		}

		@Test(expected = BrokerContext.UnknownQueueException.class)
		public void whenGettingTheQueueDepthOfAnUnexistingQueue_AnExceptionGetsThrown() {
			context.getQueueDepth("<UnexistingQueue>");
		}
	}

	public class DurableMessagesTests {
		MessageRepository repository = mock(MessageRepository.class);
		BrokerContext context = new BrokerContext(repository);

		@Before
		public void setUp() {
			context.createQueue("AAPL.Q");
		}

		@Test
		public void whenSendingADurableMessage_ItIsPersistenInTheDatabase() {
			Message message = new Message("<payload>");
			context.sendDurableMessage("AAPL.Q", message);
			verify(repository).persist(message);
		}

		@Test(expected = BrokerContext.UnknownQueueException.class)
		public void whenSendingADurableMessageToAnUnexistingQueue_AnExceptionGetsThrown() {
			context.sendDurableMessage("<UnexistingQueue>", new Message("<payload>"));
		}

		@Test(expected = BrokerContext.NullMessageException.class)
		public void whenSendingANullDurableMessage_AnExceptionGetsThrown() {
			context.sendDurableMessage("AAPL.Q", null);
		}
	}

	public class SubscriptionTests {
		MessageRepository repository = mock(MessageRepository.class);
		BrokerContext context = new BrokerContext(repository);

		@Before
		public void setUp() {
			context.createQueue("AAPL.Q");
			context.createQueue("Amazon.Q");
		}

		@Test
		public void whenSendingMessageToAQueueWithSubscribers_TheSubscribersAreCalled() {
			Subscriber aaplSubscriber = mock(Subscriber.class);
			context.registerSubscriber("AAPL.Q", aaplSubscriber);
			context.sendMessage("AAPL.Q", new Message("<payload>"));
			verify(aaplSubscriber).consumeMessage(new Message("<payload>"));
		}

		@Test
		public void whenSendingMessageToAQueueWithoutSubscribers_UnmatchedSubscribersAreNotInvoked() {
			Subscriber aaplSubscriber = mock(Subscriber.class);
			context.registerSubscriber("AAPL.Q", aaplSubscriber);
			context.sendMessage("Amazon.Q", new Message("<payload>"));
			verify(aaplSubscriber, never()).consumeMessage(anyObject());
		}

		@Test(expected = BrokerContext.NullSubscriberException.class)
		public void whenRegisteringANullSubscriber_AnExceptionGetsThrown() {
			context.registerSubscriber("AAPL.Q", null);
		}

		@Test(expected = BrokerContext.UnknownQueueException.class)
		public void whenRegisteringASubscriberToAnUnregisteredQueue_AnExceptionGetsThrown() {
			Subscriber subscriber = mock(Subscriber.class);
			context.registerSubscriber("<UnregisteredQueue>", subscriber);
		}
	}

	public class MulticastMessagesTests {
		MessageRepository repository = mock(MessageRepository.class);
		BrokerContext context = new BrokerContext(repository);

		@Before
		public void setUp() {
			context.createQueue("AAPL.Q");
			context.createQueue("Amazon.Q");
		}

		@Test
		public void whenSendingAMulticastMessage_UnmatchedSubscribersShouldNotBeCalled() {
			Subscriber aaplSubscriber = mock(Subscriber.class);
			context.registerSubscriber("AAPL.Q", aaplSubscriber);
			context.sendMulticastMessage("Ama.+", new Message("<payload>"));
			verify(aaplSubscriber, never()).consumeMessage(anyObject());
		}

		@Test
		public void whenSendingAMulticastMessage_MatchedSubscribersShouldBeCalled() {
			Subscriber aaplSubscriber = mock(Subscriber.class);
			context.registerSubscriber("AAPL.Q", aaplSubscriber);
			context.sendMulticastMessage("A.+", new Message("<payload>"));
			verify(aaplSubscriber).consumeMessage(new Message("<payload>"));
		}

		@Test(expected = BrokerContext.InvalidQueueNamePatternException.class)
		public void whenSendingAMulticastMessageWithAnInvalidQueueNamePattern_AnExceptionShouldBeThrown() {
			context.sendMulticastMessage("[", new Message("<payload>"));
		}
	}
}
