package bighead.dao;

import java.util.List;

import bighead.annotation.Dao;
import bighead.annotation.Sql;
import bighead.model.Enterprise;

@Dao("enterpriseDao")
public interface EnterpriseDao {

	@Sql("SELECT * FROM enterprise WHERE id = ?")
	Enterprise get(int id);
	
	@Sql("SELECT * FROM enterprise WHERE name = ?")
	Enterprise getByName(String name);
	
	@Sql("SELECT * FROM enterprise WHERE address= ? AND name = ?")
	Enterprise getByInfo(String address, String name);
	
	@Sql("SELECT * FROM enterprise")
	List<Enterprise> getAll();
	
	@Sql("SELECT name FROM enterprise where id = ?")
	String getNameById(int id);
	
	@Sql("SELECT id FROM enterprise WHERE name = ?")
	Integer getIdByName(String name);
	
	@Sql("INSERT INTO enterprise VALUES (?, ?, ?, ?)")
	Integer insertEnterprise(int id, String name, String address, String description);
	
	@Sql("DELETE FROM enterprise WHERE id = ?")
	Integer deleteEnterpriseById(int id);
	
	@Sql("UPDATE enterprise SET description = ? WHERE id = ?")
	Integer updateEnterpriseDescriptionById(String description, int id);
	
	@Sql("#INSERT INTO enterprise VALUES(?,?,?,?)")
	Integer insertEnterprise(Enterprise enterprise);
	
	@Sql("@INSERT INTO enterprise")
	Integer insertEnterpriseAutoInc(Enterprise enterprise);
	
}
