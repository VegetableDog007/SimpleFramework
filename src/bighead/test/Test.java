package bighead.test;

import bighead.dao.UserDao;
import bighead.model.User;
import bighead.proxy.DaoContainer;

public class Test {

	@org.junit.Test
	public void test(){
		UserDao dao = (UserDao) DaoContainer.getDao("userDao");
		User user = new User(3, "dubi009", "男", 21);
		System.out.println(user);
//		dao.insert(user);
		System.out.println(dao.get(1));
//		dao.update("db", 1);
//		dao.delete(1);
	}
}
