package uk.ac.mmu.advprog.hackathon;
import static spark.Spark.get;
import static spark.Spark.port;

import java.io.StringWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
		
		get("/quakecount", new Route() {

			@Override
			public Object handle(Request req, Response res) throws Exception {
				try(DB db = new DB()){
					
					String sMag = req.queryParams("magnitude");
					if(sMag == null || sMag.isEmpty()) return "Invalid Magnitude";
					
					try {
						Float mag = Float.parseFloat(sMag);
						if(mag < 0) return "Invalid Magnitude";
						
						int numberOfEarthquakes = db.getNumberOfEarthquakes(mag);
						if(numberOfEarthquakes == -1) return "Invalid Magnitude";
						
						return numberOfEarthquakes;
					}
					catch (NumberFormatException e) {
						return "Invalid Magnitude";
					}
				}
			}
			
		});
		
		get("/quakesbyyear", new Route() {

			@Override
			public Object handle(Request req, Response res) throws Exception {
				try(DB db = new DB()){
					
					String sMag = req.queryParams("magnitude");
					String sYear = req.queryParams("year");
					if(sMag == null || sMag.isEmpty()) return "Invalid Magnitude";
					if(sYear == null || sYear.isEmpty()) return "Invalid Year";
					
					Float inpMag;
					try {
						inpMag = Float.parseFloat(sMag);
					}
					catch (NumberFormatException e) {
						return "Invalid Magnitude";
					}
					
					if(inpMag < 0) return "Invalid Magnitude";
					
					int inpYear;
					try {
						inpYear = Integer.parseInt(sYear);
					}
					catch (NumberFormatException e) {
						return "Invalid Year";
					}
					
					if(inpYear > LocalDate.now().getYear() || inpYear < 1900) return "Invalid Year";
					
					JSONArray root = new JSONArray();
					
					ResultSet rs = db.getEarthquakesByYearAndMagnitude(inpMag, inpYear);
					while(rs.next()) {
						JSONObject earthquake = new JSONObject();
						String time = rs.getString("time");
						Float magnitude = rs.getFloat("mag"); // Don't have to check because if null then it returns 0
						Float latitude = rs.getFloat("latitude");
						String description = rs.getString("place");
						Float longitude = rs.getFloat("longitude");
						String id = rs.getString("id");
						
						String parsedDate = getDate(time);
						String parsedTime = getTime(time);
						
						if(description == null) description = "";
						if(id == null) id = "";
						
						earthquake.put("date", parsedDate);
						earthquake.put("magnitude", magnitude);
						
						JSONObject location = new JSONObject();
						location.put("latitude", latitude);
						location.put("description", description);
						location.put("longitude", longitude);
						earthquake.put("location", location);
						
						earthquake.put("id", id);
						earthquake.put("time", parsedTime);
						
						root.put(earthquake);
					}
					
					res.type("application/json");
					return root.toString();
				}
			}
			
		});
		
		get("/quakesbylocation", new Route() {

			@Override
			public Object handle(Request req, Response res) throws Exception {
				try(DB db = new DB()){
					
					String sMag = req.queryParams("magnitude");
					String sLat = req.queryParams("latitude");
					String sLon = req.queryParams("longitude");
					if(sMag == null || sMag.isEmpty()) return "Invalid Magnitude";
					if(sLat == null || sLat.isEmpty()) return "Invalid Latitude";
					if(sLon == null || sLon.isEmpty()) return "Invalid Longitude";
					
					Float inpMag;
					try {
						inpMag = Float.parseFloat(sMag);
					}
					catch (NumberFormatException e) {
						return "Invalid Magnitude";
					}
					
					if(inpMag < 0) return "Invalid Magnitude";
					
					Float inpLat;
					try {
						inpLat = Float.parseFloat(sLat);
					}
					catch (NumberFormatException e) {
						return "Invalid Latitude";
					}
					
					Float inpLon;
					try {
						inpLon = Float.parseFloat(sLon);
					}
					catch (NumberFormatException e) {
						return "Invalid Longitude";
					}
					
					
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					Document doc = dbf.newDocumentBuilder().newDocument();
					
					Element earthquakes = doc.createElement("Earthquakes");
					doc.appendChild(earthquakes);
					
					ResultSet rs = db.getEarthquakesByLocation(inpMag, inpLat, inpLon);
					while(rs.next()) {
						String time = rs.getString("time");
						String magnitude = rs.getString("mag");
						String latitude = rs.getString("latitude");
						String description = rs.getString("place");
						String longitude = rs.getString("longitude");
						String id = rs.getString("id");
						
						String parsedDate = getDate(time);
						String parsedTime = getTime(time);
						
						if(magnitude == null) magnitude = "0";
						if(latitude == null) latitude = "0";
						if(description == null) description = "";
						if(longitude == null) longitude = "0";
						if(id == null) id = "";
						
						
						Element earthquake = doc.createElement("Earthquake");
						earthquake.setAttribute("id", id);
						Element eDate = doc.createElement("Date");
						eDate.setTextContent(parsedDate);
						earthquake.appendChild(eDate);
						
						Element eTime = doc.createElement("Time");
						eTime.setTextContent(parsedTime);
						earthquake.appendChild(eTime);
						
						Element eMagnitude = doc.createElement("Magnitude");
						eMagnitude.setTextContent(magnitude);
						earthquake.appendChild(eMagnitude);
						
						Element eLocation = doc.createElement("Location");
						Element eLatitude = doc.createElement("Latitude");
						eLatitude.setTextContent(latitude);
						eLocation.appendChild(eLatitude);
						
						Element eLongitude = doc.createElement("Longitude");
						eLongitude.setTextContent(longitude);
						eLocation.appendChild(eLongitude);
						
						Element eDescription = doc.createElement("Description");
						eDescription.setTextContent(description);
						eLocation.appendChild(eDescription);
						
						earthquake.appendChild(eLocation);
						
						earthquakes.appendChild(earthquake);
					}
					
					// Get the XML into a String
					Transformer transformer = TransformerFactory.newInstance().newTransformer();
					Writer output = new StringWriter();
					transformer.setOutputProperty(OutputKeys.INDENT, "yes");
					transformer.transform(new DOMSource(doc), new StreamResult(output));
					
					res.type("application/xml");
					return output.toString();
				}
				catch (ParserConfigurationException | TransformerException ioe) {
					return "Error creating XML: " + ioe;
				}
			}
			
		});
		
		System.out.println("Web Service Started. Don't forget to kill it when done testing!");
	}
	
	/**
	 * Gets just the date from time
	 * @param time Date in LocalDateTime format
	 * @return yyyy-MM-dd date
	 */
	public static String getDate(String time) {
		String parsedDate;
		if(time == null) {
			parsedDate = "";
		} else {
			LocalDateTime dateTime = LocalDateTime.parse(time);
			parsedDate = dateTime.toLocalDate().toString();
		}
		return parsedDate;
	}
	
	/**
	 * Gets just the time from date
	 * @param time Date in LocalDateTime format
	 * @return HH:mm:ss.zzz time
	 */
	public static String getTime(String time) {
		String parsedTime;
		if(time == null) {
			parsedTime = "";
		} else {
			LocalDateTime dateTime = LocalDateTime.parse(time);
			parsedTime = dateTime.toLocalTime().toString();
		}
		return parsedTime;
	}
}
