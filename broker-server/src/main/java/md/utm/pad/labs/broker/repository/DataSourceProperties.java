package md.utm.pad.labs.broker.repository;

public class DataSourceProperties {

	private String username;
	private String password;
	private String url;
	private String driverClassName;

	private DataSourceProperties() {
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getUrl() {
		return url;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public static DataSourcePropertiesBuilder newBuilder() {
		return new DataSourcePropertiesBuilder();
	}

	public static class DataSourcePropertiesBuilder {
		private DataSourceProperties properties = new DataSourceProperties();

		public DataSourcePropertiesBuilder setUsername(String username) {
			properties.username = username;
			return this;
		}

		public DataSourcePropertiesBuilder setPassword(String password) {
			properties.password = password;
			return this;
		}

		public DataSourcePropertiesBuilder setUrl(String url) {
			properties.url = url;
			return this;
		}

		public DataSourcePropertiesBuilder setDriverClassName(String driverClassName) {
			properties.driverClassName = driverClassName;
			return this;
		}

		public DataSourceProperties build() {
			return properties;
		}
	}
}
