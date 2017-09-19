package md.utm.pad.labs.broker;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import md.utm.pad.labs.broker.subscriber.Subscriber;

public class BrokerContext {
	private static final String DEFAULT_QUEUE_NAME = "__EnterpriseMessaging_DEFAULT.QUEUE__";

	private final Map<String, MessageQueue> queues = new ConcurrentHashMap<>();
	private final Map<String, Set<Subscriber>> subscribers = new ConcurrentHashMap<>();

	public BrokerContext() {
		createQueue(DEFAULT_QUEUE_NAME);
	}

	private void publishMessageToSubscribers(String queueName, Message message) {
		Set<Subscriber> queueSubscribers = subscribers.get(queueName);
		if (queueSubscribers != null) {
			queueSubscribers.forEach(s -> s.consumeMessage(message));
		}
	}

	public void sendMulticastMessage(String queueNamePattern, Message message) {
		Pattern pattern = Pattern.compile(queueNamePattern);
		queues.forEach((queueName, messageQueue) -> {
			Matcher matcher = pattern.matcher(queueName);
			if (matcher.matches()) {
				sendMessage(queueName, message);
			}
		});
	}

	public void sendMessage(String queueName, Message message) {
		queues.getOrDefault(queueName, queues.get(DEFAULT_QUEUE_NAME)).addMessage(message);
		publishMessageToSubscribers(queueName, message);
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
		if (!queues.containsKey(queueName))
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

	public void registerSubscriber(String queueName, Subscriber subscriber) {
		if (!subscribers.containsKey(queueName))
			subscribers.put(queueName, new CopyOnWriteArraySet<>());
		subscribers.get(queueName).add(subscriber);
	}
}
