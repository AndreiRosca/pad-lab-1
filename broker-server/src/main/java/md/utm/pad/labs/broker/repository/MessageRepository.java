package md.utm.pad.labs.broker.repository;

import java.util.Map;

import md.utm.pad.labs.broker.Message;
import md.utm.pad.labs.broker.MessageQueue;

public interface MessageRepository {
	Message persist(Message message);
	void delete(long messageId);
	Map<String, MessageQueue> findAllMessageQueues();
	void createQueue(MessageQueue queue);
}
