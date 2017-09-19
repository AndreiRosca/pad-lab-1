package md.utm.pad.labs.broker;

import md.utm.pad.labs.broker.service.DefaultJsonService;

public class DefaultClientHandlerFactory implements ClientHandlerFactory {

	private final BrokerContext brokerContext = new BrokerContext();
	
	@Override
	public ClientHandler makeClient(ClientChannel channel) {
		return new ClientHandlerImpl(channel, new DefaultJsonService(), brokerContext);
	}
}
