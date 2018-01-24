package songfinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ArtistData {
	// Display specific information about that artist including the artist name,
	// number of listeners, play count, and a bio.
	private JsonObject info;
	private JsonObject data;
	
	public ArtistData(File file) {
		this.info = new JsonObject();
		this.data = new JsonObject();
		parse(file);
	}

	// parse the file to JSON to data
	private void parse(File file) {
		StringBuilder raw = new StringBuilder();
		// read the file
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
			int data = reader.read();
			while (data != -1) {
				raw.append((char) data);
				data = reader.read();
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		JsonParser parser = new JsonParser();
		JsonElement elt = parser.parse(raw.toString());
		if (elt.getAsJsonObject().get("artist") != null && ((JsonObject) elt.getAsJsonObject().get("artist")).size() > 0) {
			this.info = (JsonObject) elt.getAsJsonObject().get("artist");			
			JsonElement name = info.get("name");
			JsonObject temp = info.get("image").getAsJsonArray().get(4).getAsJsonObject();
			JsonElement img = temp.get("#text");
			temp = info.get("stats").getAsJsonObject();
			JsonElement listeners = temp.get("listeners");
			JsonElement playcount = temp.get("playcount");
			temp = info.get("bio").getAsJsonObject();
			JsonElement published = temp.get("published");
			JsonElement summary = temp.get("summary");
			JsonElement content = temp.get("content");

			JsonElement elt2 = parser.parse(String.valueOf(name.hashCode()));

			data.add("name", name);
			data.add("img", img);
			data.add("listeners", listeners);
			data.add("playcount", playcount);
			data.add("summary", summary);
			data.add("playcount", playcount);
			data.add("content", content);
			data.add("published", published);
			data.add("id", elt2);
		}
	}

	public JsonObject getData() {		
		return data;
	}
}