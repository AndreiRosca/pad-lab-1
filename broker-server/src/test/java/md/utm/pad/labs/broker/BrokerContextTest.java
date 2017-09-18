package md.utm.pad.labs.broker;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import md.utm.pad.labs.broker.service.JsonService;
import md.utm.pad.labs.broker.subscriber.Subscriber;

@RunWith(HierarchicalContextRunner.class)
public class BrokerContextTest {

	public class BasicTests {
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

	public class SubscriptionTests {
		BrokerContext context = new BrokerContext();
		JsonService jsonService = mock(JsonService.class);
		ClientChannel channel = mock(ClientChannel.class);

		@Test
		public void whenSendingMessageToAQueueWithListeners_ListenersAreNotified() {
			String queueName = "EM_FUNKY.Q";
			Subscriber subscriber = new Subscriber(channel, queueName) {
				protected JsonService createJsonService() {
					return jsonService;
				}
			};
			context.createQueue(queueName);
			context.registerSubscriber(queueName, subscriber);
			context.sendMessage(queueName, new Message("Hello"));
			verify(channel).write(anyString());
		}

		@Test
		public void whenSendingMessageToAQueueWithoutListeners_ListenersOfOtherQueuesAreNotInvoked() {
			String subscriptionQueue = "EM_SUBSCRIBERS.Q";
			String queueWithoutSubscribers = "EM_NO_SUBSCRIBERS";
			Subscriber subscriber = new Subscriber(channel, subscriptionQueue) {
				protected JsonService createJsonService() {
					return jsonService;
				}
			};
			context.createQueue(subscriptionQueue);
			context.createQueue(queueWithoutSubscribers);
			context.registerSubscriber(subscriptionQueue, subscriber);
			context.sendMessage(queueWithoutSubscribers, new Message("Hello"));
			verify(channel, never()).write(anyString());
		}
	}
}
