package songfinder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import utility.ReentrantLock;

import java.util.TreeSet;

/**
 * LibraryData class to store information of all songs.
 * 
 * @author shu16
 */
public class SongsLibrary {
	private ReentrantLock rwl;
	private Map<String, JsonObject> songs;
	private Map<String, TreeSet<JsonObject>> tagMap;
	private Map<String, TreeSet<JsonObject>> artistMap;
	private Set<String> artists;
	private Map<String, TreeSet<JsonObject>> titleMap;
	private Map<String, String> lowerToReal;
	private Set<String> songNames;
	private Set<String> tagNames;

	/**
	 * LibraryData Constructor
	 * 
	 * @param inPath,
	 *            outPath, order
	 */
	public SongsLibrary() {
		this.rwl = new ReentrantLock();
		this.songs = new HashMap<>();
		this.artistMap = new TreeMap<>();
		this.titleMap = new TreeMap<>();
		this.tagMap = new TreeMap<>();
		this.lowerToReal = new HashMap<>();
		this.artists = new TreeSet<>();
		this.tagNames = new HashSet<>();
		this.songNames = new HashSet<>();
	}

	public JsonObject getCopy(String id) {
		rwl.lockRead();
		JsonObject song = clone(songs.get(id));
		rwl.unlockRead();
		return song;
	}

	/**
	 * Return String contains information of songs ordered by tag
	 * 
	 * @return
	 */
	private String tagOutPut() {
		StringBuilder ret = new StringBuilder();
		for (Entry<String, TreeSet<JsonObject>> song : tagMap.entrySet()) {
			ret.append(song.getKey() + ": ");
			for (JsonObject one : song.getValue()) {
				ret.append(one.get("track_id").getAsString() + " ");
			}
			ret.append("\n");
		}
		String outPutTag = ret.toString();
		return outPutTag;
	}

	/**
	 * Return String contains information of songs ordered by title
	 * 
	 * @return
	 */
	private String titleOutPut() {
		StringBuilder ret = new StringBuilder();
		for (Entry<String, TreeSet<JsonObject>> pair : titleMap.entrySet()) {
			TreeSet<JsonObject> songs = pair.getValue();
			for (JsonObject song : songs) {
				ret.append(song.get("artist").getAsString() + " - " + pair.getKey() + "\n");
			}
		}
		String outPutTitle = ret.toString();
		return outPutTitle;
	}

	/**
	 * Return String contains information of songs ordered by artist
	 * 
	 * @return
	 */
	private String artistOutPut() {
		StringBuilder ret = new StringBuilder();
		for (Entry<String, TreeSet<JsonObject>> pair : artistMap.entrySet()) {
			TreeSet<JsonObject> songs = pair.getValue();
			for (JsonObject song : songs) {
				ret.append(pair.getKey() + " - " + song.get("title").getAsString() + "\n");
			}
		}
		String outPutArtist = ret.toString();
		return outPutArtist;
	}

	/**
	 * add song into library
	 */
	public void addSong(JsonObject song) {
		this.rwl.lockWrite();
		addSongsByArtist(song);
		addSongsByTitle(song);
		addSongsByTag(song);
		addSongsByID(song);
		addSongsByLowerCase(song);
		this.rwl.unlockWrite();
	}

	// helper methods for add method to add song into all kinds of data structure
	private void addSongsByTag(JsonObject song) {
		for (JsonElement tag : song.get("tags").getAsJsonArray()) {
			String name = tag.getAsJsonArray().get(0).getAsString();
			if (tagMap.containsKey(name)) {
				tagMap.get(name).add(song);
			} else {
				TreeSet<JsonObject> temp = new TreeSet<>(new TagComparator());
				tagMap.put(name, temp);
				temp.add(song);
				tagNames.add(name.toLowerCase());
			}
		}
	}

	private void addSongsByArtist(JsonObject song) {
		String artist = song.get("artist").getAsString();
		if (artistMap.containsKey(artist)) {
			artistMap.get(artist).add(song);
		} else {
			TreeSet<JsonObject> cur = new TreeSet<JsonObject>(new ArtistComparator());
			cur.add(song);
			artistMap.put(artist, cur);
			artists.add(song.get("artist").getAsString());
		}
	}

