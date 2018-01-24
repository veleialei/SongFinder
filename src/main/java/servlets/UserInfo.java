package servlets;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.gson.JsonObject;

import utility.ReentrantLock;

import java.text.SimpleDateFormat;

//store user private data
public class UserInfo {
	private String time;
	private String lasttime;
	private String name;
	private String password;
	private boolean privacy;
	private int totalVisit;
	private List<String> tagHistory;
	private List<String> titleHistory;
	private List<String> artistHistory;
	private Set<String> likeArtists;
	private Set<String> likeSongs;
	private ReentrantLock rwl;

	public UserInfo(String name, String password) {
		Date now = new Date();
		this.rwl = new ReentrantLock();
		this.likeArtists = new HashSet<String>();
		this.likeSongs = new HashSet<String>();
		this.totalVisit = 0;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		this.time = dateFormat.format(now);
		this.name = name;
		this.password = password;
		this.lasttime = "";
		this.tagHistory = new ArrayList<String>();
		this.titleHistory = new ArrayList<String>();
		this.artistHistory = new ArrayList<String>();
		this.privacy = false;
	}

	// add artist to favorite list;
	public void addLikeArtist(String id) {
		rwl.lockWrite();
		likeArtists.add(id);
		rwl.unlockWrite();
	}

	// delete artist from favorite list;
	public void deleteLikeArtist(String id) {
		rwl.lockWrite();
		likeArtists.remove(id);
		rwl.unlockWrite();
	}

	// add song to favorite list;
	public void addLikeSong(String id) {
		rwl.lockWrite();
		likeSongs.add(id);
		rwl.unlockWrite();
	}

	// delete song from favorite list;
	public void deleteLikeSong(String id) {
		rwl.lockWrite();
		likeSongs.remove(id);
		rwl.unlockWrite();
	}

	// change privacy status
	public void changePrivcay() {
		rwl.lockWrite();
		this.privacy = !privacy;
		rwl.unlockWrite();
	}

	// get privacy status
	public boolean gePrivcay() {
		rwl.lockRead();
		boolean res = privacy;
		rwl.unlockRead();
		return res;
	}

	// verify password
	public boolean verify(String password) {
		rwl.lockRead();
		boolean res = password.equals(this.password);
		rwl.unlockRead();
		return res;
	}

	// add to search history according to type
	public void addHistory(String key, String type) {
		rwl.lockWrite();
		if (type.equals("artist")) {
			artistHistory.add(key);
		} else if (type.equals("title")) {
			titleHistory.add(key);
		} else if (type.equals("tag")) {
			tagHistory.add(key);
		}
		rwl.unlockWrite();
	}

	// get the loginTime
	public void loginTime() {
		rwl.lockWrite();
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		this.time = dateFormat.format(now);
		rwl.unlockWrite();
	}

	// update last visit time
	public void updateLastTime() {
		rwl.lockWrite();
		lasttime = new String(time);
		rwl.unlockWrite();
	}

	// get the user name
	public String getName() {
		rwl.lockRead();
		String res = name;
		rwl.unlockRead();
		return res;
	}

	// get the time
	public String getTime() {
		rwl.lockRead();
		String res = time;
		rwl.unlockRead();
		return res;
	}

	// get total visit times
	public int totalVisit() {
		rwl.lockRead();
		int res = totalVisit;
		rwl.unlockRead();
		return res;
	}

	// update visit time
	public void updateTotalVisit() {
		rwl.lockWrite();
		totalVisit++;
		rwl.unlockWrite();
	}

	// clean history
	public void clearHistory() {
		rwl.lockWrite();
		artistHistory.clear();
		titleHistory.clear();
		tagHistory.clear();
		rwl.unlockWrite();
	}

