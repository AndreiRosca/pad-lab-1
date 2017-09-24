package md.utm.pad.labs.broker;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import md.utm.pad.labs.broker.repository.DataSourceProperties;
import md.utm.pad.labs.broker.repository.JdbcMessageRepository;
import md.utm.pad.labs.broker.service.DefaultJsonService;

public class DefaultClientHandlerFactory implements ClientHandlerFactory {

	private final BrokerContext brokerContext;

	public DefaultClientHandlerFactory() {
		DataSourceProperties dataSourceProperties = createDataSourceProperties();
		brokerContext = new BrokerContext(new JdbcMessageRepository(dataSourceProperties));
	}

	private DataSourceProperties createDataSourceProperties() {
		try {
			return tryCreateDataSourceProperties();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private DataSourceProperties tryCreateDataSourceProperties() throws IOException {
		Properties properties = new Properties();
		properties.load(getInputStream());
		return DataSourceProperties.newBuilder()
				.setDriverClassName(properties.getProperty("jdbc.driverClassName"))
				.setUrl(properties.getProperty("jdbc.url"))
				.setUsername(properties.getProperty("jdbc.username"))
				.setPassword(properties.getProperty("jdbc.password"))
				.build();
	}

	private InputStream getInputStream() {
		return getClass().getResourceAsStream("/dataSource.properties");
	}

	@Override
	public ClientHandler makeClient(ClientChannel channel) {
		return new ClientHandlerImpl(channel, new DefaultJsonService(), brokerContext);
	}
}
