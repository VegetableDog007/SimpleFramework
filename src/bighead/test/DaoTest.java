package bighead.test;

import java.util.List;

import bighead.dao.EnterpriseDao;
import bighead.model.Enterprise;
import bighead.proxy.DaoContainer;
import org.junit.Test;

public class DaoTest {

	@Test
	public void test(){
		EnterpriseDao enterpriseDao = (EnterpriseDao) DaoContainer.getDao("enterpriseDao");
		/**Qurey*/
		List<Enterprise> enterprises = enterpriseDao.getAll();
		for (Enterprise enterprise : enterprises) {
			System.out.println(enterprise);
		}
//		System.out.println(enterpriseDao.getNameById(1));
//		System.out.println(enterpriseDao.getByName("网易"));
		/**Update*/
		//鍩烘湰Update
//		enterpriseDao.insertEnterprise(10, "腾讯11", "深圳", "牛逼");
//		enterpriseDao.deleteEnterpriseById(10);
//		enterpriseDao.updateEnterpriseDescriptionById("阿里牛逼", 10);
		//insert瀵硅薄
//		Enterprise enterprise = new Enterprise(11, "腾讯", "深圳", "牛逼");
//		System.out.println(enterprise);
//		enterpriseDao.insertEnterprise(enterprise);
//		Enterprise enterprise = new Enterprise(0, "网易", "广州", "desc");
//		enterpriseDao.insertEnterpriseAutoInc(enterprise);
	}
}
