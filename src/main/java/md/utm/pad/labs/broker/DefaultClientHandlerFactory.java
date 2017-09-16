package md.utm.pad.labs.broker;

public class DefaultClientHandlerFactory implements ClientHandlerFactory {

	@Override
	public ClientHandler makeClient(ClientChannel channel) {
		return new ClientHandlerImpl(channel, new JsonRequestFactory());
	}
}
