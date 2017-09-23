package md.utm.pad.labs.broker.executor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import md.utm.pad.labs.broker.BrokerContext;
import md.utm.pad.labs.broker.ClientChannel;
import md.utm.pad.labs.broker.Message;
import md.utm.pad.labs.broker.ReceiveMessageResponse;
import md.utm.pad.labs.broker.Request;
import md.utm.pad.labs.broker.Response;
import md.utm.pad.labs.broker.subscriber.Subscriber;

public class RequestExecutorFactory {

	private final Map<String, BiFunction<Request, ClientChannel, RequestExecutor>> executors = new ConcurrentHashMap<>();
	private final BrokerContext brokerContext;

	public RequestExecutorFactory(BrokerContext brokerContext) {
		this.brokerContext = brokerContext;
		setUpExecutors();
	}

	private void setUpExecutors() {
		executors.put("send", (request, channel) -> new SendMessageRequestExecutor(request));
		executors.put("receive", (request, channel) -> new ReceiveMessageRequestExecutor(request));
		executors.put("close", (request, channel) -> new CloseConnectionRequestExecutor(request));
		executors.put("createQueue", (request, channel) -> new CreateQueueRequestExecutor(request));
		executors.put("subscribe", (request, channel) -> new SubscribeToQueueRequestExecutor(request, channel));
		executors.put("multicast", (request, channel) -> new SendMulticastMessageRequestExecutor(request));
		executors.put("durableSend", (request, channel) -> new SendDurableMessageRequestExecutor(request));
		executors.put("acknowledgeReceive", (request, channel) -> new AcknowledgeReceiveRequestExecutor(request));
	}

	public RequestExecutor makeExecutor(Request request, ClientChannel channel) throws InvalidRequestException {
		String command = request.getCommand().toLowerCase();
		BiFunction<Request, ClientChannel, RequestExecutor> executor = executors.get(command);
		if (executor == null)
			throw new InvalidRequestException("Invalid request type.");
		return executor.apply(request, channel);
	}

	private class SendMessageRequestExecutor extends RequestExecutor {
		public SendMessageRequestExecutor(Request request) {
			super(request);
		}

		@Override
		public Response execute() {
			brokerContext.sendMessage(request.getTargetQueueName(), new Message(request.getPayload()));
			return new Response("response", "success");
		}
	}

	private class SendDurableMessageRequestExecutor extends RequestExecutor {
		public SendDurableMessageRequestExecutor(Request request) {
			super(request);
		}

		@Override
		public Response execute() {
			brokerContext.sendDurableMessage(request.getTargetQueueName(), new Message(request.getPayload()));
			return new Response("response", "success");
		}
	}

	private class ReceiveMessageRequestExecutor extends RequestExecutor {
		public ReceiveMessageRequestExecutor(Request request) {
			super(request);
		}

		@Override
		public Response execute() {
			Message message = brokerContext.receiveMessage(request.getTargetQueueName());
			return new ReceiveMessageResponse("response", "success", message);
		}
	}

	private class CloseConnectionRequestExecutor extends RequestExecutor {
		public CloseConnectionRequestExecutor(Request request) {
			super(request);
		}

		@Override
		public Response execute() {
			return new Response("closeAccepted", "success");
		}
	}

	private class CreateQueueRequestExecutor extends RequestExecutor {
		public CreateQueueRequestExecutor(Request request) {
			super(request);
		}

		@Override
		public Response execute() {
			brokerContext.createQueue(request.getTargetQueueName());
			return new Response("response", "success");
		}
	}

	private class SubscribeToQueueRequestExecutor extends RequestExecutor {
		private final ClientChannel channel;

		public SubscribeToQueueRequestExecutor(Request request, ClientChannel channel) {
			super(request);
			this.channel = channel;
		}

		@Override
		public Response execute() {
			brokerContext.registerSubscriber(request.getTargetQueueName(),
					new Subscriber(channel, request.getTargetQueueName()));
			return new Response("response", "success");
		}
	}

	private class SendMulticastMessageRequestExecutor extends RequestExecutor {

		public SendMulticastMessageRequestExecutor(Request request) {
			super(request);
		}

		@Override
		public Response execute() {
			brokerContext.sendMulticastMessage(request.getTargetQueueName(), new Message(request.getPayload()));
			return new Response("response", "success");
		}
	}

	private class AcknowledgeReceiveRequestExecutor extends RequestExecutor {

		public AcknowledgeReceiveRequestExecutor(Request request) {
			super(request);
		}

		@Override
		public Response execute() {
			long messageId = Long.valueOf(request.getPayload());
			brokerContext.acknowledgeReceive(messageId);
			return null;
		}
	}

	public static class InvalidRequestException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public InvalidRequestException() {
		}

		public InvalidRequestException(String message) {
			super(message);
		}
	}
}
