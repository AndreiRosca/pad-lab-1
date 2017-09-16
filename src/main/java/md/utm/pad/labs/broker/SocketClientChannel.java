package md.utm.pad.labs.broker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class SocketClientChannel implements ClientChannel {

	private final Socket socket;
	private final BufferedWriter writer;
	private final BufferedReader reader;

	public SocketClientChannel(Socket socket) {
		this.socket = socket;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void write(String message) {
		try {
			writer.write(message);
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String readLine() {
		try {
			return reader.readLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		try {
			tryClose();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void tryClose() throws IOException {
		reader.close();
		writer.close();
		socket.close();
	}

}
