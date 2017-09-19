package md.utm.pad.labs.broker.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import md.utm.pad.labs.broker.Message;
import md.utm.pad.labs.broker.ReceiveMessageResponse;
import md.utm.pad.labs.broker.Request;
import md.utm.pad.labs.broker.Response;
import md.utm.pad.labs.broker.service.JsonService;

public class JsonServiceSpy implements JsonService {
	
	private final BlockingQueue<Response> pendingResponses = new LinkedBlockingQueue<>();
	private Request convertedRequest;
	
	public BlockingQueue<Response> getPendingResponses() {
		return pendingResponses;
	}

	public Request getConvertedRequest() {
		return convertedRequest;
	}
	
	@Override
	public <T> String toJson(T obj) {
		if (obj instanceof Request) {
			Request request = (Request) obj;
			addResponse(request);
			convertedRequest = request;
		}
		return "";
	}

	private void addResponse(Request request) {
		if (request.getCommand().equalsIgnoreCase("createQueue")) {
			pendingResponses.add(new Response("response", "success"));
		} else if (request.getCommand().equalsIgnoreCase("send")) {
			pendingResponses.add(new Response("response", "success"));
		} else if (request.getCommand().equalsIgnoreCase("receive")) {
			pendingResponses.add(new ReceiveMessageResponse("response", "success", new Message("<payload>")));
		}
	}

	@Override
	public <T> T fromJson(String json, Class<T> resultingClass) {
		return (T) new ReceiveMessageResponse("response", "success", null);
	}
}
