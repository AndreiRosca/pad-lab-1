package md.utm.pad.labs.broker.client;

import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
	private final BlockingQueue<Response> pendingResponses = createPendingResponsesCollection();
	private volatile boolean stopRequested = false;
	private Thread responseListenerThread;

	public Session(Connection connection, JsonService jsonService) {
		this.connection = connection;
		this.jsonService = jsonService;
		createResponseListenerThread();
	}

	protected void createResponseListenerThread() {
		responseListenerThread = new Thread(this);
		responseListenerThread.setDaemon(true);
		responseListenerThread.start();
	}

	protected BlockingQueue<Response> createPendingResponsesCollection() {
		return new ArrayBlockingQueue<>(10);
	}

	public void close() {
		stopRequested = true;
		responseListenerThread.interrupt();
	}

	public void run() {
		while (!stopRequested) {
			String jsonResponse = readResponse();
			if (jsonResponse == null)
				break;
			if (jsonResponse.trim().isEmpty())
				continue;
			ReceiveMessageResponse response = jsonService.fromJson(jsonResponse, ReceiveMessageResponse.class);
			if (responseIsSubscriptionMessage(response)) {
				pushMessageToListeners(response);
			} else {
				putResponseInQueue(response);
			}
		}
	}

	protected boolean responseIsSubscriptionMessage(ReceiveMessageResponse response) {
		return response.getType() != null && response.getType().equalsIgnoreCase("subscriptionMessage");
	}

	private void putResponseInQueue(ReceiveMessageResponse response) {
		try {
			pendingResponses.put(response);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void pushMessageToListeners(ReceiveMessageResponse response) {
		messageListeners.get(response.getPayload()).forEach((listener) -> listener.onMessage(response.getMessage()));
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
		Response response = getResponse();
		return new Queue(this, queueName);
	}

	private Response getResponse() throws ErrorResponseException {
		Response response = takeResponseFromQueue();
		if (isErrorResponse(response))
			throw new ErrorResponseException(response);
		return response;
	}

	public Message createMessage(String payload) {
		return new Message(payload);
	}

	public Message createMessage(byte[] payload) {
		String payloadAsBase64 = Base64.getEncoder().encodeToString(payload);
		return new Message(payloadAsBase64);
	}

	public void sendMessage(Message message) {
		Request request = new Request("send", "", message.getPayload());
		connection.getClientChannel().write(jsonService.toJson(request));
		Response response = getResponse();
	}

	public void sendMessage(Queue queue, Message message) {
		Request request = new Request("send", queue.getName(), message.getPayload());
		connection.getClientChannel().write(jsonService.toJson(request));
		Response response = getResponse();
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
		Message message = response.getMessage();
		if (message.getId() != null) {
			Request ackRequest = new Request("acknowledgeReceive", "", String.valueOf(message.getId()));
			connection.getClientChannel().write(jsonService.toJson(ackRequest));
		}
		return message;
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

	public void sendDurableMessage(Queue queue, Message message) {
		Request request = new Request("durableSend", queue.getName(), message.getPayload());
		connection.getClientChannel().write(jsonService.toJson(request));
		Response response = getResponse();
	}

	public static class ErrorResponseException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public ErrorResponseException(Response response) {
			super("Server returned the error response: " + response.toString());
		}
	}

	public void batchSubscribe(MessageListener messageListener, String...queueNames) {
		StringJoiner joiner = new StringJoiner(", ");
		Arrays.stream(queueNames).forEach(joiner::add);
		Request request = new Request("batchSubscribe", joiner.toString());
		connection.getClientChannel().write(jsonService.toJson(request));
		Response response = getResponse();
	}

	public void batchSubscribeByPattern(MessageListener messageListener, String queueNamePattern) throws InvalidQueueNamePatternException {
		checkQueueNamePattern(queueNamePattern);
		Request request = new Request("patternBatchSubscribe", queueNamePattern);
		connection.getClientChannel().write(jsonService.toJson(request));
		Response response = getResponse();
	}

	private void checkQueueNamePattern(String queueNamePattern) throws InvalidQueueNamePatternException {
		try {
			Pattern pattern = Pattern.compile(queueNamePattern);
		} catch (PatternSyntaxException e) {
			throw new InvalidQueueNamePatternException(e);
		}
	}

	public static class InvalidQueueNamePatternException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public InvalidQueueNamePatternException(PatternSyntaxException cause) {
			super(cause);
		}
	}
}
