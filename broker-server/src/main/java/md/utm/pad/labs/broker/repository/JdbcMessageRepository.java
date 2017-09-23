package md.utm.pad.labs.broker.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import md.utm.pad.labs.broker.Message;
import md.utm.pad.labs.broker.MessageQueue;

public class JdbcMessageRepository implements MessageRepository {
	private static final String INSERT_MESSAGE_SQL = "insert into broker.messages(message_payload) values (?)";
	private static final String INSERT_MESSAGE_PROPERTIES_SQL = "insert into broker.message_properties(property_name, property_value, message_id) values (?, ?, ?)";
	private static final String DELETE_MESSAGE_PROPERTIES_SQL = "delete from broker.message_properties where message_id = ?";
	private static final String DELETE_MESSAGE_SQL = "delete from broker.messages where id = ?";
	private static final String SELECT_QUEUE_NAMES_SQL = "select queue_name from broker.message_queues";
	private static final String CREATE_QUEUE_SQL = "insert into broker.message_queues(queue_name) values (?)";

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
			rollbackTransaction(connection);
			throw new RuntimeException(e);
		} finally {
			closeConnection(connection);
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

	protected synchronized Connection getConnection() throws SQLException {
		return DriverManager.getConnection(properties.getUrl(), properties.getUsername(), properties.getPassword());
	}

	@Override
	public void delete(long messageId) {
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			delete(connection, DELETE_MESSAGE_PROPERTIES_SQL, messageId);
			delete(connection, DELETE_MESSAGE_SQL, messageId);
			connection.commit();
		} catch (SQLException e) {
			rollbackTransaction(connection);
			throw new RuntimeException(e);
		} finally {
			closeConnection(connection);
		}
	}

	private void rollbackTransaction(Connection connection) {
		if (connection != null)
			try {
				connection.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}

	private void closeConnection(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void delete(Connection connection, String sql, long messageId) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setLong(1, messageId);
			statement.executeUpdate();
		}
	}

	@Override
	public Map<String, MessageQueue> findAllMessageQueues() {
		Map<String, MessageQueue> queues = new HashMap<>();
		Connection connection = null;
		try {
			connection = getConnection();
			connection.setAutoCommit(false);
			try (Statement statement = connection.createStatement()) {
				ResultSet resultSet = statement.executeQuery(SELECT_QUEUE_NAMES_SQL);
				while (resultSet.next()) {
					String queueName = resultSet.getString(1);
					queues.put(queueName, new MessageQueue(queueName));
				}
			}
			connection.commit();
			return queues;
		} catch (SQLException e) {
			rollbackTransaction(connection);
			throw new RuntimeException(e);
		} finally {
			closeConnection(connection);
		}
	}

	@Override
	public void createQueue(MessageQueue queue) {
		Connection connection = null;
		try {
			connection = getConnection();
			try (PreparedStatement statement = connection.prepareStatement(CREATE_QUEUE_SQL)) {
				statement.setString(1, queue.getName());
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			closeConnection(connection);
		}
	}
}
