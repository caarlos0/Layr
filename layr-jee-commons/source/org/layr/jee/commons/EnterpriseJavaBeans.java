package org.layr.jee.commons;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.layr.commons.Reflection;
import org.layr.commons.StringUtil;

/**
 * @author Miere Liniel Teixeira
 *
 */
public class EnterpriseJavaBeans {
	
	private InitialContext context;
	private Map<String, String> registeredEJBViews;
	
	public EnterpriseJavaBeans() throws NamingException {
		context = new InitialContext();
	}

	/**
	 * Mapping EJB Global JNDI as defined at Oracle's EJB 3.1 Documentation
	 * http://docs.oracle.com/javaee/6/tutorial/doc/gipjf.html#girgn
	 * 
	 * @param clazz
	 */
	public void seekForJNDIEJBViewsfor(Class<?> clazz) {
		Stateless stateless = clazz.getAnnotation(Stateless.class);
		Stateful stateful = clazz.getAnnotation(Stateful.class);
		Singleton singleton = clazz.getAnnotation(Singleton.class);

		if (stateless == null && stateful == null && singleton == null)
			return ;

		registerAnnotatedEJBViewsInterfaces(clazz);
		registerEJBViewsFromImplementedInterface(clazz);
		register(clazz);
	}

	/**
	 * @param clazz
	 */
	public void registerEJBViewsFromImplementedInterface(Class<?> clazz) {
		Class<?>[] interfaces = clazz.getInterfaces();

		for (Class<?> interfaceClass : interfaces)
			register(clazz, interfaceClass);
	}

	/**
	 * @param clazz
	 */
	public void registerAnnotatedEJBViewsInterfaces(Class<?> clazz) {
		Local local = clazz.getAnnotation(Local.class);
		Remote remote = clazz.getAnnotation(Remote.class);

		if (local != null)
			for (Class<?> interfaceClass : local.value())
				register(clazz, interfaceClass);

		if (remote != null)
			for (Class<?> interfaceClass : remote.value())
				register(clazz, interfaceClass);
	}

	/**
	 * @param clazz
	 * @param interfaceClass
	 */
	public void register(Class<?> clazz, Class<?> interfaceClass) {
		register(
			interfaceClass.getCanonicalName(),
			clazz.getSimpleName() + "!" + interfaceClass.getCanonicalName());
	}

	/**
	 * @param clazz
	 */
	public void register(Class<?> clazz) {
		register(clazz.getCanonicalName(), clazz.getSimpleName());
	}
	
	public void register(String clazz, String jndiPath) {
		getRegisteredEJBViews().put(clazz, jndiPath);
	}

	/**
	 * @param object
	 */
	public void injectEJB(Object object) {
		Class<?> clazz = object.getClass();
		while (!clazz.equals(Object.class)) {
			for (Field field : clazz.getDeclaredFields()) {
				injectEJB(object, field);
			}

			clazz = clazz.getSuperclass();
		}
	}

	/**
	 * @param target
	 * @param field
	 */
	public void injectEJB(Object target, Field field) {
		try {
			EJB ejb = field.getAnnotation(EJB.class);
			if (ejb != null) {
				Class<?> declaringClass = field.getType();
				Object lookup = lookupJNDI(ejb, declaringClass);

				Method setter = Reflection.extractSetterFor(target, field.getName());
				if( setter != null ) {
					setter.invoke(target, lookup);
				} else {
					field.setAccessible(true);
					field.set(target, lookup);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param ejbAnnotation
	 * @param clazz
	 * @return
	 * @throws NamingException
	 */
	public Object lookupJNDI(EJB ejbAnnotation, Class<?> clazz) throws NamingException {
		String jndiName = ejbAnnotation.mappedName();
		return lookupJNDI(clazz, jndiName); 
	}

	public Object lookupJNDI(Class<?> clazz, String jndiName)
			throws NamingException {
		if ( StringUtil.isEmpty(jndiName) )
				jndiName = "java:module/" + getRegisteredEJBViews().get(clazz.getCanonicalName());
		return lookup(jndiName);
	}

	public Object lookup(String jndiName) throws NamingException {
		return context.lookup(jndiName);
	}

	public Map<String, String> getRegisteredEJBViews() {
		if (registeredEJBViews == null)
			registeredEJBViews = new HashMap<String, String>();
		return registeredEJBViews;
	}

	public void setRegisteredEJBViews(Map<String, String> registeredEJBViews) {
		this.registeredEJBViews = registeredEJBViews;
	}

	public InitialContext getContext() {
		return context;
	}

	public void setContext(InitialContext context) {
		this.context = context;
	}
	
}
