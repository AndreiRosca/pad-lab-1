package md.utm.pad.labs.broker.client;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import md.utm.pad.labs.broker.ClientChannel;
import md.utm.pad.labs.broker.Message;
import md.utm.pad.labs.broker.ReceiveMessageResponse;
import md.utm.pad.labs.broker.Request;
import md.utm.pad.labs.broker.Response;
import md.utm.pad.labs.broker.service.JsonService;

public class Session implements Runnable, AutoCloseable {

	private final Connection connection;
	private final JsonService jsonService;
	private final Map<String, Set<MessageListener>> messageListeners = new ConcurrentHashMap<>();
	private final BlockingQueue<Response> pendingResponses = new ArrayBlockingQueue<>(10);
	private volatile boolean stopRequested = false;
	private final Thread responseListenerThread;

	public Session(Connection connection, JsonService jsonService) {
		this.connection = connection;
		this.jsonService = jsonService;
		responseListenerThread = new Thread(this);
		responseListenerThread.start();
	}

	public void close() {
		stopRequested = true;
		responseListenerThread.interrupt();
	}

	public void run() {
		while (!stopRequested) {
			String jsonResponse = readResponse();
			if (jsonResponse != null && jsonResponse.trim().isEmpty())
				continue;
			ReceiveMessageResponse response = jsonService.fromJson(jsonResponse, ReceiveMessageResponse.class);
			if (response.getType().equalsIgnoreCase("subscriptionMessage")) {
				messageListeners.get(response.getPayload()).forEach((listener) -> listener.onMessage(response.getMessage()));
			} else {
				try {
					pendingResponses.put(response);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Response takeResponseFromQueue() {
		try {
			return pendingResponses.take();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public Queue createQueue(String queueName) {
		Request request = new Request("createQueue", queueName);
		connection.getClientChannel().write(jsonService.toJson(request));
		Response response = takeResponseFromQueue();
		if (isErrorResponse(response))
			;// throw something
		return new Queue(this, queueName);
	}

	public Message createMessage(String payload) {
		return new Message(payload);
	}

	public void sendMessage(Message message) {
		Request request = new Request("send", "", message.getPayload());
		connection.getClientChannel().write(jsonService.toJson(request));
		Response response = takeResponseFromQueue();
		if (isErrorResponse(response))
			;// throw something
	}

	public void sendMessage(Queue queue, Message message) {
		Request request = new Request("send", queue.getName(), message.getPayload());
		connection.getClientChannel().write(jsonService.toJson(request));
		Response response = takeResponseFromQueue();
		if (isErrorResponse(response))
			;// throw something
	}

	private boolean isErrorResponse(Response response) {
		return !response.getPayload().equalsIgnoreCase("success");
	}

	public Message receiveMessage() {
		return receiveMessageFromQueue("");
	}

	private Message receiveMessageFromQueue(String queueName) {
		Request request = new Request("receive", queueName);
		connection.getClientChannel().write(jsonService.toJson(request));
		ReceiveMessageResponse response = (ReceiveMessageResponse) takeResponseFromQueue();
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

	public void registerSubscriber(String queueName, MessageListener messageListener) {
		Request request = new Request("subscribe", queueName);
		connection.getClientChannel().write(jsonService.toJson(request));
		Response response = takeResponseFromQueue();
		if (response.getPayload().equalsIgnoreCase("success"))
			addMessageListener(queueName, messageListener);
	}

	private void addMessageListener(String queueName, MessageListener listener) {
		if (!messageListeners.containsKey(queueName)) {
			messageListeners.put(queueName, new CopyOnWriteArraySet<>());
		}
		messageListeners.get(queueName).add(listener);
	}
}