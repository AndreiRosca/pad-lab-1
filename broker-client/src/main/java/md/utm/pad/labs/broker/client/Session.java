package md.utm.pad.labs.broker.client;

import md.utm.pad.labs.broker.ClientChannel;
import md.utm.pad.labs.broker.Message;
import md.utm.pad.labs.broker.ReceiveMessageResponse;
import md.utm.pad.labs.broker.Request;
import md.utm.pad.labs.broker.client.service.JsonService;

public class Session {

	private final Connection connection;
	private final JsonService jsonService;

	public Session(Connection connection, JsonService jsonService) {
		this.connection = connection;
		this.jsonService = jsonService;
	}

	public Queue createQueue(String queueName) {
		Request request = new Request("createQueue", queueName);
		connection.getClientChannel().write(jsonService.toJson(request));
		return new Queue(queueName);
	}

	public Message createMessage(String payload) {
		return new Message(payload);
	}

	public void sendMessage(Message message) {
		Request request = new Request("send", "", message.getPayload());
		connection.getClientChannel().write(jsonService.toJson(request));
	}

	public void sendMessage(Queue queue, Message message) {
		Request request = new Request("send", queue.getName(), message.getPayload());
		connection.getClientChannel().write(jsonService.toJson(request));
	}

	public Message receiveMessage() {
		return receiveMessageFromQueue("");
	}

	private Message receiveMessageFromQueue(String queueName) {
		Request request = new Request("receive", queueName);
		connection.getClientChannel().write(jsonService.toJson(request));
		String jsonResponse = readResponse();
		ReceiveMessageResponse response = jsonService.fromJson(jsonResponse, ReceiveMessageResponse.class);
		return response.getMessage();
	}

	private String readResponse() {
		ClientChannel channel = connection.getClientChannel();
		StringBuilder payload = new StringBuilder();
		String line;
		while ((line = channel.readLine()) != null && line.trim().length() > 0) {
			payload.append(line);
		}
		return payload.toString();
	}

	public Message receiveMessage(Queue queue) {
		return receiveMessageFromQueue(queue.getName());
	}
}
