package md.utm.pad.labs.broker;

public class Request {

	private String command;
	private String payload;

	public Request(String command, String payload) {
		this.command = command;
		this.payload = payload;
	}
	
	protected Request() {
	}

	public String getCommand() {
		return command;
	}

	public String getPayload() {
		return payload;
	}

	@Override
	public String toString() {
		return "Request [command=" + command + ", payload=" + payload + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((command == null) ? 0 : command.hashCode());
		result = prime * result + ((payload == null) ? 0 : payload.hashCode());
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
		Request other = (Request) obj;
		if (command == null) {
			if (other.command != null)
				return false;
		} else if (!command.equals(other.command))
			return false;
		if (payload == null) {
			if (other.payload != null)
				return false;
		} else if (!payload.equals(other.payload))
			return false;
		return true;
	}
}
