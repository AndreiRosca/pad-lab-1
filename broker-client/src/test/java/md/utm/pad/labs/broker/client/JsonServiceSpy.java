package md.utm.pad.labs.broker.client;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import md.utm.pad.labs.broker.Message;
import md.utm.pad.labs.broker.ReceiveMessageResponse;
import md.utm.pad.labs.broker.Request;
import md.utm.pad.labs.broker.Response;
import md.utm.pad.labs.broker.service.JsonService;

public class JsonServiceSpy implements JsonService {

	private final BlockingQueue<Response> pendingResponses = new LinkedBlockingQueue<>();
	private LinkedList<Request> convertedRequests = new LinkedList<>();

	public BlockingQueue<Response> getPendingResponses() {
		return pendingResponses;
	}

	public Request popConvertedRequest() {
		return convertedRequests.removeFirst();
	}

	@Override
	public <T> String toJson(T obj) {
		if (obj instanceof Request) {
			Request request = (Request) obj;
			addResponse(request);
			convertedRequests.add(request);
		}
		return "";
	}

	private void addResponse(Request request) {
		String command = request.getCommand();
		if (command.equals("createQueue")) {
			pendingResponses.add(new Response("response", "success"));
		} else if (command.equals("send")) {
			pendingResponses.add(new Response("response", "success"));
		} else if (command.equals("receive")) {
			Message message = new Message("<payload>");
			message.setId(1L);
			pendingResponses.add(new ReceiveMessageResponse("response", "success", message));
		} else if (command.equals("subscribe")) {
			pendingResponses.add(new Response("response", "success"));
		} else if (command.equals("durableSend")) {
			pendingResponses.add(new Response("response", "success"));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T fromJson(String json, Class<T> resultingClass) {
		return (T) new ReceiveMessageResponse("response", "success", null);
	}
}
