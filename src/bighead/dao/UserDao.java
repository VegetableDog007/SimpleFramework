package bighead.dao;

import bighead.annotation.Dao;
import bighead.annotation.Sql;
import bighead.model.User;

@Dao("userDao")
public interface UserDao {

	@Sql("#INSERT INTO user " +
			"VALUES(?,?,?,?)")
	Integer insert(User user);
	
	@Sql("SELECT * " +
			"FROM user " +
			"WHERE id = ?")
	User get(int id);
	
	@Sql("UPDATE user " +
			"SET name = ? " +
			"WHERE id = ?")
	Integer update(String name, int id);
	
	@Sql("DELETE FROM user " +
			"WHERE id = ?")
	Integer delete(int id);
	
}
