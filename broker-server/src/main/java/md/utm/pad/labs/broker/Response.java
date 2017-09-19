package md.utm.pad.labs.broker;

public class Response {

	protected String type = "";
	protected String payload = "";

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((payload == null) ? 0 : payload.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Response other = (Response) obj;
		if (payload == null) {
			if (other.payload != null)
				return false;
		} else if (!payload.equals(other.payload))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
