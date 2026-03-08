package uk.ac.mmu.advprog.hackathon;

// Javalin framework imports 
import io.javalin.Javalin;    // Main Javalin class to create and start the server
import io.javalin.http.Context;  // Represents an HTTP request and response
import io.javalin.http.Handler;  // Interface for handling routes/endpoints

// JSON imports from lib folder 
import org.json.JSONArray; // Used to store a list of stops for /stopinfo

// Standard Java imports
import java.util.List;
import java.util.Arrays;

// XML transformation imports — needed to return /stopsnear results in XML
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/**
 * Main web service class 
 * - Create and start a Javalin web server
 * - Define four endpoints:
 *   1. /test        → DB connectivity check
 *   2. /stops       → number of stops in a locality (plain text)
 *   3. /stopinfo    → detailed stop information (JSON)
 *   4. /stopsnear   → nearest 5 stops (XML)
 *
 * Database queries are handled in DB.java; this class handles routing and parameter validation.
 */
public class TransportWebService {

	public static void main(String[] args) {

		// Create the Javalin server instance
		Javalin app = Javalin.create();

		// -------------------------
		// 1. /test endpoint
		// Purpose: confirming the database connectivity (This has to work so I can move on with further tests)
		// -------------------------
		app.get("/test", new Handler() {
			@Override
			public void handle(Context ctx) throws Exception {
				try (DB db = new DB()) { // Auto-close DB connection
					ctx.result("Number of Entries: " + db.getNumberOfEntries());
				}
			}
		});

		// -------------------------
		// 2. /stops endpoint
		// Purpose: return number of stops in a given locality (plain text)
		// Query parameter: locality
		// -------------------------
		app.get("/stops", new Handler() {
			@Override
			public void handle(Context ctx) throws Exception {
				String locality = ctx.queryParam("locality");

				// Validate query parameter
				if (locality == null || locality.isBlank()) {
					ctx.result("Invalid Request");
					return;
				}

				try (DB db = new DB()) {
					int count = db.getStopCountByLocality(locality);
					ctx.result(String.valueOf(count)); // Plain text response
				}
			}
		});

		// -------------------------
		// 3. /stopinfo endpoint
		// Purpose: return detailed stop info for a locality and type (JSON)
		// Query parameters: locality, type
		// -------------------------
		app.get("/stopinfo", new Handler() {
			@Override
			public void handle(Context ctx) throws Exception {
				String locality = ctx.queryParam("locality");
				String type = ctx.queryParam("type");

				// Validate both parameters
				if (locality == null || locality.isBlank() || type == null || type.isBlank()) {
					ctx.result("Invalid Request");
					return;
				}

				try (DB db = new DB()) {
					JSONArray stops = db.getStopsByLocalityAndType(locality, type);

					if (stops == null) {
						// Type invalid
						ctx.result("Invalid Request");
					} else {
						ctx.contentType("application/json"); // Set header for JSON
						ctx.result(stops.toString());
					}
				}
			}
		});

		// -------------------------
		// 4. /stopsnear endpoint
		// Purpose: return up to 5 nearest stops by coordinates (XML) (Trickiest part for me)
		// Query parameters: latitude, longitude, type
		// -------------------------
		app.get("/stopsnear", new Handler() {
			@Override
			public void handle(Context ctx) throws Exception {
				String type = ctx.queryParam("type");
				String latStr = ctx.queryParam("latitude");
				String lonStr = ctx.queryParam("longitude");

				// Allowed types from looking at the assignment brief
				List<String> validTypes = Arrays.asList("BUS","MET","RLW","FER","AIR","TXR");

				// Validate type parameter
				if (type == null || type.isBlank() || !validTypes.contains(type)) {
					ctx.result("Invalid Type");
					return;
				}

				// Validate latitude parameter
				double latitude;
				try {
					latitude = Double.parseDouble(latStr);
				} catch (Exception e) {
					ctx.result("Invalid Latitude");
					return;
				}

				// Validate longitude parameter
				double longitude;
				try {
					longitude = Double.parseDouble(lonStr);
				} catch (Exception e) {
					ctx.result("Invalid Longitude");
					return;
				}

				// Query DB for nearest stops
				try (DB db = new DB()) {
					org.w3c.dom.Document xmlDoc = db.getNearestStops(latitude, longitude, type);

					if (xmlDoc == null) {
						ctx.result("Invalid Type"); // Extra safety
					} else {
						// Transform DOM XML to string for HTTP response
						TransformerFactory tf = TransformerFactory.newInstance();
						Transformer transformer = tf.newTransformer();
						StringWriter writer = new StringWriter();
						transformer.transform(new DOMSource(xmlDoc), new StreamResult(writer));

						ctx.contentType("application/xml"); // Set header for XML
						ctx.result(writer.toString());
					}
				}
			}
		});

		// -------------------------
		// Start server
		// -------------------------
		app.start(8088); 
		System.out.println("Server up! Don't forget to kill the program when done!");
	}
}
