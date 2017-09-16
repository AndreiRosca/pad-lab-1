package md.utm.pad.labs.broker;

public class ClientHandlerImpl implements ClientHandler {

	private final ClientChannel channel;
	private final RequestFactory factory;

	public ClientHandlerImpl(ClientChannel channel, RequestFactory factory) {
		this.channel = channel;
		this.factory = factory;
	}

	@Override
	public void handleClient() {
		String jsonRequest = readJsonRequest();
		Request request = factory.makeRequest(jsonRequest);
	}

	private String readJsonRequest() {
		StringBuilder requestBuilder = new StringBuilder();
		String line;
		while ((line = channel.readLine().trim()).length() > 0) {
			requestBuilder.append(line);
		}
		return requestBuilder.toString();
	}
}
