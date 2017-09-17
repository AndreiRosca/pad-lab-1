package md.utm.pad.labs.broker.client;

import md.utm.pad.labs.broker.Message;

public interface MessageListener {

	void onMessage(Message message);
}
