package bighead.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import bighead.datasource.DataSource;

/**¶ÁÈëpropertiesÎÄ¼þ*/
public class PropertiesUtil {

	public static Properties getProperties(String path){
		Properties properties = new Properties();
		InputStream inputStream = PropertiesUtil.class.getClassLoader().getResourceAsStream(path);
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}
}
