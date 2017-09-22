package md.utm.pad.labs.broker.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import md.utm.pad.labs.broker.Message;

public class JdbcMessageRepository implements MessageRepository {
	private static final String INSERT_MESSAGE_SQL = "insert into broker.messages(message_payload) values (?)";
	private static final String INSERT_MESSAGE_PROPERTIES_SQL = "insert into broker.message_properties(property_name, property_value, message_id) values (?, ?, ?)";

	private final DataSourceProperties properties;

	public JdbcMessageRepository(DataSourceProperties properties) {
		this.properties = properties;
		try {
			loadJdbcDriver(properties);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void loadJdbcDriver(DataSourceProperties properties) throws ClassNotFoundException {
		Class.forName(properties.getDriverClassName());
	}

	@Override
	public Message persist(Message message) {
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			Message persistedMessage = persistMessage(message, connection);
			persistMessageProperties(message, connection);
			connection.commit();
			return persistedMessage;
		} catch (SQLException e) {
			try {
				if (connection != null)
					connection.rollback();
			} catch (SQLException e1) {
			}
			throw new RuntimeException(e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	private void persistMessageProperties(Message message, Connection connection) {
		try (PreparedStatement statement = connection.prepareStatement(INSERT_MESSAGE_PROPERTIES_SQL)) {
			for (String propertyName : message.getPropertyNames()) {
				String propertyValue = message.getProperty(propertyName);
				statement.setString(1, propertyName);
				statement.setString(2, propertyValue);
				statement.setLong(3, message.getId());
				statement.addBatch();
			}
			statement.executeBatch();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	protected Message persistMessage(Message message, Connection connection) {
		try (PreparedStatement statement = connection.prepareStatement(INSERT_MESSAGE_SQL,
				Statement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, message.getPayload());
			statement.executeUpdate();
			ResultSet resultSet = statement.getGeneratedKeys();
			if (resultSet.next())
				message.setId(resultSet.getLong(1));
			return message;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Connection getConnection() throws SQLException {
		return DriverManager.getConnection(properties.getUrl(), properties.getUsername(), properties.getPassword());
	}

	@Override
	public Message findById(long messageId) {
		// TODO Auto-generated method stub
		return null;
	}
}
