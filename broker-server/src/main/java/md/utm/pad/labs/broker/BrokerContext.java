package md.utm.pad.labs.broker;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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

	public void sendMulticastMessage(String queueNamePattern, Message message) throws InvalidQueueNamePatternException {
		try {
			Pattern pattern = Pattern.compile(queueNamePattern);
			sendMessageToMatchedQueues(pattern, message);
		} catch (PatternSyntaxException e) {
			throw new InvalidQueueNamePatternException(e);
		}
	}

	private void sendMessageToMatchedQueues(Pattern pattern, Message message) {
		queues.forEach((queueName, messageQueue) -> {
			Matcher matcher = pattern.matcher(queueName);
			if (matcher.matches()) {
				sendMessage(queueName, message);
			}
		});
	}

	public void sendMessage(String queueName, Message message) throws NullMessageException, UnknownQueueException {
		if (message == null)
			throw new NullMessageException();
		if (!queueExists(queueName))
			throw new UnknownQueueException();
		queues.getOrDefault(queueName, queues.get(DEFAULT_QUEUE_NAME)).addMessage(message);
		publishMessageToSubscribers(queueName, message);
	}

	public void sendMessage(Message message) {
		sendMessage(DEFAULT_QUEUE_NAME, message);
	}

	public Message receiveMessage(String queueName) throws UnknownQueueException {
		if (!queueExists(queueName))
			throw new UnknownQueueException();
		return queues.getOrDefault(queueName, queues.get(DEFAULT_QUEUE_NAME)).receiveMessage();
	}

	public Message receiveMessage() {
		return receiveMessage(DEFAULT_QUEUE_NAME);
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

	public void registerSubscriber(String queueName, Subscriber subscriber)
			throws NullSubscriberException, UnknownQueueException {
		if (subscriber == null)
			throw new NullSubscriberException();
		if (!queueExists(queueName))
			throw new UnknownQueueException();
		if (!subscribers.containsKey(queueName))
			subscribers.put(queueName, new CopyOnWriteArraySet<>());
		subscribers.get(queueName).add(subscriber);
	}

	public static class InvalidQueueNameException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public InvalidQueueNameException() {
		}

		public InvalidQueueNameException(String message) {
			super(message);
		}
	}

	public static class InvalidQueueNamePatternException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public InvalidQueueNamePatternException() {
		}

		public InvalidQueueNamePatternException(Exception e) {
			super(e);
		}

		public InvalidQueueNamePatternException(String message) {
			super(message);
		}
	}

	public static class NullMessageException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public NullMessageException() {
		}

		public NullMessageException(Exception e) {
			super(e);
		}

		public NullMessageException(String message) {
			super(message);
		}
	}

	public static class NullSubscriberException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public NullSubscriberException() {
		}

		public NullSubscriberException(Exception e) {
			super(e);
		}

		public NullSubscriberException(String message) {
			super(message);
		}
	}

	public static class UnknownQueueException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public UnknownQueueException() {
		}

		public UnknownQueueException(Exception e) {
			super(e);
		}

		public UnknownQueueException(String message) {
			super(message);
		}
	}
}
