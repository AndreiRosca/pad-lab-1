package md.utm.pad.labs.broker.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.BlockingQueue;

import org.junit.Before;
import org.junit.Test;

import md.utm.pad.labs.broker.ClientChannel;
import md.utm.pad.labs.broker.Message;
import md.utm.pad.labs.broker.Request;
import md.utm.pad.labs.broker.Response;

public class SessionTest {
	private static final String SUCCESS_RESPONSE = "{'type':'response', 'payload':'success'}";

	Connection connection = mock(Connection.class);
	JsonServiceSpy jsonService = new JsonServiceSpy();
	ClientChannel channel = mock(ClientChannel.class);
	Session session;

	@Before
	public void setUp() {
		when(connection.getClientChannel()).thenReturn(channel);
		when(channel.readLine()).thenReturn(SUCCESS_RESPONSE, (String) null);
		session = new Session(connection, jsonService) {
			protected BlockingQueue<Response> createPendingResponsesCollection() {
				return jsonService.getPendingResponses();
			}

			protected void createResponseListenerThread() {
			}
		};
	}

	@Test
	public void canCreateNewQueues() {
		Queue queue = session.createQueue("AAPL.Q");
		assertEquals(new Request("createQueue", "AAPL.Q"), jsonService.popConvertedRequest());
		assertEquals("AAPL.Q", queue.getName());
		verify(channel).write(anyString());
	}

	@Test
	public void canSendMessagesToTheDefaultQueue() {
		Message message = session.createMessage("<payload>");
		session.sendMessage(message);
		assertEquals(new Request("send", "", message.getPayload()), jsonService.popConvertedRequest());
		verify(channel).write(anyString());
	}

	@Test
	public void canSendMessagesToExistingQueues() {
		Message message = session.createMessage("<payload>");
		Queue queue = new Queue(session, "AAPL.Q");
		session.sendMessage(queue, message);
		assertEquals(new Request("send", "AAPL.Q", message.getPayload()), jsonService.popConvertedRequest());
		verify(channel, atLeastOnce()).write(anyString());
	}

	@Test
	public void canSendDurableMessagesToExistingQueues() {
		Message message = session.createMessage("<payload>");
		Queue queue = new Queue(session, "AAPL.Q");
		session.sendDurableMessage(queue, message);
		assertEquals(new Request("durableSend", "AAPL.Q", message.getPayload()), jsonService.popConvertedRequest());
		verify(channel, atLeastOnce()).write(anyString());
	}

	@Test
	public void canReceiveMessagesFromTheDefaultQueue() {
		Message message = session.receiveMessage();
		assertEquals(new Request("receive", ""), jsonService.popConvertedRequest());
		assertEquals(new Request("acknowledgeReceive", "", String.valueOf(message.getId())), jsonService.popConvertedRequest());
		assertEquals(new Message("<payload>"), message);
	}

	@Test
	public void canReceiveMessagesFromExistingQueues() {
		Message message = session.receiveMessage(session.createQueue("Amazon.Q"));
		assertEquals(new Request("createQueue", "Amazon.Q"), jsonService.popConvertedRequest());
		assertEquals(new Request("receive", "Amazon.Q"), jsonService.popConvertedRequest());
		assertEquals(new Request("acknowledgeReceive", "", String.valueOf(message.getId())), jsonService.popConvertedRequest());
		assertEquals(new Message("<payload>"), message);
	}

	@Test
	public void canRegisterAQueueAsyncSubscriber() {
		MessageListener messageListener = mock(MessageListener.class);
		session.registerSubscriber("AAPL.Q", messageListener);
		assertEquals(new Request("subscribe", "AAPL.Q"), jsonService.popConvertedRequest());
		verify(channel, atLeastOnce()).write(anyString());
	}
	
	@Test
	public void canBatchRegisterSubscribersToMultipleQueues() {
		MessageListener messageListener = mock(MessageListener.class);
		session.batchSubscribe(messageListener, "AAPL.Q", "Amazon.Q");
		assertEquals(new Request("batchSubscribe", "AAPL.Q, Amazon.Q"), jsonService.popConvertedRequest());
		verify(channel, atLeastOnce()).write(anyString());
	}

	@Test
	public void canSendMessagesWithByteArraysAsPayload() throws UnsupportedEncodingException {
		Message message = session.createMessage("<payload>".getBytes("UTF-8"));
		assertEquals(new Message("PHBheWxvYWQ+"), message);
	}
}
