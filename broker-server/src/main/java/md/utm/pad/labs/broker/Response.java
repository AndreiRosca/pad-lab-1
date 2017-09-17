package md.utm.pad.labs.broker;

public class Response {

	protected final String type;
	protected final String payload;

	public Response(String type, String payload) {
		this.type = type;
		this.payload = payload;
	}

	public String getType() {
		return type;
	}

	public String getPayload() {
		return payload;
	}
}
