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
		return "ReceiveMessageResponse{message=" + message + ", type=" + type + ", payload=" + payload + "}";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReceiveMessageResponse other = (ReceiveMessageResponse) obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return true;
	}
}
