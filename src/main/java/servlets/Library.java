package servlets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import songfinder.ArtistsBuilder;
import songfinder.ArtistsLibrary;
import songfinder.LibraryBuilder;
import songfinder.SongsLibrary;

public class Library {
	private static SongsLibrary songsLib;
	private static ArtistsLibrary artistsLib;
	private static final String APIKEY = "f44b253dc0ce2c849354d7b4fe67bc59";

	public Library() {
		// get song library and artist library
		LibraryBuilder songbuilder = new LibraryBuilder("input/lastfm_subset", 10);
		songbuilder.loadSongData();
		Library.songsLib = songbuilder.getSongsLibrary();
		ArtistsBuilder artistBuilder = new ArtistsBuilder(songsLib.getArtistsSet());
		artistBuilder.loadArtistData();
		Library.artistsLib = artistBuilder.getArtistsLibrary();
	}

	//get JsonObject of artist by id
	public static JsonObject getArtist(String id) {
		return artistsLib.getCopy(id);
	}

	//get JsonObject of song by id
	public static JsonObject getSong(String id) {
		return songsLib.getCopy(id);
	}

	//add artist to library, admin function
	public String addArtist(String name) {
		String res;
		if (songsLib.getLowerToReal().containsKey(name)) {
			res = "The song already exist";
		} else {
			String artistUrl = "/2.0/?method=artist.getInfo" + "&api_key=" + APIKEY + "&artist=" + name.toLowerCase()
					+ "&format=json";
			String artistPage = HTTPFetcher.download("ws.audioscrobbler.com", artistUrl);
			int id = artistPage.indexOf("{");
			if (id == -1 || artistPage.length() < 60) {
				res = "invalid name";
			} else {
				JsonParser parser = new JsonParser();
				JsonObject info = parser.parse(artistPage.substring(id)).getAsJsonObject().get("artist")
						.getAsJsonObject();
				JsonObject artist = new JsonObject();
				JsonElement artistName = info.get("name");
				JsonObject temp = info.get("image").getAsJsonArray().get(4).getAsJsonObject();
				JsonElement img = temp.get("#text");
				temp = info.get("stats").getAsJsonObject();
				JsonElement listeners = temp.get("listeners");
				JsonElement playcount = temp.get("playcount");
				temp = info.get("bio").getAsJsonObject();
				JsonElement published = temp.get("published");
				JsonElement summary = temp.get("summary");
				JsonElement content = temp.get("content");
				JsonElement elt = parser.parse(String.valueOf(artistName.hashCode()));
				artist.add("name", artistName);
				artist.add("img", img);
				artist.add("listeners", listeners);
				artist.add("playcount", playcount);
				artist.add("summary", summary);
				artist.add("playcount", playcount);
				artist.add("content", content);
				artist.add("published", published);
				artist.add("id", elt);
				artistsLib.addArtist(artist);
				res = "the artist is successfully added";
			}
		}

		return res;
	}

	// get the artists library ordered by name
	public Map<String, JsonObject> getArtistByName() {
		Map<String, JsonObject> map = artistsLib.getByName();
		return map;
	}

	// get the artists library ordered by playcount
	public Map<Integer, Set<JsonObject>> getArtistByPlaycount() {
		return artistsLib.getByPlaycount();
	}

	// get the id map of each artist
	public Map<Integer, JsonObject> getIdMap() {
		Map<Integer, JsonObject> map = artistsLib.getById();
		return map;
	}

	// return the search list
	public List<String> contentList(String search, String type) {
		List<String> res = new ArrayList<>();
		if (search == null || search.trim().length() == 0)
			return res;

		// case and partial match process
		List<String> searchList = partialMath(search.toLowerCase().trim(), type);
		if (searchList.size() == 0) {
			res.add("<h3 style=\"display:block; text-align:center\">Sorry, there is nothing similar</h3>");
			return res;
		}

		Map<String, JsonArray> jsarrList = new TreeMap<String, JsonArray>();
		for (String name : searchList) {
			jsarrList.put(name, songsLib.getJsonSearch(name, type));
		}

		if (jsarrList.size() == 0) {
			res.add("<h3 style=\"display:block; text-align:center\">Sorry, there is nothing similar</h3>");
			return res;
		}

		StringBuilder builder = new StringBuilder();
		builder.append("<h4 style=\"width:74%\">Key Word : ");
		for (Entry<String, JsonArray> entry : jsarrList.entrySet()) {
			if(type.equals("tag")) 	builder.append(entry.getKey() + "; ");
			else {
				String real = songsLib.getLowerToReal().get(entry.getKey().toLowerCase());
				builder.append(real+ "; ");
			}
		}
		builder.append("</h4><table style=\"width:85%\"><tr>"
		+ "<th style=\"width:35%\">Artist</th>"
		+ "<th style=\"width:45%\">Song Title</th>"
		+ "<th style=\"width:20%\">More Info</th></tr>");

		res.add(builder.toString());
		for (Entry<String, JsonArray> entry : jsarrList.entrySet()) {
			if (entry.getValue() == null || entry.getValue().size() == 0)
				continue;
			for (JsonElement a : entry.getValue()) {
				res.add(processJson(a.getAsJsonObject()));
			}
			builder.append("</table>");
		}
		if (res.size() == 1) {
			res.remove(0);
			res.add("<h3 style=\"display:block; text-align:center\">Sorry, there is nothing similar</h3>");
		}
		return res;

	}

