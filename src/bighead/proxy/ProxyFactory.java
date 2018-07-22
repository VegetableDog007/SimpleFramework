package bighead.proxy;

import java.lang.reflect.Proxy;

/**Ϊע��ΪDao���ཨ������
 * ��DaoProxy����д���
 * */
public class ProxyFactory {

	public static Object getDaoProxy(Class<?> targetClass, DaoProxy handler) {
		Class[] c = {targetClass};
		return Proxy.newProxyInstance(targetClass.getClassLoader(), c, handler);
	}

}
