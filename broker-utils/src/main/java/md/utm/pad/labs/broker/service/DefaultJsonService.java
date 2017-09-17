package md.utm.pad.labs.broker.service;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DefaultJsonService implements JsonService {

	@Override
	public <T> String toJson(T obj) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> T fromJson(String json, Class<T> resultingClass) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(json, resultingClass);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
