package songfinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * SongData class to store information of each song.
 * 
 * @author shu16
 */
public class SongData {
	private JsonObject data;

	/**
	 * SongData Constructor
	 * 
	 * @param song
	 */
	public SongData(File song) {
		parseFile(song);
	}

	/**
	 * Parse the information of each songs
	 * 
	 * @throws IOException
	 */
	private void parseFile(File song) {
		StringBuilder raw = new StringBuilder();
		// read the file
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(song), "UTF-8")) {
			int data = reader.read();
			while (data != -1) {
				raw.append((char) data);
				data = reader.read();
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		// parse as json file
		JsonParser parser = new JsonParser();
		JsonElement elt = parser.parse(raw.toString());

		// record the information
		if (elt.isJsonObject()) {
			this.data = (JsonObject) elt;
		}
	}

	// send library the data represent the song
	public JsonObject getData() {
		return data;
	}
}
