package wearables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/*

The web front end is at http://wearables-emstone.apps.unc.edu/ it just
shows whats in the database, no other interaction is available.

A GET request to
http://wearables-emstone.apps.unc.edu/wearables/api/v2/datapoints will
return a json of all the points in the database

A POST to the same address with a json body like:

{
    "sensor_id":  "accel_X",
    "sensor_value":  18,
    "sensor_timestamp":  15349,
    "device_id": "Eric's Bean"
}

will add a data point.

All the write requests MUST have the header "Content-Type:
application/json" to work

 */

public class WearablesRestApi {

	private static String endpointUrl = "https://wearables-emstone.apps.unc.edu/wearables/api/v2/datapoints";
	
	public static void main(String[] args) {
		writeValues("BeanTest", "accel_Z", 0, 932482098);
	}

	public static void writeValues(String deviceID, String sensorID, int sensorValue, long sensorTimestamp) {

		try {
			URL url = new URL(endpointUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");

			String input = "{" + "\"sensor_id\":\"" + sensorID + "\"," + "\"sensor_value\":" + sensorValue + ","
					+ "\"sensor_timestamp\":" + sensorTimestamp + "," + "\"device_id\":\"" + deviceID + "\"" + "}";

			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
				throw new RuntimeException(
						"\nFailed : HTTP error code : " + conn.getResponseCode() + " " + conn.getResponseMessage());
			}

			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}

	}

	public static void readValues() {

		try {

			URL url = new URL(endpointUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException(
						"Failed : HTTP error code : " + conn.getResponseCode() + " " + conn.getResponseMessage());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}

	}

}
