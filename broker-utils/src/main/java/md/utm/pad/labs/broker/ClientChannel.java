package md.utm.pad.labs.broker;

public interface ClientChannel {
	void write(String message);
	String readLine();
	void close();
}
