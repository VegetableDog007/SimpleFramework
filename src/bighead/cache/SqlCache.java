package bighead.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import bighead.exception.UnqualifiedSqlException;

/**单例类
 * 这里仅仅是很简陋的缓存模拟
 * 最多缓存10条sql
 * 那么由一个队列记录sql进入缓存的先后
 * */
public class SqlCache {
	/**key:sql语句 val:缓存的查询记录集 模拟在第一个缓存*/
	Map<String, Object> resultMap = new HashMap<String, Object>();
	
	/**若某个已经缓存的sql所*/
	Queue<String> abandonedQueue = new LinkedList<String>();
	
	private static SqlCache cache = new SqlCache();

	private SqlCache() {
	};

	public static SqlCache getInstance() {
		return cache;
	}
	
	/**根据sql查找缓存*/
	public synchronized Object get(String sql) {
		if (!resultMap.containsKey(sql))
			return null;
		abandonedQueue.remove(sql);
		abandonedQueue.add(sql);
		return resultMap.get(sql);
	}
	
	/**把第一次查询后的结果与对应sql写入缓存，但是只写入结果集只有10条以内的sql*/
	public synchronized boolean put(String sql, Object obj) {
		if (obj instanceof List) {
			List list = (List) obj;
			if (list.size() > 10)
				return false;
		}
		String remove = null;
		//已经够10个sql需要移除最早缓存的了
		if (abandonedQueue.size() == 10) {
			remove = abandonedQueue.remove();
			resultMap.remove(remove);
		}
		//sql进入队列，并放入缓冲resultMap
		abandonedQueue.add(sql);
		resultMap.put(sql, obj);
		return true;
	}
	
	/**处理updateSql使得相应的表
	 * 并把缓存中相应的与该update表相关sql从缓存中失效
	 * */
	public synchronized int update(String updateSql) {
		try {
			updateSql = updateSql.toLowerCase().trim();
			String[] parts = updateSql.split(" ");
			if (parts.length == 0 || parts == null)
				return -1;
			String op = parts[0];
			String[] tableNames = null;
			if (op.equals("insert")) {
				String beforeFrom = StringUtils.substringBefore(updateSql, "from");
				String tableToBeInserted = null;
				if (beforeFrom.contains("("))
					tableToBeInserted = StringUtils.substringBefore(
							StringUtils.substringAfter(beforeFrom, "into"), "(");
				else
					tableToBeInserted = StringUtils.substringAfter(beforeFrom,
							"into");
				tableNames = new String[] { StringUtils
						.deleteWhitespace(tableToBeInserted) };
			} else if (op.equals("update")) {
				tableNames = StringUtils.deleteWhitespace(
						StringUtils.substringBefore(
								StringUtils.substringAfter(updateSql, "update"),
								"set")).split(",");
			} else {
				tableNames = StringUtils.deleteWhitespace(
						StringUtils.substringBefore(
								StringUtils.substringAfter(updateSql, "from"),
								"where")).split(",");
			}
			if (tableNames.length < 1)
				return -1;
			Set<String> keySet = resultMap.keySet();
			int count = 0;
			for (String sql : keySet) {
				for (String tableName : tableNames) {
					if (sql.contains(tableName))
						resultMap.remove(sql);
					count++;
					break;
				}
			}
			return count;
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
