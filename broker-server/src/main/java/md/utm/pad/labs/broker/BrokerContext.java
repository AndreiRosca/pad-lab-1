package md.utm.pad.labs.broker;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import md.utm.pad.labs.broker.repository.MessageRepository;
import md.utm.pad.labs.broker.subscriber.Subscriber;

public class BrokerContext {
	private static final String DEFAULT_QUEUE_NAME = "__EnterpriseMessaging_DEFAULT.QUEUE__";

	private final Map<String, MessageQueue> queues;
	private final Map<String, Set<Subscriber>> subscribers = new ConcurrentHashMap<>();
	private final MessageRepository messageRepository;

	public BrokerContext(MessageRepository messageRepository) {
		this.messageRepository = messageRepository;
		queues = messageRepository.findAllMessageQueues();
		if (!queueExists(DEFAULT_QUEUE_NAME))
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
		message.setQueueName(queueName);
		queues.get(queueName).addMessage(message);
		publishMessageToSubscribers(queueName, message);
	}

	public void sendMessage(Message message) {
		sendMessage(DEFAULT_QUEUE_NAME, message);
	}

	public void sendDurableMessage(String queueName, Message message) throws NullMessageException, UnknownQueueException {
		if (message == null)
			throw new NullMessageException();
		if (!queueExists(queueName))
			throw new UnknownQueueException();
		message.setQueueName(queueName);
		Message persistedMessage = messageRepository.persist(message);
		sendMessage(queueName, persistedMessage);
	}

	public Message receiveMessage(String queueName) throws UnknownQueueException {
		if (!queueExists(queueName))
			throw new UnknownQueueException();
		return queues.get(queueName).receiveMessage();
	}

	public Message receiveMessage() {
		return receiveMessage(DEFAULT_QUEUE_NAME);
	}

	public void createQueue(String queueName) throws InvalidQueueNameException {
		if (queueName == null || queueName.isEmpty())
			throw new InvalidQueueNameException();
		if (!queueExists(queueName)) {
			MessageQueue queue = new MessageQueue(queueName);
			queues.put(queueName, queue);
			messageRepository.createQueue(queue);
		}
	}

	public boolean queueExists(String queueName) {
		return queues.containsKey(queueName);
	}

	public int getQueueDepth(String queueName) throws UnknownQueueException {
		if (!queueExists(queueName))
			throw new UnknownQueueException();
		return queues.get(queueName).getPendingMessages();
	}

	public int getQueueDepth() {
		return getQueueDepth(DEFAULT_QUEUE_NAME);
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

	public void acknowledgeReceive(long messageId) {
		messageRepository.delete(messageId);
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