	// process the json file
	private String processJson(JsonObject cur) {
		StringBuilder builder = new StringBuilder();
		String artist = cur.get("artist").getAsString();
		String title = cur.get("title").getAsString();
		String trackID = cur.get("trackId").getAsString();
		builder.append("<tr>" + "<td>" + artist
				+ "<button style=\"color: white; background:transparent; padding:0px; border-radius:15%\"  name = \"likeArtist\" value = \""
				+ artist.hashCode() + "\" class=\"fa fa-heart\"></button></td>" + "<td>" + title
				+ "<button style=\"color: white; background:transparent; padding:0px; border-radius:15%\" name = \"likeSong\" value = \""
				+ cur.get("trackId").getAsString() + "\" class=\"fa fa-heart\"></button></td>"
				+ "<td><button name =\"title\" value = \"" + trackID + "\" class=\"fa fa-id-card-o\"></button></td></tr>");
		return builder.toString();
	}

	// find the nearest key word in library
	private List<String> partialMath(String search, String type) {
		Set<String> nameMap = new HashSet<String>();
		List<String> res = new ArrayList<String>();
		if (type.equals("title")) {
			nameMap = songsLib.getSongNames();
		} else if (type.equals("artist")) {
			nameMap = artistsLib.getArtistNames();
		} else if (type.equals("tag")) {
			nameMap = songsLib.getTagNames();
		}
		if (nameMap.contains(search)) {
			res.add(search);
		} else {
			for (String name : nameMap) {
				if (name.contains(search)) {
					res.add(name);
				}
			}
		}
		return res;
	}

	// return title information
	public String getTitleInfo(String title) {
		StringBuilder sb = new StringBuilder();
		JsonArray similars = songsLib.getJsonSearch(title.toLowerCase(), "title");
		sb.append("<h3>Smilar Songs List:</h3>");
		if (similars.size() != 0) {
			sb.append(
					"<table style=\"width:50%\"><tr><th style=\"width:50%\">Artist</th><th style=\"width:50%\">Song Title</th></tr>");
			for (JsonElement a : similars) {
				JsonObject obj = a.getAsJsonObject();
				String artist = obj.get("artist").getAsString();
				String track = obj.get("title").getAsString();
				sb.append("<tr>" + "<td style=\"width:50%\">" + artist + "</td>" + "<td style=\"width:50%\">" + track
						+ "</td>" + "</tr>");
			}
		} else {
			sb.append("<h4 style=\"display:block\">Sorry, there is nothing similar</h4>");
		}
		return sb.toString();
	}

	//get lastfm top list result
	public String getTopLastfm() {
		StringBuilder sb = new StringBuilder();
		String artistUrl = "/2.0/?method=chart.gettopartists&limit=5&api_key=" + APIKEY + "&format=json";
		String artistPage = HTTPFetcher.download("ws.audioscrobbler.com", artistUrl);
		String trackUrl = "/2.0/?method=chart.gettoptracks&limit=5&api_key=" + APIKEY + "&format=json";
		String trackPage = HTTPFetcher.download("ws.audioscrobbler.com", trackUrl);
		sb.append("<div>");
		sb.append(getTopList(artistPage, "artist"));
		sb.append(getTopList(trackPage, "track"));
		sb.append("</div>");
		sb.append("<hr />");

		return sb.toString();
	}