	private void addSongsByTitle(JsonObject song) {
		String title = song.get("title").getAsString();
		if (titleMap.containsKey(title)) {
			titleMap.get(title).add(song);
		} else {
			songNames.add(song.get("title").getAsString().toLowerCase());
			TreeSet<JsonObject> cur = new TreeSet<JsonObject>(new TitleComparator());
			cur.add(song);
			titleMap.put(title, cur);
		}
	}

	private void addSongsByID(JsonObject song) {
		songs.put(song.get("track_id").getAsString(), song);
	}

	private void addSongsByLowerCase(JsonObject song) {
		lowerToReal.put(song.get("artist").getAsString().toLowerCase(), song.get("artist").getAsString());
		lowerToReal.put(song.get("title").getAsString().toLowerCase(), song.get("title").getAsString());
	}

	/**
	 * Write to file according to order return true if write successfully otherwise
	 * false
	 * 
	 * @return
	 * @throws UnsupportedEncodingException,
	 *             FileNotFoundException
	 */
	public void outputFile(String outPut, String order) {
		this.rwl.lockRead();
		// make the folder if path not exist
		Path outPutPath = Paths.get(outPut);
		outPutPath.getParent().toFile().mkdirs();
		// write the file
		try (Writer output = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(outPutPath.toString()), "UTF-8"))) {
			if (order.equals("title")) {
				output.write(this.titleOutPut());
			} else if (order.equals("artist")) {
				output.write(this.artistOutPut());
			} else if (order.equals("tag")) {
				output.write(this.tagOutPut());
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		this.rwl.unlockRead();
	}

	// get methods to get different kinds of library data
	public Set<String> getTagNames() {
		rwl.lockRead();
		Set<String> newone = new HashSet<>();
		for (String str : tagNames) {
			newone.add(str);
		}
		rwl.unlockRead();
		return newone;
	}

	// return deep copy of song title
	public Set<String> getSongNames() {
		rwl.lockRead();
		Set<String> newone = new HashSet<>();
		for (String str : songNames) {
			newone.add(str);
		}
		rwl.unlockRead();
		return newone;
	}

	// return deep copy of map of songs to outside
	public Map<String, JsonObject> getSongs() {
		rwl.lockRead();
		Map<String, JsonObject> newone = new HashMap<>();
		for (Entry<String, JsonObject> entry : songs.entrySet()) {
			newone.put(entry.getKey(), clone(entry.getValue()));
		}
		rwl.unlockRead();
		return newone;
	}

	// map from all lowercase name to real name
	public Map<String, String> getLowerToReal() {
		rwl.lockRead();
		Map<String, String> newone = new HashMap<>();
		for (Entry<String, String> entry : lowerToReal.entrySet()) {
			newone.put(entry.getKey(), entry.getValue());
		}
		rwl.unlockRead();
		return newone;
	}

	// return artist set for names
	public Set<String> getArtistsSet() {
		rwl.lockRead();
		Set<String> newone = new TreeSet<>();
		for (String str : artists) {
			newone.add(str);
		}
		rwl.unlockRead();
		return newone;
	}

	// clone method to deep copy
	private JsonObject clone(JsonObject jobj) {
		JsonObject song = new JsonObject();
		song.addProperty("artist", jobj.get("artist").getAsString());
		song.addProperty("title", jobj.get("title").getAsString());
		song.addProperty("track_id", jobj.get("track_id").getAsString());
		JsonArray similars = new JsonArray();
		for (JsonElement elt : jobj.get("similars").getAsJsonArray()) {
			similars.add(elt.getAsJsonArray().get(0).getAsString());
		}
		song.add("similars", similars);

		return song;
	}

	// search method for outside to search the lib
	public void search(String input, String output) {
		rwl.lockRead();
		SearchSongs mySearch = new SearchSongs(input, output);
		mySearch.outputFile();
		rwl.unlockRead();
	}

	public JsonArray getJsonSearch(String searchKey, String order) {
		rwl.lockRead();
		SearchSongs mySearch = new SearchSongs();
		JsonArray res = mySearch.getJsonSearch(searchKey, order);
		rwl.unlockRead();
		return res;
	}

	//inner search class
	class SearchSongs {
		private JsonArray artists;
		private JsonArray titles;
		private JsonArray tags;
		private String searchInput;
		private String searchOutput;
		private String searchKey;	

		public SearchSongs(String searchInput, String searchOutput) {
			this.searchInput = searchInput;
			this.searchOutput = searchOutput;
			parseFile();
		}

		public SearchSongs() {
		}

		/**
		 * helper function for constructor to phase file;
		 */
		private void parseFile() {
			StringBuilder raw = new StringBuilder();
			File arg = Paths.get(searchInput).toFile();
			// scan the file
			try (InputStreamReader reader = new InputStreamReader(new FileInputStream(arg), "UTF-8")) {
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
				JsonObject obj = (JsonObject) elt;
				if (obj.has("searchByArtist")) {
					artists = obj.get("searchByArtist").getAsJsonArray();
				}
				if (obj.has("searchByTitle")) {
					titles = obj.get("searchByTitle").getAsJsonArray();
				}
				if (obj.has("searchByTag")) {
					tags = obj.get("searchByTag").getAsJsonArray();
				}
			}
		}

		private Map<String, Set<JsonObject>> getArtistResult() {
			Map<String, Set<JsonObject>> result = new HashMap<>();			
			if (artists != null) {			
				for (JsonElement artist : artists) {
					Set<JsonObject> one = new TreeSet<JsonObject>(new SongComparator());
					getSimilar(one, artistMap, artist.getAsString());					
					result.put(artist.getAsString(), one);
				}
			} else {
				Set<JsonObject> one = new TreeSet<JsonObject>(new SongComparator());
				getSimilar(one, artistMap, searchKey);
				result.put(searchKey, one);
			}
			return result;
		}

		/**
		 * @return search result about title
		 */
		private Map<String, Set<JsonObject>> getTitleResult() {
			Map<String, Set<JsonObject>> result = new HashMap<>();
			if (titles != null) {
				for (JsonElement title : titles) {
					Set<JsonObject> one = new TreeSet<JsonObject>(new SongComparator());
					getSimilar(one, titleMap, title.getAsString());
					result.put(title.getAsString(), one);
				}
			} else {
				Set<JsonObject> one = new TreeSet<JsonObject>(new SongComparator());
				getSimilar(one, titleMap, searchKey);
				result.put(searchKey, one);
			}
			return result;
		}

		/**
		 * add song into similar list
		 * deep copy!!!!!
		 */
		private void getSimilar(Set<JsonObject> result, Map<String, TreeSet<JsonObject>> orderMap,	String arg) {			
			if (arg == null)
				return;
			Set<JsonObject> set = orderMap.get(arg);
			if (set == null)
				return;			
			for (JsonObject song : set) {
				JsonArray similarSongs = new JsonArray();
				similarSongs = song.get("similars").getAsJsonArray();
				for (JsonElement similar : similarSongs) {
					String cur = similar.getAsJsonArray().get(0).getAsString();
					if (songs.containsKey(cur)) {	
						//deep copy!!!!!
						result.add(clone(songs.get(cur)));
					}
				}
			}
		}

		/**
		 * @return search result about tag
		 * deep copy!!!!!
		 */
		private Map<String, Set<JsonObject>> getTagResult() {
			Map<String, Set<JsonObject>> result = new HashMap<String, Set<JsonObject>>();
			if (tags != null) {
				for (JsonElement tag : tags) {
					Set<JsonObject> one = new TreeSet<JsonObject>(new SongComparator());
					for (JsonObject song : tagMap.get(tag.getAsString())) {
						//deep copy!!!!!
						one.add(clone(song));
					}
					result.put(tag.getAsString(), one);
				}
			} else {
				Set<JsonObject> one = new TreeSet<JsonObject>(new SongComparator());
				if (!tagMap.containsKey(searchKey) || tagMap.get(searchKey) == null)
					return null;
				for (JsonObject song : tagMap.get(searchKey)) {
					//deep copy!!!!!
					one.add(clone(song));
				}
				result.put(searchKey, one);
			}
			return result;
		}

		/**
		 * process data into json format
		 */
		public JsonArray toJson(Map<String, Set<JsonObject>> songs, String name, JsonArray elements) {			
			JsonArray arr = new JsonArray();
			for (JsonElement element : elements) {
				JsonObject index = new JsonObject();
				index.add(name, element);
				JsonArray similarList = new JsonArray();
				index.add("similars", similarList);
				for (JsonObject song : songs.get(element.getAsString())) {
					JsonObject one = new JsonObject();
					one.addProperty("artist", song.get("artist").getAsString());
					one.addProperty("trackId", song.get("track_id").getAsString());
					one.addProperty("title", song.get("title").getAsString());
					similarList.add(one);
				}
				/// System.out.println(index);
				arr.add(index);
			}
			return arr;
		}

		/**
		 * @return search result in jsonobject
		 */
		public JsonObject getJsonFile() {
			JsonObject raw = new JsonObject();
			if (artists != null)
				raw.add("searchByArtist", toJson(getArtistResult(), "artist", artists));
			if (titles != null)
				raw.add("searchByTitle", toJson(getTitleResult(), "title", titles));
			if (tags != null)
				raw.add("searchByTag", toJson(getTagResult(), "tag", tags));
			return raw;
		}

		/**
		 * output the search result into file
		 */
		public void outputFile() {
			// make the folder if path not exist
			Path outPutPath = Paths.get(searchOutput);
			outPutPath.getParent().toFile().mkdirs();
			// write the file
			JsonObject raw = getJsonFile();
			try (Writer output = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(outPutPath.toString()), "UTF-8"))) {
				output.write(raw.toString());
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}

		/**
		 * @return search result in Json array
		 */
		public JsonArray getJsonSearch(String searchKey, String order) {
			JsonArray raw = new JsonArray();
			if (order.equals("artist")) {
				this.searchKey = lowerToReal.get(searchKey.toLowerCase());
				raw = toWebSearch(getArtistResult());
			} else if (order.equals("title")) {
				this.searchKey = lowerToReal.get(searchKey.toLowerCase());
				raw = toWebSearch(getTitleResult());
			} else {
				this.searchKey = searchKey.toLowerCase();
				raw = toWebSearch(getTagResult());
			}
			return raw;
		}

		/**
		 * return the search result for website search;
		 */
		public JsonArray toWebSearch(Map<String, Set<JsonObject>> songs) {
			if (songs == null) {
				return null;
			}
			JsonArray similarList = new JsonArray();
			for (JsonObject song : songs.get(searchKey)) {
				JsonObject one = new JsonObject();
				one.addProperty("artist", song.get("artist").getAsString());
				one.addProperty("trackId", song.get("track_id").getAsString());
				one.addProperty("title", song.get("title").getAsString());
				similarList.add(one);
			}
			return similarList;
		}

		// clone method to deep copy
		private JsonObject clone(JsonObject jobj) {
			JsonObject song = new JsonObject();
			song.addProperty("artist", jobj.get("artist").getAsString());
			song.addProperty("title", jobj.get("title").getAsString());
			song.addProperty("track_id", jobj.get("track_id").getAsString());
			JsonArray similars = new JsonArray();
			for (JsonElement elt : jobj.get("similars").getAsJsonArray()) {
				similars.add(elt.getAsJsonArray().get(0).getAsString());
			}
			song.add("similars", similars);
			return song;
		}
	}
	

	/**
	 * Comparator to compare song by id
	 */
	class SongComparator implements Comparator<JsonObject> {
		@Override
		public int compare(JsonObject s1, JsonObject s2) {
			return s1.get("track_id").getAsString().compareTo(s2.get("track_id").getAsString());
		}
	}	
	
	/**
	 * Comparator to compare song by title
	 */
	class TagComparator implements Comparator<JsonObject> {
		@Override
		public int compare(JsonObject s1, JsonObject s2) {
			return s1.get("track_id").getAsString().compareTo(s2.get("track_id").getAsString());
		}
	}

	/**
	 * Comparator to compare song by title
	 */
	class TitleComparator implements Comparator<JsonObject> {
		@Override
		public int compare(JsonObject s1, JsonObject s2) {
			int a = s1.get("artist").getAsString().compareTo(s2.get("artist").getAsString());
			int b = s1.get("title").getAsString().compareTo(s2.get("title").getAsString());
			if (b != 0) {
				return b;
			}
			if (a != 0) {
				return a;
			}
			return s1.get("track_id").getAsString().compareTo(s2.get("track_id").getAsString());
		}
	}

	/**
	 * Comparator to compare song by artist
	 */
	class ArtistComparator implements Comparator<JsonObject> {
		@Override
		public int compare(JsonObject s1, JsonObject s2) {
			int a = s1.get("artist").getAsString().compareTo(s2.get("artist").getAsString());
			int b = s1.get("title").getAsString().compareTo(s2.get("title").getAsString());
			if (a != 0) {
				return a;
			}
			if (b != 0) {
				return b;
			}
			return s1.get("track_id").getAsString().compareTo(s2.get("track_id").getAsString());
		}
	}
}