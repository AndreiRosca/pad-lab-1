package md.utm.pad.labs.broker.client;

import md.utm.pad.labs.broker.Request;
import md.utm.pad.labs.broker.client.service.JsonService;

public class Session {

	private final Connection connection;
	private final JsonService jsonService;

	public Session(Connection connection, JsonService jsonService) {
		this.connection = connection;
		this.jsonService = jsonService;
	}

	public void createQueue(String queueName) {
		Request request = new Request("createQueue", queueName);
		connection.getClientChannel().write(jsonService.toJson(request));
	}
}
