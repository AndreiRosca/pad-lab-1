package md.utm.pad.labs.broker.client;

public class Queue {

	private final String name;
	private final Session session;

	public Queue(Session session, String name) {
		this.session = session;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setMessageListener(MessageListener messageListener) {
		session.registerSubscriber(name, messageListener);
	}
}
