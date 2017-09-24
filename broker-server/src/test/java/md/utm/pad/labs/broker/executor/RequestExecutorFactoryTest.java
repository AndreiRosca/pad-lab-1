package md.utm.pad.labs.broker.executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import md.utm.pad.labs.broker.BrokerContext;
import md.utm.pad.labs.broker.ClientChannel;
import md.utm.pad.labs.broker.Message;
import md.utm.pad.labs.broker.ReceiveMessageResponse;
import md.utm.pad.labs.broker.Request;
import md.utm.pad.labs.broker.Response;
import md.utm.pad.labs.broker.subscriber.Subscriber;

public class RequestExecutorFactoryTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	BrokerContext brokerContext = mock(BrokerContext.class);
	ClientChannel channel = mock(ClientChannel.class);
	RequestExecutorFactory factory;

	@Before
	public void setUp() {
		factory = new RequestExecutorFactory(brokerContext);
	}

	@Test(expected = RequestExecutorFactory.InvalidRequestException.class)
	public void whenSendingAnUnknownRequestType_AnExceptionGetsThrown() {
		factory.makeExecutor(new Request("<invalidRequest>"), channel);
		expectedException.expectMessage("<invalidRequest>");
	}

	@Test
	public void whenBatchSubscribing_WeShouldRegisterAsSubscribersToEveryReceivedQueue() {
		Response response = makeAndExecuteRequestExecutor(new Request("batchSubscribe", "AAPL.Q, Amazon.Q,IBM.Q"));
		verify(brokerContext).registerSubscriber(eq("AAPL.Q"), anyObject());
		verify(brokerContext).registerSubscriber(eq("Amazon.Q"), anyObject());
		verify(brokerContext).registerSubscriber(eq("IBM.Q"), anyObject());
		assertSuccessfullResponse(response);
	}

	private Response makeAndExecuteRequestExecutor(Request request) {
		RequestExecutor executor = factory.makeExecutor(request, channel);
		return executor.execute();
	}

	@Test
	public void whenReceivingASendMessageRequest_ItIsDispatchedToTheBrokerContext() {
		Response response = makeAndExecuteRequestExecutor(new Request("send", "AAPL.Q", "<payload>"));
		verify(brokerContext).sendMessage("AAPL.Q", new Message("<payload>"));
		assertSuccessfullResponse(response);
	}

	private void assertSuccessfullResponse(Response response) {
		assertEquals(new Response("response", "success"), response);
	}

	@Test
	public void whenReceivingAReceiveMessageRequest_ItIsDispatchedToTheBrokerContext() {
		when(brokerContext.receiveMessage(anyString())).thenReturn(new Message("<payload>"));
		Response response = makeAndExecuteRequestExecutor(new Request("receive", "AAPL.Q"));
		verify(brokerContext).receiveMessage("AAPL.Q");
		assertEquals(new ReceiveMessageResponse("response", "success", new Message("<payload>")), response);
	}

	@Test
	public void whenReceivingACreateQueueRequest_ItIsDispatchedToTheBrokerContext() {
		Response response = makeAndExecuteRequestExecutor(new Request("createQueue", "AAPL.Q"));
		verify(brokerContext).createQueue("AAPL.Q");
		assertSuccessfullResponse(response);
	}

	@Test
	public void whenReceivingASubscribeRequest_ItIsDispatchedToTheBrokerContext() {
		Response response = makeAndExecuteRequestExecutor(new Request("subscribe", "AAPL.Q"));
		verify(brokerContext).registerSubscriber("AAPL.Q", new Subscriber(channel, "AAPL.Q"));
		assertSuccessfullResponse(response);
	}

	@Test
	public void whenReceivingAMulticastRequest_ItIsDispatchedToTheBrokerContext() {
		Response response = makeAndExecuteRequestExecutor(new Request("multicast", "A.+", "<payload>"));
		verify(brokerContext).sendMulticastMessage("A.+", new Message("<payload>"));
		assertSuccessfullResponse(response);
	}

	@Test
	public void whenReceivingADurableSendMessageRequest_ItIsDispatchedToTheBrokerContext() {
		Response response = makeAndExecuteRequestExecutor(new Request("durableSend", "AAPL.Q", "<payload>"));
		verify(brokerContext).sendDurableMessage("AAPL.Q", new Message("<payload>"));
		assertSuccessfullResponse(response);
	}

	@Test
	public void whenReceivingAnAcknowledgeReceiveRequest_ItIsDispatchedToTheBrokerContext() {
		String queueName = "";
		String messageId = "1";
		Response response = makeAndExecuteRequestExecutor(new Request("acknowledgeReceive", queueName, messageId));
		verify(brokerContext).acknowledgeReceive(eq(1L));
		assertNull(response);
	}

	@Test
	public void whenReceivingAnAcknowledgeReceiveRequestWithEmptyPayload_NothingHappens() {
		String queueName = "";
		String messageId = "";
		Response response = makeAndExecuteRequestExecutor(new Request("acknowledgeReceive", queueName, messageId));
		verify(brokerContext, never()).acknowledgeReceive(anyLong());
		assertNull(response);
	}

	@Test
	public void whenReceivingACloseRequest_ItGetsAccepted() {
		Response response = makeAndExecuteRequestExecutor(new Request("close"));
		assertEquals(new Response("closeAccepted", "success"), response);
	}
}
