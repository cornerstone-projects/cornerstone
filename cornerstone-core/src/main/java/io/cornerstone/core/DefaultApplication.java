package io.cornerstone.core;

import java.io.File;
import java.io.IOException;
import java.lang.StackWalker.StackFrame;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

import io.cornerstone.core.util.ReflectionUtils;
import jakarta.servlet.ServletContext;
import lombok.Getter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Profiles;
import org.springframework.util.ClassUtils;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;
import static org.springframework.core.env.AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME;
import static org.springframework.jndi.JndiLocatorDelegate.IGNORE_JNDI_PROPERTY_NAME;

@Getter
public class DefaultApplication extends SpringBootServletInitializer implements Application {

	private String hostName;

	private String hostAddress;

	static volatile Application currentApplication;

	@Autowired
	private ApplicationContext context;

	public DefaultApplication() {
		boolean createdBySpring = StackWalker.getInstance(RETAIN_CLASS_REFERENCE)
			.walk(s -> s.map(StackFrame::getDeclaringClass)
				.anyMatch(c -> c == AbstractAutowireCapableBeanFactory.class));
		if (createdBySpring) {
			currentApplication = this;
		}
	}

	@Override
	public String getHostName() {
		if (this.hostName == null) {
			this.hostName = Application.super.getHostName();
		}
		return this.hostName;
	}

	@Override
	public String getHostAddress() {
		if (this.hostAddress == null) {
			this.hostAddress = Application.super.getHostAddress();
		}
		return this.hostAddress;
	}

	protected static void init(String[] args) throws IOException {

		System.setProperty(IGNORE_JNDI_PROPERTY_NAME, String.valueOf(true));

		if (ClassUtils.isPresent("org.springframework.boot.devtools.RemoteSpringApplication",
				DefaultApplication.class.getClassLoader())) {
			String profiles = System.getProperty(ACTIVE_PROFILES_PROPERTY_NAME);
			if (profiles == null) {
				profiles = System.getenv(ACTIVE_PROFILES_PROPERTY_NAME.replaceAll("\\.", "_").toUpperCase(Locale.ROOT));
				if (profiles == null) {
					System.setProperty(ACTIVE_PROFILES_PROPERTY_NAME, "dev");
				}
			}
		}

	}

	protected static void start(String[] args) throws Exception {
		start(args, null);
	}

	protected static void start(String[] args, Consumer<ApplicationContext> postStartAction) throws Exception {
		init(args);
		Class<?> caller = StackWalker.getInstance(RETAIN_CLASS_REFERENCE)
			.walk(s -> s
				.filter(f -> Objects.equals(f.getMethodName(), "main")
						&& Objects.equals(f.getMethodType(), MethodType.methodType(void.class, String[].class)))
				.findFirst()
				.map(StackFrame::getDeclaringClass)
				.orElseThrow(() -> new RuntimeException("start() method should be called in main method")));
		ApplicationContext ctx = SpringApplication.run(caller, args);
		if (postStartAction != null) {
			postStartAction.accept(ctx);
		}
		if (ctx.getEnvironment().acceptsProfiles(Profiles.of("dev"))) {
			File source = new ApplicationHome(caller).getSource();
			if (source != null && source.getAbsolutePath().endsWith("/bin/main")) {
				// run in eclipse
				System.out.println("Press 'Enter' key to shutdown");
				System.in.read();
				System.exit(0);
			}
		}
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(getClass());
	}

	@Override
	protected void deregisterJdbcDrivers(ServletContext servletContext) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			String className = "com.mysql.cj.jdbc.AbandonedConnectionCleanupThread";
			String methodName = "checkedShutdown";
			if (ClassUtils.isPresent(className, cl)) {
				ClassUtils.forName(className, cl).getMethod(methodName).invoke(null);
			}
		}
		catch (Throwable ignored) {
		}
		try {
			String className = "com.mysql.jdbc.AbandonedConnectionCleanupThread";
			String methodName = "checkedShutdown";
			if (ClassUtils.isPresent(className, cl)) {
				ClassUtils.forName(className, cl).getMethod(methodName).invoke(null);
			}
		}
		catch (Throwable ignored) {
		}
		super.deregisterJdbcDrivers(servletContext);
		cancelTimers();
		cleanupThreadLocals();
	}

	protected void cancelTimers() {
		try {
			for (Thread thread : Thread.getAllStackTraces().keySet()) {
				if (thread.getClass().getSimpleName().equals("TimerThread")) {
					cancelTimer(thread);
				}
			}
		}
		catch (Throwable ignored) {
		}
	}

	private void cancelTimer(Thread thread) throws Exception {
		Object queue = ReflectionUtils.getFieldValue(thread, "queue");
		Method m = queue.getClass().getDeclaredMethod("isEmpty");
		m.setAccessible(true);
		if ((boolean) m.invoke(queue)) {
			// Timer::cancel
			synchronized (queue) {
				ReflectionUtils.setFieldValue(thread, "newTasksMayBeScheduled", false);
				m = queue.getClass().getDeclaredMethod("clear");
				m.setAccessible(true);
				m.invoke(queue);
				queue.notify();
			}
		}
	}

	protected void cleanupThreadLocals() {
		try {
			for (Thread thread : Thread.getAllStackTraces().keySet()) {
				cleanupThreadLocals(thread);
			}
		}
		catch (Throwable ignored) {
		}
	}

	private void cleanupThreadLocals(Thread thread) throws Exception {
		if ("JettyShutdownThread".equals(thread.getName())) {
			return; // see https://github.com/eclipse/jetty.project/issues/5782
		}
		for (String name : "threadLocals,inheritableThreadLocals".split(",")) {
			Field f = Thread.class.getDeclaredField(name);
			f.setAccessible(true);
			f.set(thread, null);
		}
	}

}
