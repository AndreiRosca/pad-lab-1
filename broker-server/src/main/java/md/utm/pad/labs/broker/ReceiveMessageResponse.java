package md.utm.pad.labs.broker;

public class ReceiveMessageResponse extends Response {

	private Message message;

	public ReceiveMessageResponse(String type, String payload, Message message) {
		super(type, payload);
		this.message = message;
	}

	protected ReceiveMessageResponse() {
	}

	public Message getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "ReceiveMessageResponse [message=" + message + ", type=" + type + ", payload=" + payload + "]";
	}
}
