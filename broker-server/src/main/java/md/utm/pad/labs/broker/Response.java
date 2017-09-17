package md.utm.pad.labs.broker;

public class Response {

	protected String type;
	protected String payload;

	public Response(String type, String payload) {
		this.type = type;
		this.payload = payload;
	}
	
	protected Response() {
	}

	public String getType() {
		return type;
	}

	public String getPayload() {
		return payload;
	}

	@Override
	public String toString() {
		return "Response [type=" + type + ", payload=" + payload + "]";
	}
}
