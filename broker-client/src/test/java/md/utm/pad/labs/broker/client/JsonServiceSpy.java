package md.utm.pad.labs.broker.client;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
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
	private final Map<String, Response> responses = new HashMap<>();

	public JsonServiceSpy() {
		populateResponsesMap();
	}

	private void populateResponsesMap() {
		responses.put("createQueue", new Response("response", "success"));
		responses.put("send", new Response("response", "success"));
		responses.put("receive", new ReceiveMessageResponse("response", "success", new Message("<payload>", 1L, "AAPL.Q")));
		responses.put("subscribe", new Response("response", "success"));
		responses.put("durableSend", new Response("response", "success"));
		responses.put("acknowledgeReceive", null);
		responses.put("batchSubscribe", new Response("response", "success"));
		responses.put("patternBatchSubscribe", new Response("response", "success"));
	}

	public BlockingQueue<Response> getPendingResponses() {
		return pendingResponses;
	}

	public Request popConvertedRequest() {
		try {
			return convertedRequests.removeFirst();
		} catch (Exception e) {
			throw new NoConvertedRequestsException();
		}
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
		Response response = responses.get(command);
		if (!responses.containsKey(command))
			throw new UnknownRequestException(String.format("Unexpected request: %s", request));
		if (commandIsNotAcknowledgeReceive(command))
			pendingResponses.add(response);
	}

	private boolean commandIsNotAcknowledgeReceive(String command) {
		return !command.equals("acknowledgeReceive");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T fromJson(String json, Class<T> resultingClass) {
		return (T) new ReceiveMessageResponse("response", "success", null);
	}

	public static class NoConvertedRequestsException extends RuntimeException {
		private static final long serialVersionUID = 1L;

	}

	public static class UnknownRequestException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public UnknownRequestException(String message) {
			super(message);
		}
	}
}
