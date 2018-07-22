package bighead.pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import bighead.datasource.DataSource;
import bighead.util.PropertiesUtil;

/**操作ConnectionPool的帮助类，若有多个数据源对应多个连接池时候，可以维护一个map即可*/
public class ConnectionPoolManager {
	
	private DataSource dataSource = null;
	private ConnectionPool connectionPool = null;
	
	public ConnectionPoolManager(String configurationPath){
		Properties properties = PropertiesUtil.getProperties(configurationPath);
		this.dataSource = new DataSource(properties);
		this.connectionPool = new ConnectionPool(dataSource, properties);
		try {
			connectionPool.createPool();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Connection getConnection(){
		try {
			return this.getConnectionPool().getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void returnConnection(Connection conn){
		this.getConnectionPool().returnConnection(conn);
	}
	
	public DataSource getDataSource() {
		return dataSource;
	}

	public ConnectionPool getConnectionPool() {
		return connectionPool;
	}
}
