package bighead.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ��ĳ����ע��ΪDao����
 * ����ĳЩ������Ҫ�Ƕ����ݿ�CRUD
 * ��ô�Ϳ��������������Sqlע��
 * */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sql {
	public abstract String value();
}