	// print favorite artist list
	public List<String> printArtistList() {
		rwl.lockRead();
		List<String> arr = new ArrayList<String>();
		for (String id : likeArtists) {
			JsonObject jobj = Library.getArtist(id.trim());
			if (jobj == null || jobj.get("name") == null)
				continue;
			StringBuilder sb = new StringBuilder();
			sb.append(
					"<tr><td>" + jobj.get("name").getAsString() + "</td>" + "<td>" + jobj.get("playcount").getAsString()
							+ "</td>" + "" + "<td><a name =\"info\" href='info?artist=" + jobj.get("id").getAsInt()
							+ "'><p>info</p></a></td>" + "<td><button name = \"dislikeArtist\" value = \""
							+ jobj.get("id").getAsInt() + "\">delete</button></td></tr>");
			arr.add(sb.toString());
		}
		rwl.unlockRead();
		return arr;
	}

	// print favorite song list
	public List<String> printSongList() {
		rwl.lockRead();
		List<String> arr = new ArrayList<String>();
		for (String id : likeSongs) {
			JsonObject jobj = Library.getSong(id.trim());
			StringBuilder builder = new StringBuilder();
			builder.append("<tr><td>" + jobj.get("title").getAsString() + "</td>" + "<td>"
					+ jobj.get("artist").getAsString() + "</td>" + "" + "<td><a name =\"info\" href='info?title="
					+ jobj.get("track_id").getAsString()+ "'><p>info</p></a></td>" 
					+ "<td><button name = \"dislikeSong\" value = \"" + jobj.get("track_id").getAsString() + "\">delete</button></td></tr>");
			arr.add(builder.toString());
		}
		rwl.unlockRead();
		return arr;
	}

	// print account info
	public String infoToHtml() {
		rwl.lockRead();
		StringBuilder builder = new StringBuilder();
		builder.append("<center>");
		builder.append("<h1  style=\"margin-top:-16px\"><i class=\"fa fa-user\"></i>" + name + "'s Information</h1>");
		builder.append("<h2>Last Visit Time : " + lasttime + "</h2>");
		builder.append("<h2>Search History :</h2>");
		builder.append("<h4 style=\"margin-bottom:0px; margin-top:0px\">Artist History :</h4>");
		for (String record : artistHistory) {
			builder.append("<p style=\"margin-bottom:0px; margin-top:0px\">" + record + "</p>");
		}
		builder.append("<h4 style=\"margin-bottom:0px; margin-top:0px\">Title History :</h4>");
		for (String record : titleHistory) {
			builder.append("<p style=\"margin-bottom:0px; margin-top:0px\">" + record + "</p>");
		}
		builder.append("<h4 style=\"margin-bottom:0px; margin-top:0px\">Tag History :</h4>");
		for (String record : tagHistory) {
			builder.append("<p style=\"margin-bottom:0px; margin-top:0px\">" + record + "</p>");
		}
		builder.append("<h2>Clean History :</h2>");
		builder.append("<button style=\"margin-top:-5px\" name=\"clear\" value=\"" + name + "\">clean</button>");
		builder.append("<h2>Favorite Artists List</h2>");
		if (likeArtists.isEmpty()) {
			builder.append("<h4>Empty</h4>");
		} else {
			builder.append(
					"<a style=\"margin-top:10px; display:inline-block\" name =\"like\" value = \"artist\" href='favorite_list?type=artist&pagination=25&page=1'>"
							+ "<p style=\"padding: 0px\"> favorite artists </p></a>");
		}
		builder.append("<h2>Favorite Songs List</h2>");
		if (likeSongs.isEmpty()) {
			builder.append("<h4>Empty</h4>");
		} else {
			builder.append(
					"<a style=\"margin-top:10px; display:inline-block\" name =\"like\" value = \"song\" href='favorite_list?type=song&pagination=25&page=1'>"
							+ "<p style=\"padding: 0px\"> favorite songs </p></a>");
		}
		builder.append("</center>");
		rwl.unlockRead();
		return builder.toString();

	}

}