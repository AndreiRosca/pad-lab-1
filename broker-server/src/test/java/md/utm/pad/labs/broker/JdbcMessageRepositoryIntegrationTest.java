package md.utm.pad.labs.broker;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import md.utm.pad.labs.broker.repository.DataSourceProperties;
import md.utm.pad.labs.broker.repository.JdbcMessageRepository;
import md.utm.pad.labs.broker.repository.MessageRepository;

public class JdbcMessageRepositoryIntegrationTest {

	MessageRepository repository;
	Connection connection = mock(Connection.class);
	PreparedStatement statement = mock(PreparedStatement.class);
	ResultSet resultSet = mock(ResultSet.class);

	@Before
	public void setUp() throws SQLException {
		DataSourceProperties properties = mock(DataSourceProperties.class);
		repository = new JdbcMessageRepository(properties) {
			protected Connection getConnection() throws SQLException {
				return connection;
			}

			protected void loadJdbcDriver(DataSourceProperties properties) throws ClassNotFoundException {
			}
		};
		when(connection.prepareStatement(anyString(), anyInt())).thenReturn(statement);
		when(connection.prepareStatement(anyString())).thenReturn(statement);
		when(statement.getGeneratedKeys()).thenReturn(resultSet);
		when(resultSet.next()).thenReturn(true);
		when(resultSet.getLong(1)).thenReturn(1L);
	}

	@Test
	public void canPersistMessages() throws SQLException {
		Message message = new Message("<payload>");
		message.setProperty("<prop_name>", "<prop_value>");
		repository.persist(message);
		assertNotNull(message.getId());
		verify(connection).commit();
	}
}
