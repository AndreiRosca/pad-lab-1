package md.utm.pad.labs.broker.service;

public interface JsonService {

	<T> String toJson(T obj);
	<T> T fromJson(String json, Class<T> resultingClass);
}
