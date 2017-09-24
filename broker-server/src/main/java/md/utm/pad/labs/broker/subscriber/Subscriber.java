package md.utm.pad.labs.broker.subscriber;

import md.utm.pad.labs.broker.ClientChannel;
import md.utm.pad.labs.broker.Message;
import md.utm.pad.labs.broker.ReceiveMessageResponse;
import md.utm.pad.labs.broker.service.DefaultJsonService;
import md.utm.pad.labs.broker.service.JsonService;

public class Subscriber {

	private final ClientChannel channel;
	private final JsonService jsonService;
	private final String queueName;

	public Subscriber(ClientChannel channel, String queueName) {
		this.channel = channel;
		this.queueName = queueName;
		jsonService = createJsonService();
	}

	protected JsonService createJsonService() {
		return new DefaultJsonService();
	}

	public void consumeMessage(Message message) {
		ReceiveMessageResponse response = new ReceiveMessageResponse("subscriptionMessage", queueName, message);
		channel.write(jsonService.toJson(response));
	}

	public String getQueueName() {
		return queueName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		result = prime * result + ((queueName == null) ? 0 : queueName.hashCode());
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
		Subscriber other = (Subscriber) obj;
		if (channel == null) {
			if (other.channel != null)
				return false;
		} else if (!channel.equals(other.channel))
			return false;
		if (queueName == null) {
			if (other.queueName != null)
				return false;
		} else if (!queueName.equals(other.queueName))
			return false;
		return true;
	}
}
