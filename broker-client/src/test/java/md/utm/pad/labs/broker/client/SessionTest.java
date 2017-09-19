package md.utm.pad.labs.broker.client;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import md.utm.pad.labs.broker.ClientChannel;
import md.utm.pad.labs.broker.Message;
import md.utm.pad.labs.broker.ReceiveMessageResponse;
import md.utm.pad.labs.broker.Request;
import md.utm.pad.labs.broker.Response;
import md.utm.pad.labs.broker.service.JsonService;

public class SessionTest {
	// private static final String GET_MESSAGE_RESPONSE =
	// "{\"type\":\"response\",\"payload\":\"success\",\"message\":{\"properties\":[],\"payload\":\"Test\"}}";
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
		assertEquals(new Request("createQueue", "AAPL.Q"), jsonService.getConvertedRequest());
		assertEquals("AAPL.Q", queue.getName());
		verify(channel).write(anyString());
	}

	@Test
	public void canSendMessagesToTheDefaultQueue() {
		Message message = session.createMessage("<payload>");
		session.sendMessage(message);
		assertEquals(new Request("send", "", message.getPayload()), jsonService.getConvertedRequest());
		verify(channel).write(anyString());
	}

	@Test
	public void canSendMessagesToExistingQueues() {
		Message message = session.createMessage("<payload>");
		Queue queue = new Queue(session, "AAPL.Q");
		session.sendMessage(queue, message);
		assertEquals(new Request("send", "AAPL.Q", message.getPayload()), jsonService.getConvertedRequest());
		verify(channel, atLeastOnce()).write(anyString());
	}

	@Test
	public void canReceiveMessagesFromTheDefaultQueue() {
		Message message = session.receiveMessage();
		assertEquals(new Request("receive", ""), jsonService.getConvertedRequest());
		assertEquals(new Message("<payload>"), message);
	}

	@Test
	public void canReceiveMessagesFromExistingQueues() {
		Message message = session.receiveMessage(session.createQueue("Amazon.Q"));
		assertEquals(new Request("receive", "Amazon.Q"), jsonService.getConvertedRequest());
		assertEquals(new Message("<payload>"), message);
	}
	
	@Test
	public void canRegisterAQueueAsyncSubscriber() {
		MessageListener messageListener = mock(MessageListener.class);
		session.registerSubscriber("AAPL.Q", messageListener);
		assertEquals(new Request("subscribe", "AAPL.Q"), jsonService.getConvertedRequest());
		verify(channel, atLeastOnce()).write(anyString());
	}
}
