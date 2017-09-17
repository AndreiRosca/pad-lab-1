package md.utm.pad.labs.broker.executor;

import md.utm.pad.labs.broker.BrokerContext;
import md.utm.pad.labs.broker.Message;
import md.utm.pad.labs.broker.ReceiveMessageResponse;
import md.utm.pad.labs.broker.Request;
import md.utm.pad.labs.broker.Response;

public class RequestExecutorFactory {

	private final BrokerContext brokerContext;

	public RequestExecutorFactory(BrokerContext brokerContext) {
		this.brokerContext = brokerContext;
	}

	public RequestExecutor makeExecutor(Request request) throws InvalidRequestException {
		if (request.getCommand().equalsIgnoreCase("send"))
			return new SendMessageRequestExecutor(request);
		else if (request.getCommand().equalsIgnoreCase("receive"))
			return new ReceiveMessageRequestExecutor(request);
		else if (request.getCommand().equalsIgnoreCase("close"))
			return new CloseConnectionRequestExecutor(request);
		else if (request.getCommand().equalsIgnoreCase("createQueue"))
			return new CreateQueueRequestExecutor(request);
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
			brokerContext.createQueue(request.getPayload());
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
