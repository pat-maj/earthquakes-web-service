package uk.ac.mmu.advprog.hackathon;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
			ResultSet results = s.executeQuery("SELECT COUNT(*) AS count FROM earthquakes");
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
	 * Returns the number of earthquakes with at least the specified magnitude in the database
	 * @param magnitude The magnitude inputed by the user
	 * @return the number of earthquakes with at least the specified magnitude, or -1 if empty
	 */
	public int getNumberOfEarthquakes(Float magnitude) {
		int result = -1;
		try {
			String sql = "Select COUNT(*) AS Number FROM earthquakes WHERE mag >= ?;";
			PreparedStatement p = connection.prepareStatement(sql);
			p.setFloat(1, magnitude);
			ResultSet results = p.executeQuery();
					
			while(results.next()) {
				result = results.getInt(results.findColumn("Number"));
			}
		}
		catch (SQLException sqle) {
			error(sqle);
		}
		
		return result; 
	}
	
	/**
	 * Returns all earthquakes that occurred in the specified year and with at least specified magnitude in database
	 * @param magnitude The magnitude inputed by the user
	 * @param year The year inputed by the user
	 * @return earthquakes that occurred in the specified year and with at least specified magnitude
	 */
	public ResultSet getEarthquakesByYearAndMagnitude(Float magnitude, int year) { // is it better to pass year as a string
		try {
			String sql = "SELECT * FROM earthquakes WHERE time LIKE ? AND mag >= ? ORDER BY time ASC;";
			PreparedStatement p = connection.prepareStatement(sql);
			p.setString(1, "%" + Integer.toString(year) + "%");
			p.setFloat(2, magnitude);
			return p.executeQuery();
		}
		catch (SQLException sqle) {
			error(sqle);
		}
		return null; // WHAT TO RETURN????
	}
	
	/**
	 * Returns 10 closest earthquakes to the provided location, calculated based on latitude and longitude, and with
	 * at least specified magnitude from the database 
	 * @param magnitude The magnitude inputed by the user
	 * @param latitude The latitude inputed by the user
	 * @param longitude The longitude inputed by the user
	 * @return 10 closest earthquakes to the specified location with at least specified magnitude
	 */
	public ResultSet getEarthquakesByLocation(Float magnitude, Float latitude, Float longitude) {
		try {
			String sql = "SELECT * FROM earthquakes WHERE mag >= ?"
					+ "ORDER BY (((? - Latitude) * (? - Latitude)) +"
					+ "(0.595 * ((? - Longitude) * (? - Longitude))))"
					+ "ASC LIMIT 10;";
			PreparedStatement p = connection.prepareStatement(sql);
			p.setFloat(1, magnitude);
			p.setFloat(2, latitude);
			p.setFloat(3, latitude);
			p.setFloat(4, longitude);
			p.setFloat(5, longitude);
			return p.executeQuery();
		}
		catch (SQLException sqle) {
			error(sqle);
		}
		return null; //WHAT TO RETURN?
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
