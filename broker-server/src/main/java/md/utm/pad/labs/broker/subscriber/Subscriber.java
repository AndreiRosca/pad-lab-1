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
		jsonService = new DefaultJsonService();
	}

	public void consumeMessage(Message message) {
		channel.write(jsonService.toJson(new ReceiveMessageResponse("subscriptionMessage", queueName, message)));
	}
}