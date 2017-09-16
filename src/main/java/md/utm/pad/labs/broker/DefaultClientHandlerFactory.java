package md.utm.pad.labs.broker;

public class DefaultClientHandlerFactory implements ClientHandlerFactory {

	private final BrokerContext brokerContext = new BrokerContext();
	
	@Override
	public ClientHandler makeClient(ClientChannel channel) {
		return new ClientHandlerImpl(channel, new JsonRequestResponseFactory(), brokerContext);
	}
}
