package md.utm.pad.labs.broker.executor;

import md.utm.pad.labs.broker.Request;
import md.utm.pad.labs.broker.Response;

public abstract class RequestExecutor {
	protected final Request request;

	public RequestExecutor(Request request) {
		this.request = request;
	}

	public abstract Response execute();
}
