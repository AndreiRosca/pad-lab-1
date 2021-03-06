package md.utm.pad.labs.broker;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Message {

	private Long id;
	private final Map<String, String> properties = new HashMap<>();
	private String payload = "";
	private String queueName = "";

	public Message() {
	}

	public Message(String payload) {
		this.payload = payload;
	}
	
	public Message(String payload, long id, String queueName) {
		this.payload = payload;
		this.id = id;
		this.queueName = queueName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setProperty(String name, String value) {
		properties.put(name, value);
	}

	public String getProperty(String name) {
		return properties.get(name);
	}

	public boolean hasProperty(String name) {
		return properties.containsKey(name);
	}

	public String getPayload() {
		return payload;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public Set<String> getPropertyNames() {
		return Collections.unmodifiableSet(properties.keySet());
	}

	public byte[] decodePayload() throws InvalidPayloadException {
		try {
			return Base64.getDecoder().decode(payload);			
		} catch (IllegalArgumentException e) {
			throw new InvalidPayloadException("Payload is not base64 encoded.", e);
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((payload == null) ? 0 : payload.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
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
		Message other = (Message) obj;
		if (payload == null) {
			if (other.payload != null)
				return false;
		} else if (!payload.equals(other.payload))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Message{properties=" + properties + ", payload=" + payload + "}";
	}
	
	public static class InvalidPayloadException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public InvalidPayloadException(String message, IllegalArgumentException cause) {
			super(message, cause);
		}
	}
}
