package md.utm.pad.labs.broker;

public class ClientHandlerImpl implements ClientHandler {

	private final ClientChannel channel;
	private final RequestResponseFactory factory;
	private final BrokerContext brokerContext;
	private boolean closeRequested = false;

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
			e.printStackTrace(System.out);
			channel.write(factory.makeResponse(new Response("error", e.getMessage())));
		} finally {
			channel.close();
		}
	}

	private void tryHandleClient() {
		do {
			String jsonRequest = readJsonRequest();
			if (jsonRequest.isEmpty())
				break;
			Request request = factory.makeRequest(jsonRequest);
			RequestExecutor executor = makeExecutor(request);
			Response response = executor.execute();
			System.out.println("Got request: " + request + "; Sending response:" + response);
			if (response.getType().equals("closeAccepted"))
				break;
			channel.write(factory.makeResponse(response));
		} while (!closeRequested);
	}

	private RequestExecutor makeExecutor(Request request) {
		if (request.getCommand().equalsIgnoreCase("send"))
			return new SendMessageRequestExecutor(request);
		else if (request.getCommand().equalsIgnoreCase("receive"))
			return new ReceiveMessageRequestExecutor(request);
		else if (request.getCommand().equalsIgnoreCase("close"))
			return new CloseConnectionRequestExecutor(request);
		throw new InvalidRequestException("Invalid request type. Expected send|receive.");
	}

	private String readJsonRequest() {
		StringBuilder requestBuilder = new StringBuilder();
		String line;
		while ((line = channel.readLine()) != null && line.trim().length() > 0) {
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

	private class CloseConnectionRequestExecutor extends RequestExecutor {
		public CloseConnectionRequestExecutor(Request request) {
			super(request);
		}

		@Override
		public Response execute() {
			closeRequested = true;
			return new Response("closeAccepted", "success");
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
