package md.utm.pad.labs.broker;

import md.utm.pad.labs.broker.executor.RequestExecutor;
import md.utm.pad.labs.broker.executor.RequestExecutorFactory;

public class ClientHandlerImpl implements ClientHandler {

	private final ClientChannel channel;
	private final RequestResponseFactory factory;
	private final BrokerContext brokerContext;
	private RequestExecutorFactory executorFactory;

	public ClientHandlerImpl(ClientChannel channel, RequestResponseFactory factory, BrokerContext brokerContext) {
		this.channel = channel;
		this.factory = factory;
		this.brokerContext = brokerContext;
		this.executorFactory = createRequestExecutorFactory();
	}

	protected RequestExecutorFactory createRequestExecutorFactory() {
		return new RequestExecutorFactory(brokerContext);
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
				continue;
			Request request = factory.makeRequest(jsonRequest);
			RequestExecutor executor = executorFactory.makeExecutor(request);
			Response response = executor.execute();
			if (isCloseResponse(response))
				break;
			channel.write(factory.makeResponse(response));
		} while (true);
	}

	private boolean isCloseResponse(Response response) {
		return response.getType().equals("closeAccepted");
	}

	private String readJsonRequest() {
		StringBuilder requestBuilder = new StringBuilder();
		String line;
		while ((line = channel.readLine()) != null && line.trim().length() > 0) {
			requestBuilder.append(line);
		}
		return requestBuilder.toString();
	}
}
