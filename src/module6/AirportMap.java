package module6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.geo.Location;
import parsing.ParseFeed;
import processing.core.PApplet;

/** An applet that shows airports (and routes)
 * on a world map.  
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 * MOOC team
 *
 */
public class AirportMap extends PApplet {
	
	UnfoldingMap map;
	private List<Marker> airportList;
	private List<Marker> routeList;
	
	private CommonMarker lastClicked;
	private CommonMarker lastSelected;
	
	public void setup() {
		// setting up PAppler
		size(800,600, OPENGL);
		
		// setting up map and default events
		map = new UnfoldingMap(this, 0, 0, 800, 600, new Google.GoogleMapProvider());
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// get features from airport data
		List<PointFeature> features = ParseFeed.parseAirports(this, "https://raw.githubusercontent.com/jpatokal/openflights/master/data/airports.dat");
		
		// list for markers, hashmap for quicker access when matching with routes
		airportList = new ArrayList<Marker>();
		HashMap<Integer, Location> airports = new HashMap<Integer, Location>();
		
		// create markers from features
		for(PointFeature feature : features) {
			//System.out.println(feature.getId()+" "+feature.getProperties());
			AirportMarker m = new AirportMarker(feature);
			
			//copy the airport Id from feature to AirportMarker m!!!
			m.setId(feature.getId());
			
			m.setRadius(5);
			airportList.add(m);
			//System.out.println(m.getId()+" "+m.getProperties());
			
			// put airport in hashmap with OpenFlights unique id for key
			airports.put(Integer.parseInt(feature.getId()), feature.getLocation());
		
		}
		
		
		// parse route data
		List<ShapeFeature> routes = ParseFeed.parseRoutes(this, "https://raw.githubusercontent.com/jpatokal/openflights/master/data/routes.dat");
		routeList = new ArrayList<Marker>();
		for(ShapeFeature route : routes) {
			
			// get source and destination airportIds
			int source = Integer.parseInt((String)route.getProperty("source"));
			int dest = Integer.parseInt((String)route.getProperty("destination"));
			
			// get locations for airports on route
			if(airports.containsKey(source) && airports.containsKey(dest)) {
				route.addLocation(airports.get(source));
				route.addLocation(airports.get(dest));
			}
			
			SimpleLinesMarker sl = new SimpleLinesMarker(route.getLocations(), route.getProperties());
		
			//System.out.println(sl.getProperties());
			
			//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
			routeList.add(sl);
		}
		
		
		
		//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
		// hide all routes as default
		for(Marker marker : routeList) {
			marker.setHidden(true);
		}
		map.addMarkers(routeList);
		
		//set the color of the airports (take long time when setting up the map)
		airportWithRoute(); 
		
		map.addMarkers(airportList);
		
	}
	
	public void draw() {
		background(0);
		map.draw();
		
	}
	
	@Override
	public void mouseMoved(){
		if(lastSelected != null){
			lastSelected.setSelected(false);
			lastSelected = null;
		}
		else{
			for (Marker m : airportList) 
			{
				CommonMarker marker = (CommonMarker)m;
				if (marker.isInside(map, mouseX, mouseY)) {
					lastSelected = marker;
					marker.setSelected(true);
					
					// print out the distance (in km) between the clicked airport and the mouse selected airport 
					// the selected airport needs to be a destination airport of the clicked airport (the selected airport is connected with the clicked airport by a route)
					if (lastClicked != null && !lastSelected.isHidden()){
						double distance = lastClicked.getDistanceTo(lastSelected.getLocation());
						String output = ((AirportMarker)lastClicked).getName()+"\t"+((AirportMarker)lastSelected).getName()+"\t"+distance;
						System.out.println(output);
					}
					return;
				}
			}
		}
			
	}
	
	@Override
	public void mouseClicked()
	{
		//reset the map
		if (lastClicked != null) {
			unhideMarkers();
			lastClicked = null;
		}
		
		else if (lastClicked == null) 
		{
			checkAirportForClick();
		}
	}
	
	//reset the map: show all the airports, hide all the routes
	private void unhideMarkers() {
		for(Marker marker : airportList) {
			marker.setHidden(false);
		}
			
		for(Marker marker : routeList) {
			marker.setHidden(true);
		}
	}
	
	private void checkAirportForClick(){
		if (lastClicked != null) return;
		// Loop over the airport markers to see if one of them is selected
		for (Marker marker : airportList) {
			if (marker.isInside(map, mouseX, mouseY)) {
				lastClicked = (AirportMarker) marker;
				AirportMarker am = (AirportMarker) marker;
				
				// Hide all the other airports
				for (Marker mhide : airportList) {
					if (mhide != am) {
						mhide.setHidden(true);
					}
				}
				
				//create the routes for the clicked airports
				am.createRoutes(routeList);
				List<SimpleLinesMarker> sourceRoutes = am.getSourceRoutes();
				if (sourceRoutes != null){
					for (Marker r: sourceRoutes) {
						//show all the sourceRoutes
						r.setHidden(false);
						
						//show the destination airport of the routes
						int dest = Integer.parseInt(((SimpleLinesMarker)r).getStringProperty("destination"));
						for(Marker destm: airportList){
							if (Integer.parseInt(destm.getId()) == dest){
								destm.setHidden(false);
								//print out all the distances between the clicked airport and the destination airports
								//double distance = am.getDistanceTo(destm.getLocation());
								//String output = am.getName()+"\t"+((AirportMarker)destm).getName()+"\t"+distance;
								//System.out.println(output);
							}
						}
					}
				}
				return;
			}
		}	
	}

	// sort the airports bases on whether they have source routes
	// the color of the airports will be red when withRoute is true; blue when withRoute is false
	private void airportWithRoute(){
		for(Marker marker: airportList){
			AirportMarker am = (AirportMarker) marker;
			am.createRoutes(routeList);
			if (am.getSourceRoutes().size() > 0){
				am.setWithRoute(true);
				// check if the destination airport and the departing airport are in the same country. 
				// if not, set International true
				for(Marker r: am.getSourceRoutes()){
					int dest = Integer.parseInt(((SimpleLinesMarker)r).getStringProperty("destination"));
					for(Marker destm: airportList){
						if (Integer.parseInt(destm.getId()) == dest){
							if(!am.getCountry().equals(((AirportMarker)destm).getCountry())){
								am.setInternational(true);
								break;
							}
						}
					}
					if(am.getInternational()) 
						break;
				}
			}
			else
				am.setWithRoute(false);
		}
	}

}
