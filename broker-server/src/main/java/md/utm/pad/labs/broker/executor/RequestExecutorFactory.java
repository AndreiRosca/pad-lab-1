package md.utm.pad.labs.broker.executor;

import md.utm.pad.labs.broker.BrokerContext;
import md.utm.pad.labs.broker.ClientChannel;
import md.utm.pad.labs.broker.Message;
import md.utm.pad.labs.broker.ReceiveMessageResponse;
import md.utm.pad.labs.broker.Request;
import md.utm.pad.labs.broker.Response;
import md.utm.pad.labs.broker.subscriber.Subscriber;

public class RequestExecutorFactory {

	private final BrokerContext brokerContext;

	public RequestExecutorFactory(BrokerContext brokerContext) {
		this.brokerContext = brokerContext;
	}

	public RequestExecutor makeExecutor(Request request, ClientChannel channel) throws InvalidRequestException {
		if (request.getCommand().equalsIgnoreCase("send"))
			return new SendMessageRequestExecutor(request);
		else if (request.getCommand().equalsIgnoreCase("receive"))
			return new ReceiveMessageRequestExecutor(request);
		else if (request.getCommand().equalsIgnoreCase("close"))
			return new CloseConnectionRequestExecutor(request);
		else if (request.getCommand().equalsIgnoreCase("createQueue"))
			return new CreateQueueRequestExecutor(request);
		else if (request.getCommand().equalsIgnoreCase("subscribe"))
			return new SubscribeToQueueRequestExecutor(request, channel);
		else if (request.getCommand().equalsIgnoreCase("multicast"))
			return new SendMulticastMessage(request);
		throw new InvalidRequestException("Invalid request type. Expected send|receive.");
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

	private class SendMulticastMessage extends RequestExecutor {

		public SendMulticastMessage(Request request) {
			super(request);
		}

		@Override
		public Response execute() {
			brokerContext.sendMulticastMessage(request.getTargetQueueName(), new Message(request.getPayload()));
			return new Response("response", "success");
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
