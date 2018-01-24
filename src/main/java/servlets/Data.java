package servlets;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import utility.ReentrantLock;

public class Data {
	// maintain a map of name to UserInfo object
	protected HashMap<String, UserInfo> userInfo;
	private HashMap<String, Integer> topTitle;
	private HashMap<String, Integer> topArtist;
	private HashMap<String, Integer> topTag;
	private ReentrantLock rwl;
	
	// constructor
	public Data() {
		rwl = new ReentrantLock();
		topTitle = new HashMap<>();
		topTag = new HashMap<>();
		topArtist = new HashMap<>();
		userInfo = new HashMap<String, UserInfo>();
		this.addUser("admin", "admin");
	}

	//add to user's favorite artist list
	public void addLikeArtist(String name, String id) {
		this.rwl.lockWrite();
		userInfo.get(name).addLikeArtist(id);
		this.rwl.unlockWrite();
	}
	
	//add to user's favorite song list
	public void addLikeSong(String name, String id) {
		this.rwl.lockWrite();
		userInfo.get(name).addLikeSong(id);
		this.rwl.unlockWrite();		
	}
	
	//remove from user's favorite artist list
	public void dislikeArtist(String name, String id) {
		this.rwl.lockWrite();
		userInfo.get(name).deleteLikeArtist(id);
		this.rwl.unlockWrite();		
	}	

	//remove from user's favorite song list
	public void dislikeSong(String name, String id) {
		this.rwl.lockWrite();
		userInfo.get(name).deleteLikeSong(id);
		this.rwl.unlockWrite();		
	}
	
	//list of user's favorite artist list
	public List<String> printLikeArtist(String name) {
		this.rwl.lockRead();
		List<String>  res = userInfo.get(name).printArtistList();
		this.rwl.unlockRead();
		return res;
	}

	//list of user's favorite song list
	public List<String> printLikeSong(String name) {
		this.rwl.lockRead();
		List<String> res = userInfo.get(name).printSongList();
		this.rwl.unlockRead();
		return res;
	}
	
	// add suggestion search
	public void addTop(String keyword, String type) {
		this.rwl.lockWrite();
		if (keyword == null || keyword.trim().length() == 0)
			return;
		if (type.equals("title")) {
			if (!topTitle.containsKey(keyword)) {
				topTitle.put(keyword, 0);
			}
			topTitle.put(keyword, topTitle.get(keyword) + 1);

		} else if (type.equals("artist")) {
			if (!topArtist.containsKey(keyword)) {
				topArtist.put(keyword, 0);
			}
			topArtist.put(keyword, topArtist.get(keyword) + 1);
		} else if (type.equals("tag")) {
			if (!topTag.containsKey(keyword)) {
				topTag.put(keyword, 0);
			}
			topTag.put(keyword, topTag.get(keyword) + 1);
		}
		this.rwl.unlockWrite();
	}

	//change login time
	public void changeTime(String name) {
		this.rwl.lockWrite();
		userInfo.get(name).updateLastTime();
		this.rwl.unlockWrite();
	}

	//update login time
	public void updateLoginTime(String name) {
		this.rwl.lockWrite();
		if (userInfo.containsKey(name))
			userInfo.get(name).loginTime();
		this.rwl.unlockWrite();
	}

	// return top artist
	public String getTopArtist() {
		this.rwl.lockRead();
		String top = getTop(topArtist);
		this.rwl.unlockRead();
		return top;
	}

	// return top song
	public String getTopTitle() {
		this.rwl.lockRead();
		String top = getTop(topTitle);
		this.rwl.unlockRead();
		return top;
	}

	// return top tag
	public String getTopTag() {
		this.rwl.lockRead();
		String top = getTop(topTag);
		this.rwl.unlockRead();
		return top;
	}

	// helper function
	public String getTop(HashMap<String, Integer> map) {
		this.rwl.lockRead();
		String cur = null;
		int max = 0;
		for (Entry<String, Integer> entry : map.entrySet()) {
			if (entry.getValue() > max) {
				max = entry.getValue();
				cur = entry.getKey();
			}
		}
		this.rwl.unlockRead();
		return cur;
	}

	/*
	 * Returns true if the user exists in the data store.
	 */
	public boolean userExists(String name) {
		this.rwl.lockRead();
		boolean res = userInfo.containsKey(name);
		this.rwl.unlockRead();
		return res;
	}

	/*
	 * Add a new UserInfo object for a particular user.
	 */
	public void addUser(String name, String password) {
		this.rwl.lockWrite();
		if (!userInfo.containsKey(name))
			userInfo.put(name, new UserInfo(name, password));
		this.rwl.unlockWrite();
	}

