package md.utm.pad.labs.broker;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonRequestFactory implements RequestFactory {

	@Override
	public Request makeRequest(String jsonRequest) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(jsonRequest, Request.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
