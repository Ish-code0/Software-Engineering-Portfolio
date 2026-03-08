package uk.ac.mmu.advprog.hackathon;

// Standard JDBC imports for database connection and querying
                                  //These notes are to help my understanding and to make things clearer
import java.sql.Connection;       // Represents a DB connection
import java.sql.DriverManager;    // Used to connect to SQLite DB
import java.sql.ResultSet;        // Holds results of SQL queries
import java.sql.SQLException;     // Handles SQL errors     
import java.sql.Statement;        // For simple queries
import java.sql.PreparedStatement;// For parameterised queries (prevents SQL injection)

import java.util.List;            // Used for validating stop types
import java.util.Arrays;          // To define allowed types easily

// JSON library from starter project which is the lib folder

import org.json.JSONArray;       // Holds multiple JSON objects
import org.json.JSONObject;      // Holds individual stop details

//XML handling classes from the Java Standard Library (Java SE)
//These are not external libraries or additional JAR files because it is from Java SE
//Used to construct XML output for the /stopsnear endpoint 

import org.w3c.dom.Document;     // Represents the XML document
import org.w3c.dom.Element;      // Represents XML nodes/elements
import javax.xml.parsers.DocumentBuilder;        // To build XML
import javax.xml.parsers.DocumentBuilderFactory; // Creates document builder

/**
 * Handles all database interactions for the transport web service.
 *
 *  - Connects to the SQLite database in ./data/NaPTAN.db
 *  - Provides methods to retrieve:
 *       1. Number of entries (/test)
 *       2. Stop count by locality (/stops)
 *       3. Stop details by locality and type (/stopinfo)
 *       4. Nearest 5 stops by coordinates and type (/stopsnear)
 *  - Return JSON or XML for endpoints as required
 *
 * Notes:
 * - Does NOT handle HTTP requests or parameter validation (handled in the TransportWebService)
 */
public class DB implements AutoCloseable {

	private static final String JDBC_CONNECTION_STRING = "jdbc:sqlite:./data/NaPTAN.db";

	private Connection connection = null; // Reusable connection object

	/**
	 * Constructor — establishes database connection when a DB object is created
	 */
	public DB() {
		try {
			connection = DriverManager.getConnection(JDBC_CONNECTION_STRING);
		} catch (SQLException sqle) {
			error(sqle); // Print error and terminate if DB connection fails
		}
	}

	/**
	 * Task 0: /test endpoint
	 * Returns total number of entries in DB to confirm connectivity. (I check this first before moving forward)
	 */
	public int getNumberOfEntries() {
		int result = -1;

		try {
			Statement s = connection.createStatement();
			ResultSet results = s.executeQuery("SELECT COUNT(*) AS count FROM NaPTAN");

			if (results.next()) { // Single row result from COUNT(*)
				result = results.getInt("count");
			}
		} catch (SQLException sqle) {
			error(sqle);
		}

		return result;
	}

	/**
	 * Task 1: /stops endpoint
	 * Returns number of stops in a given locality.
	 * Uses parameterised query to prevent SQL injection.
	 * Again I check if this works accordingly before moving on.
	 */
	public int getStopCountByLocality(String locality) {
		int result = 0;

		String sql = "SELECT COUNT(*) AS count FROM NaPTAN WHERE Locality = ?";

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, locality); // Insert user input safely
			ResultSet results = ps.executeQuery();

