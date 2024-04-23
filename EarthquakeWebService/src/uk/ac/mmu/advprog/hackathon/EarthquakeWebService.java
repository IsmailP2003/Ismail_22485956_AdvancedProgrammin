package uk.ac.mmu.advprog.hackathon;
import static spark.Spark.get;
import static spark.Spark.port;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handles the setting up and starting of the web service
 * You will be adding additional routes to this class, and it might get quite large
 * You should push some of the work into additional child classes, like I did with DB
 * @author You, Mainly!
 */
public class EarthquakeWebService {
	
	/**
	 * Main program entry point, starts the web service
	 * @param args not used
	 */
	public static void main(String[] args) {		
		port(8088);	
		
		//You can check the web service is working by loading http://localhost:8088/test in your browser
		get("/test", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				try (DB db = new DB()) {
					return "Number of entries: " + db.getNumberOfEntries();
				}
			}
		});
/**
 * Used to identify number of earthquakes with minimum magnitude.
 *  allow clients to retrieve the number
of earthquakes in the database with at least the specified magnitude.
 * @returns if valid will return magnitude value or else will return invalid magnitude and will also show invalid if no magnitude
 * 
 */
		get("/quakecount", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				if (request.queryParams("magnitude") == null || request.queryParams("magnitude").isEmpty()){
					return "Invalid Magnitude";
				}
				float magnitude;

				try{
					 magnitude = Float.parseFloat(request.queryParams("magnitude"));
				} catch (NumberFormatException e) {
					return "Invalid Magnitude";
				}

				try (DB db = new DB()) {
					return db.QuakeCounter(magnitude);
				}
			}
		});

/**
 * Created a route with 2 URL parameters controlling the year in which to search and minimum magnitude.
 * @return If theres no valid year it will return Invalid Year same with magnitude.But if they valid it will display earthquakes by year and magnitude.
 * 
 */
		get("/quakesbyyear", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				if (request.queryParams("year") == null || request.queryParams("year").isEmpty()){
					return "Invalid Year";
				}
				int year;
				if (request.queryParams("magnitude") == null || request.queryParams("magnitude").isEmpty()){
					return "Invalid magnitude";
				}
				float magnitude;

				try{
					year = Integer.parseInt(request.queryParams("year"));

					magnitude = Float.parseFloat(request.queryParams("magnitude"));
				} catch (NumberFormatException e) {
					return "Invalid Year and Magnitude";

				}

				try (DB db = new DB()) {
					return db.QuakesPerYear(year,magnitude);
				}
			}
		});
/**
 * @return if is empty or no value it would output invalid for all
 * If it works it will display earthquakes by location in XML format
 */
		get ("/quakesbylocation", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				if (request.queryParams("latitude") == null || request.queryParams("latitude").isEmpty()){
					return "Invalid Latitude";
				}
				if (request.queryParams("longitude") == null || request.queryParams("longitude").isEmpty()){
					return "Invalid Longitude";
				}
				if (request.queryParams("magnitude") == null || request.queryParams("magnitude").isEmpty()){
					return "Invalid magnitude";
				}
				float latitude,longitude,magnitude;

				try{
					latitude = Float.parseFloat(request.queryParams("latitude"));
					longitude = Float.parseFloat(request.queryParams("longitude"));
					magnitude = Float.parseFloat(request.queryParams("magnitude"));
				} catch (NumberFormatException e) {
					return "Invalid Latitude, Longitude and magnitude";
				}

				try (DB db = new DB()) {
					return db.quakesbylocation(latitude,longitude,magnitude);
				}
			}
		});
		

		
		System.out.println("Web Service Started. Don't forget to kill it when done testing!");
	}
}
