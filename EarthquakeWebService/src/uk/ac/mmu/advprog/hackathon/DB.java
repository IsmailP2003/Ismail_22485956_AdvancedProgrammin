package uk.ac.mmu.advprog.hackathon;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.sql.*;
import java.io.Writer;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Handles database access from within your web service
 * @author You, Mainly!
 */
public class DB implements AutoCloseable {

	//allows us to easily change the database used
	private static final String JDBC_CONNECTION_STRING = "jdbc:sqlite:./data/earthquakes.db";
	
	//allows us to re-use the connection between queries if desired
	private Connection connection = null;
	
	/**
	 * Creates an instance of the DB object and connects to the database
	 */
	public DB() {
		try {
			connection = DriverManager.getConnection(JDBC_CONNECTION_STRING);
		}
		catch (SQLException sqle) {
			error(sqle);
		}
	}
	
	/**
	 * Returns the number of entries in the database, by counting rows
	 * @return The number of entries in the database, or -1 if empty
	 */
	public int getNumberOfEntries() {
		int result = -1;
		try {
			Statement s = connection.createStatement();
			ResultSet results;
            results = s.executeQuery("SELECT COUNT(*) AS count FROM earthquakes");
            while(results.next()) { //will only execute once, because SELECT COUNT(*) returns just 1 number
				result = results.getInt(results.findColumn("count"));
			}
		}
		catch (SQLException sqle) {
			error(sqle);
			
		}
		return result;
	}
	/**
	 * Returns the number of earthquakes with minimum magnitude.
	 * Or -1 if empty
	 * @param magnitude
	 * @return
	 */
	public int QuakeCounter(float magnitude) {
		int result = -1;
		try {
			PreparedStatement s;
            s = connection.prepareStatement("SELECT COUNT(*) AS Number FROM earthquakes WHERE mag >= ?");
            s.setFloat(1, magnitude);
			ResultSet results = s.executeQuery();
			while(results.next()) {
				result = results.getInt(results.findColumn("number"));
			}
		}
		catch (SQLException sqle) {
			error(sqle);

		}
		return result;
	}
	
	/**
	 * Displays the earthquakes by year and magnitude of the earthquake
	 * @param year
	 * @param magnitude
	 * @return the earthquake and location
	 */
	public String QuakesPerYear(int year, float magnitude){
		JSONArray result = new JSONArray();
		try{
			PreparedStatement s = connection.prepareStatement("SELECT *FROM earthquakes WHERE time LIKE ? AND mag >= ? ORDER BY time ASC");
			s.setString(1,year + "%");
			s.setFloat(2,magnitude);
			ResultSet results = s.executeQuery();
			while(results.next()){
				JSONObject quake = new JSONObject();
				quake.put("id",results.getString(results.findColumn("id")));
				quake.put("magnitude",results.getFloat(results.findColumn("mag")));
				String timeString = results.getString(results.findColumn("time"));
				quake.put("time",timeString.split("T",2)[1]);
				quake.put("date",timeString.split("T",2)[0]);

				JSONObject location = new JSONObject();
				location.put("latitude",results.getFloat(results.findColumn("latitude")));
				location.put("longitude",results.getFloat(results.findColumn("longitude")));
				location.put("description",results.getString(results.findColumn("place")));
				quake.put("location",location);

				result.put(quake);
				System.out.println(quake);
				System.out.println(result);
			}
		}
		catch (SQLException sqle){
			error(sqle);
		}
		return result.toString();
	}
	/**
	 * 
	 * @param magnitude 
	 * @param latitude
	 * @param longitude
	 * @return an XML format of the date time location and magnitude of teh earthquake
	 * If it doesnt work it will output an error.
	 */
	public String quakesbylocation(float magnitude,float latitude,float longitude) {

		try {
			try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			Document doc = dbf.newDocumentBuilder().newDocument();
			Element root = doc.createElement("earthquakes");
			doc.appendChild(root);


			PreparedStatement s;
			s = connection.prepareStatement("SELECT * FROM earthquakes WHERE mag >= ? ORDER BY(((? - latitude) * (? - latitude)) + (0.595 * ((? - longitude) * (? - longitude)))) ASC LIMIT 10;");
			s.setFloat(1, magnitude);
			s.setFloat(2, latitude);
			s.setFloat(3, latitude);
			s.setFloat(4, longitude);
			s.setFloat(5, longitude);

			ResultSet results = s.executeQuery();
			while (results.next()) {

				Element location = doc.createElement("location");
				Element earthquake = doc.createElement("earthquake");
				Element magnitudes = doc.createElement("magnitude");
				magnitudes.setTextContent(results.getString(results.findColumn("mag")));
				Element latitudes = doc.createElement("latitude");
				latitudes.setTextContent(results.getString(results.findColumn("latitude")));
				Element longitudes = doc.createElement("longitude");
				longitudes.setTextContent(results.getString(results.findColumn("longitude")));
				Element descriptions = doc.createElement("description");
				descriptions.setTextContent(results.getString(results.findColumn("place")));
				Element times = doc.createElement("time");
				times.setTextContent(results.getString(results.findColumn("time")).split("T")[1]);
				Element dates = doc.createElement("date");
				dates.setTextContent(results.getString(results.findColumn("time")).split("T")[0]);
				earthquake.appendChild(magnitudes);
				location.appendChild(latitudes);
				location.appendChild(longitudes);
				location.appendChild(descriptions);
				earthquake.appendChild(location);
				earthquake.appendChild(times);
				earthquake.appendChild(dates);


				root.appendChild(earthquake);
			}
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			Writer output = new StringWriter();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(new DOMSource(doc), new StreamResult(output));
			System.out.println(output.toString());
			return output.toString();
			}
			catch(ParserConfigurationException | TransformerException e) {
				return e.getMessage();
			}

		} catch (SQLException  sqle) {
			error(sqle);
			return null;
		}
		
	}

	/**
	 * Closes the connection to the database, required by AutoCloseable interface.
	 */
	@Override
	public void close() {
		try {
			if ( !connection.isClosed() ) {
				connection.close();
			}
		}
		catch(SQLException sqle) {
			error(sqle);
		}
	}
	
	/**
	 * Prints out the details of the SQL error that has occurred, and exits the programme
	 * @param sqle Exception representing the error that occurred
	 */
	private void error(SQLException sqle) {
		System.err.println("Problem Accessing Database! " + sqle.getClass().getName());
		sqle.printStackTrace();
		System.exit(1);
	}
	
	


}
