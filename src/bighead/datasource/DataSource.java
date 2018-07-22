
package bighead.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import bighead.util.PropertiesUtil;

/**数据源类*/
public class DataSource {

	private String driverClassName;
	private String url;
	private String username;
	private String password;
	
	public DataSource(Properties properties){
		driverClassName = properties.getProperty("jdbc.driverClassName");
		url = properties.getProperty("jdbc.url");
		username = properties.getProperty("jdbc.username");
		password = properties.getProperty("jdbc.password");
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public String getUrl() {
		return url;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
}