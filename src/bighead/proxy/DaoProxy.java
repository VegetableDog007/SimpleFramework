package bighead.proxy;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bighead.annotation.Sql;
import bighead.cache.SqlCache;
import bighead.exception.InsertException;
import bighead.exception.returnTypeMismatchedException;
import bighead.pool.ConnectionPool;
import bighead.util.ClassUtil;
import bighead.util.ConnectionUtil;
import bighead.util.JdbcUtil;

/**代理注解为Dao的类*/
public class DaoProxy implements InvocationHandler {

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		//检查注解
		if(method.isAnnotationPresent(Sql.class)){
			Sql annotation = method.getAnnotation(Sql.class);
			String sql = annotation.value().trim();
			boolean isSelect = "select".equalsIgnoreCase(sql.split(" ")[0]);
			if(isSelect){
				//处理查询
				return handleSelect(sql, method, args);
			}else{
				//处理增删改操作
				handleUpdate(sql, method, args);
			}

		}
		return null;
	}

	/***********************************************************************/
	private Integer handleUpdate(String sql, Method method, Object[] args)
			throws SQLException, InsertException {
		// 以后从pool获取
				// Connection connection = ConnectionPool.getConnection();
		Connection connection = (Connection) ConnectionUtil.getConnection();
		// 普通insert是指直接插入对应的VALUE(?,?,?) 带#号开头的是插入对象的情况
				/** 约定#insert是插入一个对象,而且对象的属性都是基本类型 */
		if ("@insert".equalsIgnoreCase(sql.split("\\s+")[0])) {
			// 非法参数
			if (args.length > 1) {
				throw new InsertException(
						"#insert : more than one object param");
			}
			// 构造真正的Sql
			String actualSql = sql.substring(1);
			String tableName = actualSql.split("\\s+")[2];
			int tableColumnNum = this.getColumnNum(tableName);
			if (tableColumnNum > 0) {
				StringBuilder sb = new StringBuilder(" VALUES(");
				sb.append("?");
				for (int cur = 1; cur < tableColumnNum; cur++) {
					sb.append(",?");
				}
				sb.append(")");
				actualSql = actualSql + sb.toString();
			} else {
				throw new InsertException("#insert : 0 columns in " + tableName);
			}

			PreparedStatement prepareStatement = connection
					.prepareStatement(actualSql);

			// 获取record中所有的Field, 并且建立一个Map<String, Object>
			Object record = args[0];
			Field[] fields = record.getClass().getDeclaredFields();
			
			//构建对象的字段表
			Map<String, Object> fieldMap = getFieldMap(fields, record);

			// 根据fieldMap设置prepareStatement中的参数
			setInsertParam(prepareStatement, fieldMap, tableName);
			System.out.println(prepareStatement.toString());
			Integer updatedRows = prepareStatement.executeUpdate();
			System.out.println("update:" + updatedRows);

			/**
			 * 使插入的表对应的缓存失效
			 * */
			updateCache(prepareStatement.toString().split(":")[1]);
			return updatedRows;
		} else if ("#insert".equalsIgnoreCase(sql.split("\\s+")[0])) {
			String actualSql = sql.substring(1);
			PreparedStatement prepareStatement = connection
					.prepareStatement(actualSql);
			if (args.length > 1) {
				throw new InsertException(
						"#insert : more than one object param");
			}
			// 获取record中所有的Field, 并且建立一个Map<String, Object>
			Object record = args[0];
			Field[] fields = record.getClass().getDeclaredFields();
			
			//构建对象的字段表
			Map<String, Object> fieldMap = getFieldMap(fields, record);
			
			// 根据fieldMap设置prepareStatement中的参数
			String tableName = actualSql.split("\\s+")[2];
			setInsertParam(prepareStatement, fieldMap, tableName);
			System.out.println(prepareStatement.toString());
			Integer updatedRows = prepareStatement.executeUpdate();
			System.out.println("update:" + updatedRows);
			/**
			 * 使插入的表对应的缓存失效
			 * */
			updateCache(prepareStatement.toString().split(":")[1]);
			return updatedRows;
		} else {
			//普通正常sql增删改
			// 约定sql开头不能有空格
			PreparedStatement prepareStatement = connection
					.prepareStatement(sql);
			JdbcUtil.setParam(prepareStatement, method, args);
			System.out.println(prepareStatement.toString());
			Integer updatedRows = prepareStatement.executeUpdate();
			System.out.println("update:" + updatedRows);

			/**
			 * 使插入的表对应的缓存失效
			 * */
			updateCache(prepareStatement.toString().split(":")[1]);
			return updatedRows;
		}
	}

	/**增删改要更新缓存*/
	private void updateCache(String statement) {
		SqlCache sqlCache = SqlCache.getInstance();
		System.out.println(sqlCache);
		sqlCache.update(statement);
	}
	
	/**简历obj对象的field表*/
	private Map<String, Object> getFieldMap(Field[] fields, Object obj) throws InsertException {
		Map<String, Object> fieldMap = new HashMap<String, Object>();
		for (Field field : fields) {
			// 设置为可获取
			field.setAccessible(true);
			String fieldName = field.getName();
			Object fieldValue = null;
			// 字段名
			System.out.print(fieldName + ":");
			if (field.getType().getName()
					.equals(java.lang.String.class.getName())) {
				// String type
				try {
					fieldValue = field.get(obj);
					if(fieldValue!=null)
						System.out.println("fieldValueClass:"+fieldValue.getClass()+" "+"fieldValue:"+fieldValue);
					else
						System.out.println("fieldValue:null");
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (field.getType().getName()
					.equals(java.lang.Integer.class.getName())
					|| field.getType().getName().equals("int")) {
				// Integer type
				try {
					fieldValue = field.get(obj);
					if(fieldValue!=null)
						System.out.println("fieldValueClass:"+fieldValue.getClass()+" "+"fieldValue:"+fieldValue);
					else
						System.out.println("fieldValue:null");
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (field.getType().getName()
					.equals(java.lang.Short.class.getName())
					|| field.getType().getName().equals("short")) {
				// Short type
				try {
					fieldValue = field.get(obj);
					if(fieldValue!=null)
						System.out.println("fieldValueClass:"+fieldValue.getClass()+" "+"fieldValue:"+fieldValue);
					else
						System.out.println("fieldValue:null");
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (field.getType().getName()
					.equals(java.lang.Long.class.getName())
					|| field.getType().getName().equals("long")) {
				// Long type
				try {
					fieldValue = field.get(obj);
					if(fieldValue!=null)
						System.out.println("fieldValueClass:"+fieldValue.getClass()+" "+"fieldValue:"+fieldValue);
					else
						System.out.println("fieldValue:null");
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (field.getType().getName()
					.equals(java.lang.Boolean.class.getName())
					|| field.getType().getName().equals("boolean")) {
				// Boolean type
				try {
					fieldValue = field.get(obj);
					if(fieldValue!=null)
						System.out.println("fieldValueClass:"+fieldValue.getClass()+" "+"fieldValue:"+fieldValue);
					else
						System.out.println("fieldValue:null");
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (field.getType().getName()
					.equals(java.lang.Float.class.getName())
					|| field.getType().getName().equals("float")) {
				// Float type
				try {
					fieldValue = field.get(obj);
					if(fieldValue!=null)
						System.out.println("fieldValueClass:"+fieldValue.getClass()+" "+"fieldValue:"+fieldValue);
					else
						System.out.println("fieldValue:null");
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (field.getType().getName()
					.equals(java.lang.Double.class.getName())
					|| field.getType().getName().equals("double")) {
				// Double type
				try {
					fieldValue = field.get(obj);
					if(fieldValue!=null)
						System.out.println("fieldValueClass:"+fieldValue.getClass()+" "+"fieldValue:"+fieldValue);
					else
						System.out.println("fieldValue:null");
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// ...閸忔湹绮猾璇茬�锋潻妯款洣缂佈呯敾閸愭瑤绗�
			else {
				throw new InsertException(
						"@insert : only basic types allowed");
			}
			if (fieldName != null) {
				fieldMap.put(fieldName, fieldValue);
			}
		}
		return fieldMap;
	}
	
	/**根据tableName获取相应表的元数据进而获取列的数目*/
	private int getColumnNum(String tableName) {
		Connection conn = ConnectionUtil.getConnection();
		String sql = "select * from " + tableName + " limit 1";
		PreparedStatement stmt;
		try {
			stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData metadata = rs.getMetaData();
			int columnNum = metadata.getColumnCount();
			return columnNum;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			ConnectionUtil.returnConnection(conn);
		}
		return -1;
	}

	// 插入一个对象时需要设置相应的value（？？?）中的值
	private void setInsertParam(PreparedStatement prepareStatement,
			Map<String, Object> fieldMap, String tableName) throws SQLException {
		String[] columnNames = getColumnNames(tableName);
		for (int i = 1; i < columnNames.length; i++) {
			String columnName = columnNames[i];
			System.out.println("columnName:" + columnName);
			if (fieldMap.containsKey(columnName)) {
				Object fieldValue = fieldMap.get(columnName);
				// 没有定义的字段值就设置为null即可
				if (fieldValue == null) {
					prepareStatement.setNull(i, Types.NULL);
					continue;
				}
				System.out.println("fieldValue:" + fieldValue);
				System.out.println("fieldValue.getClass():"
						+ fieldValue.getClass());
				if (int.class == fieldValue.getClass()
						|| Integer.class == fieldValue.getClass()) {
					prepareStatement.setInt(i, (int) fieldValue);
				} else if (short.class == fieldValue.getClass()
						|| Short.class == fieldValue.getClass()) {
					prepareStatement.setShort(i, (short) fieldValue);
				} else if (long.class == fieldValue.getClass()
						|| Long.class == fieldValue.getClass()) {
					prepareStatement.setLong(i, (long) fieldValue);
				} else if (float.class == fieldValue.getClass()
						|| Float.class == fieldValue.getClass()) {
					prepareStatement.setFloat(i, (float) fieldValue);
				} else if (double.class == fieldValue.getClass()
						|| Double.class == fieldValue.getClass()) {
					prepareStatement.setDouble(i, (double) fieldValue);
				} else if (boolean.class == fieldValue.getClass()
						|| Boolean.class == fieldValue.getClass()) {
					prepareStatement.setBoolean(i, (boolean) fieldValue);
				} else if (Date.class == fieldValue.getClass()) {
					prepareStatement.setDate(i, (Date) fieldValue);
				} else if (Timestamp.class == fieldValue.getClass()) {
					prepareStatement.setTimestamp(i, (Timestamp) fieldValue);
				} else {
					prepareStatement.setString(i, (String) fieldValue);
				}
			}
		}
	}

	// 根据表明获取相应列的名称
	private String[] getColumnNames(String tableName) {
		String[] columnNames = null;
		//获取连接
		Connection conn = ConnectionUtil.getConnection();
		String sql = "select * from " + tableName + " limit 1";
		PreparedStatement stmt;
		try {
			stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData metadata = rs.getMetaData();
			columnNames = new String[metadata.getColumnCount() + 1];
			for (int i = 1; i <= metadata.getColumnCount(); i++) {
				columnNames[i] = metadata.getColumnName(i);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally{
			ConnectionUtil.returnConnection(conn);
		}
		return columnNames;
	}
	/***********************************************************/

	/**处理查询*/
	//异常要不优化一下？？直接Exception算了
	private Object handleSelect(String sql, Method method, Object[] args) 
			throws Exception {
		//实际从pool获取
		Connection connection = ConnectionUtil.getConnection();
		PreparedStatement prepareStatement = connection.prepareStatement(sql);
		//约定sql开头不能有空格
		JdbcUtil.setParam(prepareStatement, method, args);
		//查缓存
		SqlCache cache = SqlCache.getInstance();
		String statement = prepareStatement.toString().split(":")[1];
		Object obj = cache.get(statement);
		if(obj != null) return obj;
		//缓存没有
		System.out.println(prepareStatement.toString());
		ResultSet rs = prepareStatement.executeQuery();
		System.out.println("warnings:"+prepareStatement.getWarnings());
		//获取返回值类型
		Class returnType = (Class) method.getReturnType();
		List list = null;
		if(returnType == List.class){
			ParameterizedType genericReturnType = (ParameterizedType) method.getGenericReturnType();
			returnType = (Class) genericReturnType.getActualTypeArguments()[0];
			list = new ArrayList();
		}
		if(list == null && JdbcUtil.getRowCount(rs) > 1){
			throw new returnTypeMismatchedException("return type is a single model but the result set has more than 1 row");
		}
		ResultSetMetaData metaData = rs.getMetaData();
		Object instance = null;
		if(ClassUtil.isBasicType(returnType)){
			//返回基本数据类型
			while(rs.next()){
				System.out.println("basic:"+returnType.getSimpleName());
				instance = JdbcUtil.constructBasicObject(returnType, rs);
				if(list != null){
					list.add(instance);
				}else{
					break;
				}
			}
		}else{
			//返回bean
			Method[] methods = returnType.getMethods();
			Map<String, Method> setters = new HashMap<String, Method>();
			ClassUtil.fillSetters(setters, methods);
			while(rs.next()){
				System.out.println(returnType.getSimpleName());
				try {
					instance = returnType.newInstance();
				} catch (InstantiationException e) {
					System.out.println("there is an absence of a default constructor in the return model");
					e.printStackTrace();
				}
				JdbcUtil.setAttributes(instance, rs, setters, metaData);
				if(list != null){
					list.add(instance);
				}else{
					break;
				}
			}
		}
		rs.close();
		prepareStatement.close();
		ConnectionUtil.returnConnection(connection);
		if(list != null) {
			cache.put(statement, list);
			return list;
		}
		cache.put(statement, instance);
		return instance;
	}

}
