package md.utm.pad.labs.broker.demo;

import java.io.IOException;

import md.utm.pad.labs.broker.DefaultClientHandlerFactory;
import md.utm.pad.labs.broker.Server;

public class Main {
	public static void main(String[] args) throws IOException {
		Server server = new Server(new DefaultClientHandlerFactory());
		server.start();
		System.out.println("Press any key to stop the server...");
		System.in.read();
		server.stop();
	}
}
