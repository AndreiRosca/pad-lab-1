package md.utm.pad.labs.broker;

public interface RequestResponseFactory {

	Request makeRequest(String jsonRequest);
	String makeResponse(Response response);

}
