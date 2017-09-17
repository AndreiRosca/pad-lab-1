package md.utm.pad.labs.broker.client;

public class Queue {

	private final String name;
	private final Session session;
	private MessageListener messageListener;

	public Queue(Session session, String name) {
		this.session = session;
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public void setMessageListener(MessageListener messageListener) {
		this.messageListener = messageListener;
		//Todo:
	}
}
