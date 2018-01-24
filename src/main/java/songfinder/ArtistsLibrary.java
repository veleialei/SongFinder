package songfinder;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import com.google.gson.JsonObject;

import utility.ReentrantLock;

public class ArtistsLibrary {
	private ReentrantLock rwl;
	private Map<String, JsonObject> sortedByName;
	private Map<Integer, JsonObject> idMap;
	private Map<Integer, Set<JsonObject>> sortedByPlaycount;
	private Set<String> artistNames;

	public ArtistsLibrary() {
		this.rwl = new ReentrantLock();
		this.idMap = new HashMap<>();
		this.artistNames = new HashSet<>();
		this.sortedByName = new TreeMap<>();
		this.sortedByPlaycount = new TreeMap<>(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o2 - o1;
			}
		});
	}
	
	public JsonObject getCopy(String id) {
		rwl.lockRead();		
		JsonObject res =  clone(idMap.get(Integer.parseInt(id)));
		rwl.unlockRead();
		return res;
	}	
	
	public String getArtist(String id) {
		rwl.lockRead();
		String res =  idMap.get(Integer.parseInt(id)).get("artist").getAsString();
		rwl.unlockRead();
		return res;
	}	
	
	/**
	 * add artist data into library
	 */
	public void addArtist(JsonObject artist) {
		this.rwl.lockWrite();
		sortedByName.put(artist.get("name").getAsString(), artist);
		if (!sortedByPlaycount.containsKey(artist.get("playcount").getAsInt())) {
			artistNames.add(artist.get("name").getAsString().toLowerCase());
			Set<JsonObject> a = new HashSet<>();
			sortedByPlaycount.put(artist.get("playcount").getAsInt(), a);
		}
		sortedByPlaycount.get(artist.get("playcount").getAsInt()).add(artist);
		idMap.put(artist.get("id").getAsInt(), artist);
		this.rwl.unlockWrite();
	}

	/**
	 * @return name of artists
	 */
	public Set<String> getArtistNames() {
		this.rwl.lockRead();
		Set<String> newone = new HashSet<>();
		for(String str : artistNames) {
			newone.add(str);
		}
		this.rwl.unlockRead();
		return newone;
	}

	/**
	 * @return return map ordered by name
	 */
	public Map<String, JsonObject> getByName() {
		this.rwl.lockRead();
		Map<String, JsonObject> newone = new TreeMap<>();
		for(Entry<String, JsonObject> entry : sortedByName.entrySet()) {
			JsonObject obj = clone(entry.getValue());
			if(obj!=null) {
				newone.put(entry.getKey(), obj);
			}			
		}
		this.rwl.unlockRead();
		return newone;
	}

	/**
	 * @return return map ordered by playcount
	 */
	public Map<Integer, Set<JsonObject>> getByPlaycount() {
		this.rwl.lockRead();
		Map<Integer, Set<JsonObject>> newone = new TreeMap<>(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o2 - o1;
			}
		});
		for(Entry<Integer, Set<JsonObject>> entry : sortedByPlaycount.entrySet()) {
			Set<JsonObject> set = new HashSet<JsonObject>();
			for(JsonObject obj : entry.getValue()) {
				JsonObject jobj = clone(obj);
				if(jobj!=null) {
					set.add(jobj);
				}	
			}
			newone.put(entry.getKey(), set);
		}
		this.rwl.unlockRead();
		return newone;
	}
	
	
	
	/**
	 * @return return map by id
	 */
	public Map<Integer, JsonObject> getById() {
		this.rwl.lockRead();
		Map<Integer, JsonObject> newone = new TreeMap<>();
		for(Entry<Integer, JsonObject> entry : idMap.entrySet()) {
			JsonObject obj = clone(entry.getValue());
			if(obj!=null) {
				newone.put(entry.getKey(), obj);
			}			
		}
		this.rwl.unlockRead();
		return newone;
	}

	/**
	 * @return deep copy the JsonObject
	 */
	private JsonObject clone(JsonObject info) {
		JsonObject artist = new JsonObject();
		if(info == null || info.get("name") == null) return null;
		artist.addProperty("name", info.get("name").getAsString());
		artist.addProperty("img", info.get("img").getAsString());
		artist.addProperty("listeners", info.get("listeners").getAsString());
		artist.addProperty("playcount", info.get("playcount").getAsString());
		artist.addProperty("summary", info.get("summary").getAsString());
		artist.addProperty("content", info.get("content").getAsString());
		artist.addProperty("published", info.get("published").getAsString());
		artist.addProperty("id", info.get("id").getAsString());		
		return artist ;
	}
}