	//delete by admin
	public void removeUser(String name) {
		this.rwl.lockWrite();
		if (userInfo.containsKey(name)) {
			userInfo.remove(name);
		}
		this.rwl.unlockWrite();
	}

	//return if contains name
	public boolean containsName(String name) {
		this.rwl.lockRead();
		boolean res = userInfo.containsKey(name);
		this.rwl.unlockRead();
		return res;
	}

	//verify user
	public boolean verifyName(String name, String password) {
		this.rwl.lockRead();
		boolean res = userInfo.get(name).verify(password);
		this.rwl.unlockRead();
		return res;
	}

	/*
	 * For a given user, add a new todo.
	 */
	public boolean add(String name, String search, String type) {
		this.rwl.lockWrite();
		boolean res;
		if (!userInfo.containsKey(name)) {
			res = false;
		} else {
			res = true;
			userInfo.get(name).addHistory(search, type);
		}
		this.rwl.unlockWrite();
		return res;
	}

	//delete history
	public void clearHistory(String name) {
		this.rwl.lockWrite();
		userInfo.get(name).clearHistory();
		this.rwl.unlockWrite();
	}

	//time of visits
	public void updateTotalVisit(String name) {
		this.rwl.lockWrite();
		userInfo.get(name).updateTotalVisit();
		this.rwl.unlockWrite();
	}

	/*
	 * Returns a String containing an HTML representation of the list associated
	 * with the session identified by id.
	 */
	public String listToHtml(String name) {
		this.rwl.lockRead();
		String list = null;
		if (userInfo.containsKey(name)) {
			list = userInfo.get(name).infoToHtml();
		}
		this.rwl.unlockRead();
		return list;
	}

	//return suggestion search list
	public String getTopList() {
		this.rwl.lockRead();
		StringBuilder builder = new StringBuilder();
		if (!topTitle.isEmpty() || !topArtist.isEmpty() || !topTag.isEmpty()) {
			builder.append("Poplular Search Keyword : ");
		}
		if (!topArtist.isEmpty()) {
			builder.append(" Artist - " + getTopArtist() + "; ");
		}
		if (!topTitle.isEmpty()) {
			builder.append("Title - " + getTopTitle() + "; ");
		}
		if (!topTag.isEmpty()) {
			builder.append("Tag - " + getTopTag() + ";");
		}
		this.rwl.unlockRead();
		return builder.toString();
	}

	/*
	 * Returns a String containing an HTML representation of the list associated
	 * with the session identified by id.
	 */
	public String adminFunction() {
		this.rwl.lockRead();
		StringBuilder builder = new StringBuilder();
		builder.append("<hr />");
		builder.append("<h2 style = \"margin-bottom:40px; margin-top:10px\" >Administrator Function</h2>");

		builder.append("<h3 style =\"margin-bottom:0px; margin-top:30px; \">User List</h3>");
		if (userInfo.size() > 1) {
			builder.append("<table style=\"width:50%\"><tr><th style=\"width:35%\">Name</th>"
					+ "<th style=\"width:30%\">Last Visit Time</th>" + "<th style=\"width:20%\">Total Visit Times</th>"
					+ "<th style=\"width:15%\">Delete User</th></tr>");

			for (Entry<String, UserInfo> one : userInfo.entrySet()) {
				if (one.getKey().equals("admin"))
					continue;
				builder.append("<tr><td style=\"width:35%\">" + one.getKey() + "</td>" + "<td style=\"width:30%\">"
						+ one.getValue().getTime() + "</td>" + "<td style=\"width:20%\">" + one.getValue().totalVisit()
						+ "</td>" + "<td style=\"width:15%\">" + "<button name = \"delete\" value=\"" + one.getKey()
						+ "\">delete</button></td></tr>");
			}
			builder.append("</table>");
		} else {
			builder.append("<h4 style =\"margin-bottom:0px;\">No User Currently</h4>");
		}
		builder.append("<h3 style =\"margin-bottom:0px; margin-top:50px;\" >Add New Artist</h3>");
		builder.append("<input style = \"width:15%\"  type=\"text\" name=\"addartist\">"
				+ "<input type=\"submit\" value=\"Enter\">"
				+ "<p style = \"text-align:center; display:block\"  type=\"text\">enter an artist name exists in Last.fm</p>");

		builder.append("<h3 style =\"margin-bottom:0px; margin-top:50px;\" >Gracefully Shutdown the Song Finder</h3>");
		builder.append(
				"<button style = \"margin-bottom:60px; display:block\" name=\"shutdown\" value=\"shutdown\">Shutdown</button>");

		this.rwl.unlockRead();
		return builder.toString();
	}
}
