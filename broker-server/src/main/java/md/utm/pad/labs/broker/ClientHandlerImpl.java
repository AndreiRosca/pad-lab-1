package md.utm.pad.labs.broker;

public class ClientHandlerImpl implements ClientHandler {

	private final ClientChannel channel;
	private final RequestResponseFactory factory;
	private final BrokerContext brokerContext;

	public ClientHandlerImpl(ClientChannel channel, RequestResponseFactory factory, BrokerContext brokerContext) {
		this.channel = channel;
		this.factory = factory;
		this.brokerContext = brokerContext;
	}

	@Override
	public void handleClient() {
		try {
			tryHandleClient();
		} catch (Exception e) {
			channel.write(factory.makeResponse(new Response("error", e.getMessage())));
		}
	}

	private void tryHandleClient() {
		String jsonRequest = readJsonRequest();
		Request request = factory.makeRequest(jsonRequest);
		RequestExecutor executor = makeExecutor(request);
		Response response = executor.execute();
		channel.write(factory.makeResponse(response));
	}

	private RequestExecutor makeExecutor(Request request) {
		if (request.getCommand().equalsIgnoreCase("send"))
			return new SendMessageRequestExecutor(request);
		else if (request.getCommand().equalsIgnoreCase("receive"))
			return new ReceiveMessageRequestExecutor(request);
		throw new InvalidRequestException("Invalid request type. Expected send|receive.");
	}

	private String readJsonRequest() {
		StringBuilder requestBuilder = new StringBuilder();
		String line;
		while ((line = channel.readLine().trim()).length() > 0) {
			requestBuilder.append(line);
		}
		return requestBuilder.toString();
	}

	private abstract class RequestExecutor {
		protected final Request request;

		public RequestExecutor(Request request) {
			this.request = request;
		}

		public abstract Response execute();
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

	public static class InvalidRequestException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public InvalidRequestException() {
		}

		public InvalidRequestException(String message) {
			super(message);
		}
	}
}