	//helper function to get table
	private String getTopList(String raw, String type) {
		int id = raw.indexOf("{");
		if (id == -1)
			return "";
		StringBuilder sb = new StringBuilder();
		JsonParser parser = new JsonParser();
		JsonElement elt = parser.parse(raw.substring(id).toString());
		JsonArray arr = (JsonArray) elt.getAsJsonObject().get(type + "s").getAsJsonObject().get(type).getAsJsonArray();
		sb.append("<h2>Most Popular " + type.substring(0, 1).toUpperCase() + type.substring(1) + "s</h2>"
				+ "<table style=\"width:50%\"><tr><th style=\"width:50%\">Name</th><th style=\"width:50%\">Listeners</th></tr>");
		for (JsonElement one : arr) {
			sb.append("<tr><td style=\"width:50%\">" + one.getAsJsonObject().get("name").getAsString() + "</td>"
					+ "<td style=\"width:50%\">" + one.getAsJsonObject().get("listeners").getAsInt() + "</td></tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}

	//get lastfm top list result according geo
	public String getTopGeoLastfm(String geo) {
		StringBuilder sb = new StringBuilder();
		String artistUrl = "/2.0/?method=geo.gettopartists&limit=5&country=" + geo.toLowerCase() + "&api_key=" + APIKEY
				+ "&format=json";
		String trackUrl = "/2.0/?method=geo.gettoptracks&limit=5&country=" + geo.toLowerCase() + "&api_key=" + APIKEY
				+ "&format=json";
		String artistPage = HTTPFetcher.download("ws.audioscrobbler.com", artistUrl);
		String trackPage = HTTPFetcher.download("ws.audioscrobbler.com", trackUrl);
		int id = artistPage.indexOf("{");
		if (id == -1)
			return "<h3>wrong input</h3>";
		String tmp = artistPage.substring(id);
		if (tmp.length() < 60)
			return "<h3>wrong input</h3>";
		sb.append(getTopGeoList(artistPage, "artist", geo));
		sb.append(getTopGeoList(trackPage, "track", geo));
		return sb.toString();
	}

	//helper function to get geo list
	private String getTopGeoList(String raw, String type, String geo) {
		StringBuilder sb = new StringBuilder();
		JsonParser parser = new JsonParser();
		int id = raw.indexOf("{");
		String res = raw.substring(id);
		JsonElement elt = parser.parse(res);
		JsonArray arr;
		if (type.equals("artist")) {
			arr = (JsonArray) elt.getAsJsonObject().get("top" + type + "s").getAsJsonObject().get(type)
					.getAsJsonArray();
		} else {
			arr = (JsonArray) elt.getAsJsonObject().get(type + "s").getAsJsonObject().get(type).getAsJsonArray();
		}
		sb.append("<h2>Most Popular " + type.substring(0, 1).toUpperCase() + type.substring(1) + "s in " + geo + "</h2>"
				+ "<table style=\"width:50%\"><tr><th style=\"width:50%\">Name</th><th style=\"width:50%\">Listeners</th></tr>");
		for (JsonElement one : arr) {
			sb.append("<tr><td style=\"width:50%\">" + one.getAsJsonObject().get("name").getAsString() + "</td>"
					+ "<td style=\"width:50%\">" + one.getAsJsonObject().get("listeners").getAsInt() + "</td></tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}

	//return list of artist by name
	public List<String> getArtistListbyName() {
		Map<String, JsonObject> map = getArtistByName();
		List<String> res = new ArrayList<>();
		for (Entry<String, JsonObject> artist : map.entrySet()) {
			StringBuilder builder = new StringBuilder();
			builder.append("<tr>" + "<td>" + artist.getKey() + "</td>" + "<td>"
					+ artist.getValue().get("playcount").getAsInt() + "</td>"
					+ "<td><a name =\"info\" href='info?artist=" + artist.getValue().get("id").getAsInt()
					+ "'><p>info</p></a></td>" + "<td><button name = \"like\" value = \""
					+ artist.getValue().get("id").getAsInt() + "\" class=\"fa fa-heart\"></button></td>" + "</tr>");
			res.add(builder.toString());
		}
		return res;
	}

	//return list of artist by playcount
	public List<String> getArtistListbyPlaycount() {
		Map<Integer, Set<JsonObject>> map = getArtistByPlaycount();
		List<String> res = new ArrayList<>();
		for (Entry<Integer, Set<JsonObject>> entry : map.entrySet()) {
			StringBuilder builder = new StringBuilder();
			for (JsonObject artist : entry.getValue()) {
				builder.append("<tr>" + "<td>" + artist.get("name").getAsString() + "</td>" + "<td>"
						+ artist.get("playcount").getAsInt() + "</td>" + "<td><a name =\"info\" href='info?artist="
						+ artist.get("id").getAsInt() + "'><p>info</p></a></td>"
						+ "<td><button name = \"like\" value = \"" + artist.get("id").getAsInt()
						+ "\" class=\"fa fa-heart\"></button></td>" + "</tr>");
				res.add(builder.toString());
			}
		}
		return res;
	}
}