package bighead.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 若某个被注解为Dao的类
 * 其中某些方法需要是对数据库CRUD
 * 那么就可在这个方法增加Sql注解
 * */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sql {
	public abstract String value();
}