			if (results.next()) {
				result = results.getInt("count");
			}
		} catch (SQLException sqle) {
			error(sqle);
		}

		return result;
	}

	/**
	 * Task 2: /stopinfo endpoint
	 * Returns JSON array of stops in a locality filtered by type.
	 * Returns null if type invalid.
	 * I proof check before moving forward by checking the relevant URL
	 */
	public JSONArray getStopsByLocalityAndType(String locality, String type) {
		JSONArray jsonArray = new JSONArray();

		// Allowed types per assignment spec
		List<String> validTypes = Arrays.asList("BUS","MET","RLW","FER","AIR","TXR");
		if (!validTypes.contains(type)) {
			return null; // Caller handles invalid type
		}

		String sql = "SELECT * FROM NaPTAN WHERE Locality = ? AND Type = ?";

		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, locality);
			ps.setString(2, type);

			ResultSet rs = ps.executeQuery();

			// Build JSON object for each stop
			while (rs.next()) {
				JSONObject stop = new JSONObject();
				stop.put("name", rs.getString("CommonName") != null ? rs.getString("CommonName") : "");
				stop.put("NaPTAN_code", rs.getString("NaptanCode") != null ? rs.getString("NaptanCode") : "");
				stop.put("locality", rs.getString("Locality") != null ? rs.getString("Locality") : "");
				stop.put("type", rs.getString("Type") != null ? rs.getString("Type") : "");

				JSONObject location = new JSONObject();
				location.put("Street", rs.getString("Street") != null ? rs.getString("Street") : "");
				location.put("Landmark", rs.getString("Landmark") != null ? rs.getString("Landmark") : "");
				location.put("Indicator", rs.getString("Indicator") != null ? rs.getString("Indicator") : "");
				location.put("Bearing", rs.getString("Bearing") != null ? rs.getString("Bearing") : "");

				stop.put("location", location);
				jsonArray.put(stop);
			}
		} catch (SQLException sqle) {
			error(sqle);
		}

		return jsonArray;
	}

	/**
	 * Task 3: /stopsnear endpoint
	 * Returns up to 5 nearest stops of a given type as XML.
	 * This Calculates distance by using the simple Euclidean formula. 
	 * Returns empty <stops> if no results.
	 */
	public Document getNearestStops(double latitude, double longitude, String type) {
		Document doc = null;
		List<String> validTypes = Arrays.asList("BUS","MET","RLW","FER","AIR","TXR");
		if (!validTypes.contains(type)) {
			return null;
		}

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.newDocument();

			Element root = doc.createElement("stops"); // Root element
			doc.appendChild(root);

			String sql = "SELECT *, ((Latitude - ?) * (Latitude - ?) + (Longitude - ?) * (Longitude - ?)) AS distance "
					+ "FROM NaPTAN WHERE Type = ? ORDER BY distance ASC LIMIT 5";

			try (PreparedStatement ps = connection.prepareStatement(sql)) {

				// The latitude and longitude values are inserted into the SQL query here.
				// (Latitude - inputLatitude)^2 + (Longitude - inputLongitude)^2
				ps.setDouble(1, latitude);
				ps.setDouble(2, latitude);
				ps.setDouble(3, longitude);
				ps.setDouble(4, longitude);

				// This sets the transport type (e.g. BUS, MET) to filter the database results.
				ps.setString(5, type);

				// Execute the SQL query – results are already ordered by distance
				// Also they are limited to 5 rows by the SQL statement itself.
				ResultSet rs = ps.executeQuery();

				while (rs.next()) {

					// Each loop iteration represents one nearby stop returned from the database.
					Element stopEl = doc.createElement("stop");

					
					// Null checks are used so missing database values do not break the XML output.
					Element nameEl = doc.createElement("name");
					nameEl.setTextContent(rs.getString("CommonName") != null ? rs.getString("CommonName") : "");
					stopEl.appendChild(nameEl);

					// Adds the NaPTAN code for the stop.
					Element codeEl = doc.createElement("NaPTAN_code");
					codeEl.setTextContent(rs.getString("NaptanCode") != null ? rs.getString("NaptanCode") : "");
					stopEl.appendChild(codeEl);

					// Create a <location> element to group address-related information.
					Element locEl = doc.createElement("location");

					Element streetEl = doc.createElement("Street");
					streetEl.setTextContent(rs.getString("Street") != null ? rs.getString("Street") : "");

					Element landmarkEl = doc.createElement("Landmark");
					landmarkEl.setTextContent(rs.getString("Landmark") != null ? rs.getString("Landmark") : "");

					Element indicatorEl = doc.createElement("Indicator");
					indicatorEl.setTextContent(rs.getString("Indicator") != null ? rs.getString("Indicator") : "");

					Element bearingEl = doc.createElement("Bearing");
					bearingEl.setTextContent(rs.getString("Bearing") != null ? rs.getString("Bearing") : "");

					// All location-related elements are added to the <location> parent.
					locEl.appendChild(streetEl);
					locEl.appendChild(landmarkEl);
					locEl.appendChild(indicatorEl);
					locEl.appendChild(bearingEl);

					// Attach the <location> element to the current <stop>.
					stopEl.appendChild(locEl);

					// Lastly, I add the completed <stop> element to the root <stops> element.
					root.appendChild(stopEl);
				}
			}

		// Catches any exception that may occur whilst building the XML doc.
		} catch (Exception e) { 
			e.printStackTrace(); //Prints the full error for debugging.
		}

		return doc;
	}

	@Override
	public void close() {
		try {
			//Checks if db connection is still open or not.
			if (!connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException sqle) { //If an issue occurs while closing, then it handles it consistently.
			error(sqle);
		}
	}

	private void error(SQLException sqle) {
		System.err.println("Problem Opening Database! " + sqle.getClass().getName()); //Outputs a clear error message
		sqle.printStackTrace(); //Prints the full stack trace so error is known.
		System.exit(1); //Terminates the program.
	}
}
