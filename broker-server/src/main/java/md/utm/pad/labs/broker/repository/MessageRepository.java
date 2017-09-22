package md.utm.pad.labs.broker.repository;

import md.utm.pad.labs.broker.Message;

public interface MessageRepository {
	Message persist(Message message);
	Message findById(long messageId);
}
