package md.utm.pad.labs.broker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueue {

	private final String name;
	private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();

	public MessageQueue(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addMessage(Message message) {
		try {
			messageQueue.put(message);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public Message receiveMessage() {
		try {
			return messageQueue.take();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public int getPendingMessages() {
		return messageQueue.size();
	}
}
