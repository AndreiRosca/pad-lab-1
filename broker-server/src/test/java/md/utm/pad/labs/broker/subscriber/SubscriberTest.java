package md.utm.pad.labs.broker.subscriber;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import md.utm.pad.labs.broker.ClientChannel;
import md.utm.pad.labs.broker.Message;
import md.utm.pad.labs.broker.ReceiveMessageResponse;
import md.utm.pad.labs.broker.service.JsonService;

public class SubscriberTest {

	ClientChannel channel = mock(ClientChannel.class);
	JsonService jsonService = mock(JsonService.class);
	Subscriber subscriber;

	@Before
	public void setUp() {
		subscriber = new Subscriber(channel, "AAPL.Q") {
			protected JsonService createJsonService() {
				return jsonService;
			}
		};
	}

	@Test
	public void whenSubscriberConsumesAMessage_ItShouldConvertItToAJsonResponseAndSendItToTheClientViaClientChannel() {
		Message message = new Message("<payload>");
		subscriber.consumeMessage(message);
		verify(jsonService).toJson(new ReceiveMessageResponse("subscriptionMessage", "AAPL.Q", message));
		verify(channel, atLeastOnce()).write(anyString());
	}

	@Test
	public void afterCreatingASubscriber_TheSubscribersQueueNameIsAccesible() {
		assertEquals("AAPL.Q", subscriber.getQueueName());
	}
}
