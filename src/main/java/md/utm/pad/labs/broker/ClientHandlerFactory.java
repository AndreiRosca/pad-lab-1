package md.utm.pad.labs.broker;

public interface ClientHandlerFactory {

	ClientHandler makeClient(ClientChannel channel);
}
