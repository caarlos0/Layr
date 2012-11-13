package org.layr.jee.routing.business.sample;

import java.util.List;

import org.layr.jee.routing.business.Parameter;
import org.layr.jee.routing.business.Route;
import org.layr.jee.routing.business.WebResource;



@WebResource(rootURL="/hello/", template="/hello.xhtml")
public class HelloResource{

	public static final String EXECUTED_METHOD = "EXECUTED_METHOD";

	private Hello hello;
	private List<World> worlds;
	private double sum;

	@Route(json=true)
	public String sayHello() {
		return "Hello World";
	}

	@Route(pattern="/say/#{id}/something/")
	public String say(
				@Parameter("id") Integer id1,
				@Parameter("hello.realworld.id") Long id2)
	{
		return "Id '"+id1+"' and '"+id2+"' arrived from request.";
	}
	
	@Route(pattern="/add")
	public String create(
			@Parameter("name") String name) {
		return name;
	}
	
	@Route(pattern="/addAsJSON", json=true)
	public String createAsJSON(
			@Parameter("name") String name) {
		return name;
	}

	public Hello getHello() {
		return hello;
	}

	public void setHello(Hello hello) {
		this.hello = hello;
	}

	public double getSum() {
		return sum;
	}

	public void setSum(Double sum) {
		this.sum = sum;
	}

	public List<World> getWorlds() {
		return worlds;
	}

	public void setWorlds(List<World> worlds) {
		this.worlds = worlds;
	}
}