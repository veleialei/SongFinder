package servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.server.Server;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import songfinder.ArtistData;

public class InfoServlet extends BaseServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// data needed
		HttpSession session = request.getSession();
		Data data = (Data) getServletConfig().getServletContext().getAttribute(DATA);
		Library lib = (Library) getServletConfig().getServletContext().getAttribute(LIBRARY);

		String songinfo = request.getParameter("title");
		String artistinfo = request.getParameter("artist");
		String account = request.getParameter("account");
		String lastfmtop = request.getParameter("lastfmtop");
		String privacy = request.getParameter(PRIVACY);

		//change privacy status
		if (privacy != null) {
			if (privacy.equals("open")) {
				session.setAttribute(PRIVACY, "open");
			}
			if (privacy.equals("close")) {
				session.setAttribute(PRIVACY, "close");
			}
		} else {
			privacy = "close";
		}

		//clear history
		if (request.getParameter("clear") != null) {
			if (session.getAttribute(NAME) != null) {
				data.clearHistory((String) session.getAttribute(NAME));
			}
		}

		//back to /search
		if (request.getParameter("home") != null) {
			response.sendRedirect(response.encodeRedirectURL("/search"));
			return;
		}

		//delete user, function of admin
		if (request.getParameter("delete") != null) {
			data.removeUser(request.getParameter("delete"));
		}
		
		//shutdown server, function of admin
		if (request.getParameter("shutdown") != null) {
			SongFinderServer.shutdown(response);
			return;
		}
		
		//add new artist into list, function of admin
		if (request.getParameter("addartist") != null && request.getParameter("addartist").trim().length() > 1) {
			lib.addArtist(request.getParameter("addartist"));
		}
		
		// content
		PrintWriter out = prepareResponse(response);
		out.println(header("INFO"));
		out.println("<form action=\"info\" method=\"post\">");
		out.println(
				"<button name =\"home\" value = \"search\" style=\"float:right; margin-top:24px; margin-right:10px\" class=\"fa fa-home\"></button>");
		out.println("</form>");
		out.println("<center>");

		
		// display song info
		if (songinfo != null) {
			JsonObject s = lib.getSong(songinfo);
			String title = s.get("title").getAsString();
			String artist = s.get("artist").getAsString();
			JsonParser parser = new JsonParser();
			String query = artist.replace(" ", "+") + "+" + title.replace(" ", "+");
			JsonObject ytb = YOUTUBEFetcher.getJSON(query);
			if(ytb!=null && ytb.size()!=0) {
				JsonArray ytbItems = ytb.get("items").getAsJsonArray();
				JsonObject objectJ = ytbItems.get(0).getAsJsonObject();
				String topSuggestion = "<a href=\""
				+ "https://www.youtube.com/watch?v=" + objectJ.get("id").getAsJsonObject().get("videoId").getAsString()
				+ "\">" + "<img src=\""
				+ objectJ.get("snippet").getAsJsonObject().get("thumbnails").getAsJsonObject().get("medium").getAsJsonObject().get("url").getAsString()
				+ "\" style=\"width:320px; height:180px\"></a>";
				out.println("<h1 style=\"margin-top:-16px;\" ><i class=\"fa fa-music\"></i>Track Information</h1>");
				out.println("<h3>MV on YOUTUBE: </h3>");				
				out.println(topSuggestion);
			}else {
				out.println("<h3>MV on YOUTUBE: </h3>");
				out.println("<h4>No MV Found</h4>");
			}

			
			out.println("<h3>Title: " + title + "</h3>");
			out.println("<h3>Aritst: " + artist + "</h3>");
			out.println("<center>");
			out.println(lib.getTitleInfo(title));
			out.println("</center>");
		}

		// display artist info
		if (artistinfo != null) {
			JsonObject a = lib.getIdMap().get(Integer.parseInt(artistinfo));
			out.println("<h1 style=\"margin-top:-16px;\" ><i class=\"fa fa-user\"></i>" + a.get("name").getAsString()
					+ "</h1>");
			out.println("<img src=\"" + a.get("img").getAsString() + "\">");
			out.println("<div style=\"width:66%\">");
			out.println("<h4>Playcount : <p>" + a.get("playcount").getAsInt() + "</p></h4>");
			out.println("<h4>Listeners : <p> " + a.get("listeners").getAsInt() + "</p></h4>");
			out.println("<h4>Published : <p> " + a.get("published").getAsString() + "</p></h4>");
			out.println("<h4>Summary : <p>" + a.get("summary").getAsString() + "</p></h4>");
			out.println("<h4>Content : <p>" + a.get("content").getAsString() + "</p></h4>");
			out.println("</div >");
		}

		// display artist info
		if (lastfmtop != null) {
			out.println("<form action=\"info?lastfmtop\" method=\"post\">");
			out.println("<h1 style=\"margin-top:-16px;\" ><i class=\"fa fa-music\"></i>Current Trend in LASTFM</h1>");
			out.println(lib.getTopLastfm());
			out.println("<h2>Explore Trend in Specific Country</h2>");
			out.println("<p>Query:</p><input type=\"text\" name=\"search\"><input type=\"submit\" value=\"Enter\">");
			if (request.getParameter("search") != null && request.getParameter("search").trim().length() > 0) {
				out.println(lib.getTopGeoLastfm(request.getParameter("search").trim()));
			}
			out.println("</form>");
		}

		// display account info
		if (account != null) {
			out.println("<form action=\"info?account=" + (String) session.getAttribute(NAME) + "\" method=\"post\">");
			out.println(data.listToHtml((String) session.getAttribute(NAME)));
			if (session.getAttribute(PRIVACY)!=null && session.getAttribute(PRIVACY).equals("close")) {
				out.println("<h2>Private Search : No</h2>");
			} else if (session.getAttribute(PRIVACY).equals("open")) {
				out.println("<h2>Private Search : Yes</h2>");
			}
			out.println("<button style=\"margin-top:-40px\" name = \"privacy\" value = \"open\">Open</button>");
			out.println("<button style= \"margin-top:-40px\" name = \"privacy\" value = \"close\">Close</button>");
			out.println("</form>");
			String curName = (String) session.getAttribute(NAME);
			if (curName.equals("admin")) {
				out.println("<form action=\"info?account=admin\" method=\"post\">");
				out.println(data.adminFunction());
				out.println("</form>");
			}
		}
		out.println("</center>");
		out.println(footer());
	}
}
