package layr.routing.lifecycle;

import static layr.commons.StringUtil.oneOf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import layr.commons.Reflection;
import layr.engine.RequestContext;
import layr.engine.expressions.URLPattern;
import layr.routing.api.DELETE;
import layr.routing.api.GET;
import layr.routing.api.POST;
import layr.routing.api.PUT;
import layr.routing.api.PathParameter;
import layr.routing.api.QueryParameter;
import layr.routing.api.TemplateParameter;
import layr.routing.api.WebResource;

/**
 * Extract all Layr needed information from an class that
 * should to be used during requests. Developers should not
 * "handle" classes during request time, but during deploy time
 * instead. This approach will grant that requests are lightweight
 * enough for a fast response.
 */
public class HandledClass {

    Class<?> targetClass;
    List<HandledMethod> routes;
    List<HandledParameter> parameters;
    String rootPath;

    public HandledClass(Class<?> targetClass) {
    	this.targetClass = targetClass;
    	this.rootPath = extractRootPath();
    	this.routes = extractMethodRoutes();
	}

	/**
	 * @return
	 */
	public String extractRootPath() {
		WebResource webResource = targetClass.getAnnotation( WebResource.class );
		String rootPath = webResource.value();
		return ("/" + rootPath + "/").replace("//", "/");
	}

	/**
	 * @return
	 */
	public List<HandledMethod> extractMethodRoutes() {
		List<HandledMethod> routes = new ArrayList<HandledMethod>();
		for ( Method method : measureAvailableRoutes() )
			for ( Annotation annotation : method.getAnnotations() ) {
				HandledMethod routeMethod = createRouteMethod( method, annotation );
				if ( routeMethod != null )
					routes.add( routeMethod );
			}
		return routes;
	}

	/**
	 * @param method
	 * @return
	 */
	public HandledMethod createRouteMethod(Method method, Annotation httpMethodAnnotation) {
		if (httpMethodAnnotation instanceof POST)
			return new HandledMethod( this, method, "POST", ((POST) httpMethodAnnotation).value() );
		else if (httpMethodAnnotation instanceof PUT)
			return new HandledMethod( this, method, "PUT", ((PUT) httpMethodAnnotation).value() );
		else if (httpMethodAnnotation instanceof DELETE)
			return new HandledMethod( this, method, "DELETE", ((DELETE) httpMethodAnnotation).value() );
		else if (httpMethodAnnotation instanceof GET)
			return new HandledMethod( this, method, "GET", ((GET) httpMethodAnnotation).value() );
		return null;
	}

	/**
	 * Measure which route methods are available from target instance
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public List<Method> measureAvailableRoutes() {
		return Reflection.extractAnnotatedMethodsFor(
				targetClass, GET.class, POST.class, DELETE.class, PUT.class);
	}

	/**
	 * @return
	 */
	public List<HandledParameter> getParameters(){
		if ( parameters == null ) {
			parameters = new ArrayList<HandledParameter>();
			for ( Field field : measureAvailableParameters() )
				createRouteParameter( field );
		}

		return parameters;
	}

	@SuppressWarnings("unchecked")
	public List<Field> measureAvailableParameters() {
		return Reflection.extractAnnotatedFieldsFor(
				targetClass, TemplateParameter.class, PathParameter.class, QueryParameter.class );
	}

	public void createRouteParameter(Field field) {
		for ( Annotation annotation : field.getAnnotations()){
			if ( PathParameter.class.isInstance( annotation ) )
				parameters.add( createPathParameter( field, annotation ));
			else if ( QueryParameter.class.isInstance( annotation ) )
				parameters.add( createQueryParameter( field, annotation ));
			else if ( TemplateParameter.class.isInstance( annotation ) )
				parameters.add( createTemplateParameter( field, annotation ));
		}
	}

	public PathHandledParameter createPathParameter(Field field, Annotation annotation) {
		String parameterName = oneOf( ((PathParameter)annotation).value(), field.getName() );
		return new PathHandledParameter( parameterName, field.getType() );
	}

	public QueryHandledParameter createQueryParameter(Field field, Annotation annotation) {
		String parameterName = oneOf( ((QueryParameter)annotation).value(), field.getName() );
		return new QueryHandledParameter( parameterName, field.getType() );
	}

	public TemplateHandledParameter createTemplateParameter(Field field, Annotation annotation) {
		String parameterName = oneOf( ((TemplateParameter)annotation).value(), field.getName() );
		return new TemplateHandledParameter( parameterName, field.getType() );
	}

	public boolean matchesTheRequestURI(RequestContext requestContext) {
		String methodUrlPattern = new URLPattern().parseMethodUrlPatternToRegExp( rootPath ) + ".*";
		String requestURI = requestContext.getRequestURI();
		return requestURI.matches( methodUrlPattern );
	}

	/**
	 * @return
	 */
	public Class<?> getTargetClass() {
		return targetClass;
	}

	/**
	 * @return
	 */
	public List<HandledMethod> getRouteMethods() {
		return routes;
    }
}