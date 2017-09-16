package md.utm.pad.labs.broker;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonRequestResponseFactory implements RequestResponseFactory {

	@Override
	public Request makeRequest(String jsonRequest) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(jsonRequest, Request.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String makeResponse(Response response) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(response);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
