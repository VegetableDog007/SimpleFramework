package bighead.proxy;

import java.lang.reflect.Proxy;

/**为注解为Dao的类建立代理
 * 由DaoProxy类进行代理
 * */
public class ProxyFactory {

	public static Object getDaoProxy(Class<?> targetClass, DaoProxy handler) {
		Class[] c = {targetClass};
		return Proxy.newProxyInstance(targetClass.getClassLoader(), c, handler);
	}

}
