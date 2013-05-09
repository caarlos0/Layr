package home;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import layr.routing.Response;
import layr.routing.ResponseBuilder;
import layr.routing.annotations.GET;
import layr.routing.annotations.Route;
import layr.routing.annotations.TemplateParameter;
import layr.routing.annotations.WebResource;

@WebResource("home")
@Stateless
public class HomeResource {

	@EJB ProfileMeasurer profileMeasurer;
	@TemplateParameter String userName;

	@GET @Route
	public Response chooseARandomHomeScreen(){
		String templateName = ( profileMeasurer.measure() == 0 )
							? "home/default.xhtml"
							: "home/alternative.xhtml";

		if ( userName == null )
			userName = "Guest";

		return ResponseBuilder.renderTemplate( templateName );
	}
}