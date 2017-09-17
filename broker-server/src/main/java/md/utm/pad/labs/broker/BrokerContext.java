package md.utm.pad.labs.broker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BrokerContext {
	private static final String DEFAULT_QUEUE_NAME = "__EnterpriseMessaging_DEFAULT.QUEUE__";

	private final Map<String, MessageQueue> queues = new ConcurrentHashMap<>();

	public BrokerContext() {
		createQueue(DEFAULT_QUEUE_NAME);
	}

	public void sendMessage(String queueName, Message message) {
		queues.getOrDefault(queueName, queues.get(DEFAULT_QUEUE_NAME)).addMessage(message);
	}

	public void sendMessage(Message message) {
		sendMessage("", message);
	}

	public Message receiveMessage(String queueName) {
		return queues.getOrDefault(queueName, queues.get(DEFAULT_QUEUE_NAME)).receiveMessage();
	}

	public Message receiveMessage() {
		return receiveMessage("");
	}

	public void createQueue(String queueName) throws InvalidQueueNameException {
		if (queueName == null || queueName.isEmpty())
			throw new InvalidQueueNameException();
		queues.put(queueName, new MessageQueue(queueName));
	}

	public boolean queueExists(String queueName) {
		return queues.containsKey(queueName);
	}

	public int getQueueDepth(String queueName) {
		return queues.getOrDefault(queueName, queues.get(DEFAULT_QUEUE_NAME)).getPendingMessages();
	}

	public int getQueueDepth() {
		return getQueueDepth("");
	}

	public static class InvalidQueueNameException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public InvalidQueueNameException() {
		}

		public InvalidQueueNameException(String message) {
			super(message);
		}
	}
}
