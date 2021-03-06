package org.layr.jee.routing.natural;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;

import org.layr.engine.AbstractRequestContext;
import org.layr.engine.components.DefaultComponentFactory;
import org.layr.engine.components.IComponentFactory;
import org.layr.engine.components.TagLib;
import org.layr.jee.commons.JEEConfiguration;

@HandlesTypes(TagLib.class)
public class PluginInitializer implements ServletContainerInitializer {

	private Map<String, IComponentFactory> registeredTagLibs;
	private JEEConfiguration jeeConfiguration;

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContainerInitializer#onStartup(java.util.Set, javax.servlet.ServletContext)
	 */
	@Override
	public void onStartup(Set<Class<?>> classes, ServletContext servletContext)
			throws ServletException {
		try {
			initializeConfiguration(servletContext);
			for (Class<?> clazz : classes)
				handleClass(clazz);
			reportLayrInitializationStatus(servletContext);
		} catch (Throwable e) {
			throw new ServletException("Can't initialize the Application Context.", e);
		}
	}

	/**
	 * @param clazz
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void handleClass(Class<?> clazz) throws InstantiationException,
			IllegalAccessException {
		tryToRegisterATag(clazz);
	}

	/**
	 * @param servletContext
	 */
	public void reportLayrInitializationStatus(ServletContext servletContext) {
		servletContext.log("[Layr] JEE Natural Routing Plugin loaded");
	}

	/**
	 * Initialize Layr Natural Routing Configuration and configure it for further use.
	 * @param servletContext
	 */
	public void initializeConfiguration(ServletContext servletContext) throws NamingException {
		registeredTagLibs = initializeTagLibs();
		setJeeConfiguration(new JEEConfiguration(servletContext));
		getJeeConfiguration().setRegisteredTagLibs(registeredTagLibs);
		servletContext.setAttribute(JEEConfiguration.class.getCanonicalName(), getJeeConfiguration());
	}
	
	/**
	 * @return
	 */
	public Map<String, IComponentFactory> initializeTagLibs(){
		HashMap<String, IComponentFactory> registeredTagLibs = new HashMap<String, IComponentFactory>();
		AbstractRequestContext.populateWithDefaultTagLibs(registeredTagLibs);
		return registeredTagLibs;
	}

	/**
	 * Try to Register a TagLib based on class annotation.
	 * 
	 * @param clazz
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void tryToRegisterATag(Class<?> clazz)
			throws InstantiationException, IllegalAccessException
	{
		TagLib annotation = clazz.getAnnotation(TagLib.class);
		if (annotation == null)
			return;

		String namespace = annotation.value();
		IComponentFactory factory = (IComponentFactory)clazz.newInstance();
		defineRootDirIfIsADefaultComponentFactory(clazz, factory);
		getRegisteredTagLibs().put(namespace, factory);
	}

	/**
	 * @param clazz
	 * @param factory
	 */
	public void defineRootDirIfIsADefaultComponentFactory(Class<?> clazz, IComponentFactory factory) {
		if (DefaultComponentFactory.class.isInstance(factory))
			((DefaultComponentFactory)factory).setRootDir(
				clazz.getPackage().getName().replace(".", "/"));
	}

	/**
	 * @return
	 */
	public Map<String, IComponentFactory> getRegisteredTagLibs() {
		return registeredTagLibs;
	}

	/**
	 * @param registeredTagLibs
	 */
	public void setRegisteredTagLibs(Map<String, IComponentFactory> registeredTagLibs) {
		this.registeredTagLibs = registeredTagLibs;
	}

	/**
	 * @return
	 */
	public JEEConfiguration getJeeConfiguration() {
		return jeeConfiguration;
	}

	/**
	 * @param jeeConfiguration
	 */
	public void setJeeConfiguration(JEEConfiguration jeeConfiguration) {
		this.jeeConfiguration = jeeConfiguration;
	}

}
