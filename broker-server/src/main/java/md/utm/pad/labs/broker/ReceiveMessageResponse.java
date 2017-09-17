package md.utm.pad.labs.broker;

public class ReceiveMessageResponse extends Response {

	private final Message message;

	public ReceiveMessageResponse(String type, String payload, Message message) {
		super(type, payload);
		this.message = message;
	}

	public Message getMessage() {
		return message;
	}
}
