package md.utm.pad.labs.broker;

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
		return DataSourceProperties.newBuilder()
				.setDriverClassName("org.postgresql.Driver")
				.setUrl("jdbc:postgresql://localhost:5432/simple_message_broker")
				.setUsername("postgres")
				.setPassword("admin")
				.build();
	}

	@Override
	public ClientHandler makeClient(ClientChannel channel) {
		return new ClientHandlerImpl(channel, new DefaultJsonService(), brokerContext);
	}
}
