package io.cornerstone.core.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Value;
import lombok.experimental.UtilityClass;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.hibernate.proxy.HibernateProxy;

import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.ClassUtils;

@UtilityClass
public class ReflectionUtils {

	public static final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

	private static final boolean JDK9PLUS = ClassUtils.isPresent("java.lang.StackWalker",
			System.class.getClassLoader());

	public static Class<?> getEntityClass(Object entity) {
		Class<?> clazz = entity.getClass();
		return HibernateProxy.class.isAssignableFrom(clazz) ? clazz.getSuperclass() : clazz;
	}

	public static Set<Class<?>> getAllInterfaces(Class<?> clazz) {
		Set<Class<?>> set = new HashSet<>();
		if (clazz.isInterface()) {
			set.add(clazz);
			for (Class<?> intf : clazz.getInterfaces()) {
				set.addAll(getAllInterfaces(intf));
			}
		}
		else {
			for (Class<?> intf : ClassUtils.getAllInterfacesForClassAsSet(clazz)) {
				set.addAll(getAllInterfaces(intf));
			}
		}
		return set;
	}

	public static String[] getParameterNames(Constructor<?> ctor) {
		return doGetParameterNames(ctor);
	}

	public static String[] getParameterNames(Method method) {
		method = BridgeMethodResolver.findBridgedMethod(method);
		return doGetParameterNames(method);
	}

	public static String[] getParameterNames(JoinPoint jp) {
		MethodSignature sig = (MethodSignature) jp.getSignature();
		Class<?> clz = jp.getTarget().getClass();
		Method method;
		try {
			method = Proxy.isProxyClass(clz) ? sig.getMethod()
					: clz.getDeclaredMethod(sig.getName(), sig.getParameterTypes());
		}
		catch (NoSuchMethodException ex) {
			method = sig.getMethod();
		}
		return getParameterNames(method);
	}

	private static String[] doGetParameterNames(Executable executable) {
		if ((executable instanceof Method)) {
			return parameterNameDiscoverer.getParameterNames((Method) executable);
		}
		else {
			return parameterNameDiscoverer.getParameterNames((Constructor<?>) executable);
		}
	}

	public static Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
		try {
			Field f = clazz.getDeclaredField(name);
			f.setAccessible(true);
			return f;
		}
		catch (NoSuchFieldException ex) {
			if (clazz == Object.class) {
				throw ex;
			}
			return getField(clazz.getSuperclass(), name);
		}

	}

	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(Object o, String name) {
		try {
			Field f = getField(o.getClass(), name);
			return (T) f.get(o);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static void setFieldValue(Object o, String name, Object value) {
		try {
			Field f = getField(o.getClass(), name);
			f.set(o, value);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static String getCurrentMethodName() {
		return Thread.currentThread().getStackTrace()[2].getMethodName();
	}

	public static String stringify(Method method) {
		return stringify(method, false);
	}

	public static String stringify(Method method, boolean simpleParameterName) {
		return stringify(method, false, false);
	}

	public static String stringify(Method method, boolean fullParameterName, boolean excludeDeclaringClass) {
		StringBuilder sb = new StringBuilder();
		if (!excludeDeclaringClass) {
			sb.append(method.getDeclaringClass().getName()).append(".");
		}
		sb.append(method.getName()).append("(");
		Class<?>[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			sb.append(fullParameterName ? parameterTypes[i].getName() : parameterTypes[i].getSimpleName());
			if (i < (parameterTypes.length - 1)) {
				sb.append(',');
			}
		}
		sb.append(")");
		return sb.toString();
	}

	public static Object invokeDefaultMethod(Object object, Method method, Object[] arguments) throws Throwable {
		if (!method.isDefault()) {
			throw new IllegalArgumentException("Method is not default: " + method);
		}
		Class<?> objectType = method.getDeclaringClass();
		MethodHandle mh = defaultMethods.computeIfAbsent(new MethodCacheKey(object, method), key -> {
			try {
				Object o = key.getObject();
				Method m = key.getMethod();
				if (JDK9PLUS) {
					return MethodHandles.lookup()
							.findSpecial(objectType, m.getName(),
									MethodType.methodType(m.getReturnType(), m.getParameterTypes()), objectType)
							.bindTo(o);
				}
				else {
					Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);
					constructor.setAccessible(true);
					return constructor.newInstance(objectType).in(objectType).unreflectSpecial(m, objectType).bindTo(o);
				}
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
		return mh.invokeWithArguments(arguments);
	}

	@Value
	private static class MethodCacheKey {

		Object object;

		Method method;

	}

	private static final Map<MethodCacheKey, MethodHandle> defaultMethods = new ConcurrentHashMap<>();

}
