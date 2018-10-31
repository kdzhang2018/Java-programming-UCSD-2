package module6;

import java.util.ArrayList;
import java.util.List;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import processing.core.PGraphics;

/** 
 * A class to represent AirportMarkers on a world map.
 *   
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 * MOOC team
 *
 */
public class AirportMarker extends CommonMarker {
	private boolean withRoute;
	private boolean international;
	private List<SimpleLinesMarker> routes;
	private List<SimpleLinesMarker> sourceRoutes;
	private List<SimpleLinesMarker> destRoutes;
	
	public AirportMarker(Feature city) {
		super(((PointFeature)city).getLocation(), city.getProperties());
	
	}
	
	public void setWithRoute(boolean status){
		withRoute = status;	
	}
	
	public void setInternational(boolean status){
		international = status;	
	}
	
	public boolean getInternational(){
		return international;	
	}

	// create routes using allRoutes list
	public void createRoutes(List<Marker> allRoutes){
		routes = new ArrayList<SimpleLinesMarker>();
		sourceRoutes = new ArrayList<SimpleLinesMarker>();
		destRoutes = new ArrayList<SimpleLinesMarker>();
		for(Marker m: allRoutes){
			SimpleLinesMarker slm = (SimpleLinesMarker) m;
			int code = Integer.parseInt(getId()); 
			int source = Integer.parseInt(slm.getStringProperty("source"));
			int dest = Integer.parseInt(slm.getStringProperty("destination"));
			if(code == source){
				sourceRoutes.add(slm);
				routes.add(slm);
			}
			if(code == dest){
				destRoutes.add(slm);
				routes.add(slm);
			}
		}
	}
	
	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		// red square: no departing routes
		if(!withRoute){
			pg.fill(255, 0, 0);
			pg.rect(x, y, 5, 5);
		}
		// blue dot: only departing routes in the same country
		else if (!international){
			pg.fill(0, 0, 255);
			pg.ellipse(x, y, 5, 5);
		}
		// yellow dot: international airports
		else{
			pg.fill(255, 255, 0);
			pg.ellipse(x, y, 5, 5);
		}
		
		
	}

	@Override
	public void showTitle(PGraphics pg, float x, float y) {
		 // show rectangle with title
		String title = getName() + "," + getCity() + "," + getCountry();
		pg.pushStyle();
		
		pg.fill(255, 255, 255);
		pg.rect(x+5, y-10, pg.textWidth(title)+10, 20);
		pg.fill(0, 0, 0);
		pg.text(title, x+7, y+5);
		pg.popStyle();
		
		// show routes
		
		/*createRoutes(routeList); // routeList is a private variable in AirportMap and cannot be used here
		for (Marker r:getRoutes()) {
			r.setHidden(false);
		}*/
		
	}
	
	public String getName()
	{
		return getStringProperty("name").substring(1, getStringProperty("name").length()-1);
	}
	
	public String getCity()
	{
		return getStringProperty("city").substring(1, getStringProperty("city").length()-1);
	}
	
	public String getCountry()
	{
		return getStringProperty("country").substring(1, getStringProperty("country").length()-1);
	}
	
	public String getCode()
	{
		return getStringProperty("code");	
	}
	
	
	public List<SimpleLinesMarker> getRoutes(){
		return routes;
	}
	public List<SimpleLinesMarker> getSourceRoutes(){
		return sourceRoutes;
	}
	public List<SimpleLinesMarker> getDestRoutes(){
		return destRoutes;
	}
	
}
