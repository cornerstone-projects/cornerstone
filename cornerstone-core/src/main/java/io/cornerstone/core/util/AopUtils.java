package io.cornerstone.core.util;

import java.lang.reflect.Proxy;

import org.hibernate.proxy.HibernateProxy;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;

import javassist.util.proxy.ProxyObject;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AopUtils {

	public static Class<?> getActualClass(Object object) {
		return getActualClass(object.getClass());
	}

	public static Class<?> getActualClass(Class<?> clazz) {
		if (Proxy.isProxyClass(clazz))
			return clazz.getInterfaces()[0];
		if (ProxyObject.class.isAssignableFrom(clazz) || HibernateProxy.class.isAssignableFrom(clazz)
				|| SpringProxy.class.isAssignableFrom(clazz) || clazz.getName().contains("$$EnhancerBySpringCGLIB$$")) {
			clazz = clazz.getSuperclass();
			return getActualClass(clazz);
		} else {
			return clazz;
		}
	}

	public static Object getTargetObject(Object proxy) {
		while (proxy instanceof Advised) {
			try {
				Object target = ((Advised) proxy).getTargetSource().getTarget();
				if (target == null)
					return proxy;
				else
					proxy = target;
			} catch (Exception e) {
				e.printStackTrace();
				return proxy;
			}
		}
		return proxy;
	}

}
