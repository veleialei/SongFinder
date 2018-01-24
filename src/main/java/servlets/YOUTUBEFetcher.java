package servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

// API Credentials:
// reference link:
// https://developers.google.com/youtube/v3/quickstart/java
public class YOUTUBEFetcher {

	/** Application name. */
	private static final String APPLICATION_NAME = "SongFinder";

	/** Directory to store user credentials for this application. */
	private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"),
			".credentials/youtube-java-quickstart");

	/** Global instance of the {@link FileDataStoreFactory}. */
	private static FileDataStoreFactory DATA_STORE_FACTORY;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport HTTP_TRANSPORT;

	/**
	 * Global instance of the scopes required by this quickstart.
	 *
	 * If modifying these scopes, delete your previously saved credentials at
	 * ~/.credentials/drive-java-quickstart
	 */
	private static final List<String> SCOPES = Arrays.asList(YouTubeScopes.YOUTUBE_READONLY);

	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}

	public static Credential authorize() throws IOException {
		// Load client secrets.
		InputStream in = Library.class.getResourceAsStream("/client_secret.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
		return credential;
	}

	public static YouTube getYouTubeService() throws IOException {
		Credential credential = authorize();
		return new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME)
				.build();
	}

	// YouTube search API:
	// reference link:
	// https://developers.google.com/youtube/v3/docs/search/list
	public static JsonObject getJSON(String query) {
		JsonObject ret = new JsonObject();
		try {
			YouTube youtube = getYouTubeService();
			HashMap<String, String> parameters = new HashMap<>();
			parameters.put("part", "snippet");
			parameters.put("maxResults", "3");
			parameters.put("q", query);
			parameters.put("type", "");

			YouTube.Search.List searchListByKeywordRequest = youtube.search().list(parameters.get("part").toString());
			if (parameters.containsKey("maxResults")) {
				searchListByKeywordRequest.setMaxResults(Long.parseLong(parameters.get("maxResults").toString()));
			}

			if (parameters.containsKey("q") && parameters.get("q") != "") {
				searchListByKeywordRequest.setQ(parameters.get("q").toString());
			}

			if (parameters.containsKey("type") && parameters.get("type") != "") {
				searchListByKeywordRequest.setType(parameters.get("type").toString());
			}

			SearchListResponse response = searchListByKeywordRequest.execute();
			JsonParser parser = new JsonParser();
			ret = parser.parse(response.toString()).getAsJsonObject();
		} catch (IOException exception) {

		}
		return ret;
	}
}
