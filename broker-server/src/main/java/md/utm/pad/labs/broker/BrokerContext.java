package md.utm.pad.labs.broker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BrokerContext {

	private final Map<String, MessageQueue> queues = new ConcurrentHashMap<>();

	public BrokerContext() {
	}

	public void sendMessage(String queueName, Message message) {
		queues.get(queueName).addMessage(message);
	}

	public Message receiveMessage(String queueName) {
		return queues.get(queueName).receiveMessage();
	}

	public void createQueue(String queueName) {
		queues.put(queueName, new MessageQueue(queueName));
	}

	public boolean queueExists(String queueName) {
		return queues.containsKey(queueName);
	}

	public int getQueueDepth(String queueName) {
		return queues.get(queueName).getPendingMessages();
	}
}
