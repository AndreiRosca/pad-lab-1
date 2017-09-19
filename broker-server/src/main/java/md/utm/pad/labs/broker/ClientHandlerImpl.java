package md.utm.pad.labs.broker;

import md.utm.pad.labs.broker.executor.RequestExecutor;
import md.utm.pad.labs.broker.executor.RequestExecutorFactory;
import md.utm.pad.labs.broker.service.JsonService;

public class ClientHandlerImpl implements ClientHandler {

	private final ClientChannel channel;
	private final JsonService jsonService;
	private final BrokerContext brokerContext;
	private RequestExecutorFactory executorFactory;

	public ClientHandlerImpl(ClientChannel channel, JsonService jsonService, BrokerContext brokerContext) {
		this.channel = channel;
		this.jsonService = jsonService;
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
			channel.write(jsonService.toJson(new Response("error", e.getMessage())));
		} finally {
			channel.close();
		}
	}

	private void tryHandleClient() {
		do {
			String jsonRequest = readJsonRequest();
			if (jsonRequest.isEmpty())
				continue;
			Request request = jsonService.fromJson(jsonRequest, Request.class);
			RequestExecutor executor = executorFactory.makeExecutor(request, channel);
			Response response = executor.execute();
			if (isCloseResponse(response))
				break;
			channel.write(jsonService.toJson(response));
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
