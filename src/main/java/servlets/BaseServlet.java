package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/*
 * Base class for all Servlets in this application.
 * Provides general helper methods.
 */

public class BaseServlet extends HttpServlet {
	public static final String DATA = "data";
	public static final String UUID = "uuid";
	public static final String ITEM = "item";
	public static final String NAME = "name";
	public static final String PASSWORD = "password";
	public static final String LIBRARY = "library";
	public static final String SEARCH = "search";
	public static final String TYPE = "type";
	public static final String ORDER = "order";
	public static final String PRIVACY = "privacy";
	public static final String SELECTARTIST = "select_artist";
	public static final String SELECTTITLE = "select_title";
	public static final String DELETE = "delete";
	public static final String STATUS = "status";
	public static final String ERROR = "error";
	public static final String NOT_LOGGED_IN = "not_logged_in";

	/*
	 * Prepare a response of HTML 200 - OK. Set the content type and status. Return
	 * the PrintWriter.
	 */
	protected PrintWriter prepareResponse(HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		return response.getWriter();
	}

	/*
	 * Return the beginning part of the HTML page.
	 */
	protected String header(String title) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><title>" + title + "</title></head><body>");
		sb.append(
				"<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css\">");
		sb.append("<style>" + // https://i.ytimg.com/vi/KLwXbeE4dBU/maxresdefault.jpg//
								// https://i.imgur.com/inOTmev.jpg
				"body {color: white; background-image: url(http://pregnancydoctorpenang.com/wp-content/uploads/sites/27/minimalizm-gradient-background.jpg); "
				+ "background-repeat: no-repeat; background-attachment: fixed; background-size: 100% 100%; background-color: black; margin: 0px}"
				+ "h1 {background-color : #00001a; color: #E6E6FA; border: 13px solid #00001a; margin-top:0px;}"
				+ "table, th, td {border: 1px solid #E6E6FA; border-collapse: collapse; padding: 6px;}"
				+ "button, table, th, td, caption, input, select, p{margin: 6px; text-align: left}"
				+ "i {margin: 8px;  text-align: left}" + "td {font-weight: bold; }"
				+ "p {padding: 4px; display:inline-block}"
				+ "button, select, input {-webkit-box-shadow: none; -moz-box-shadow: none;color:#00001a; background-color: #E6E6FA; box-shadow: none; border-radius: 3px; padding:4px;}"
				+ "button:hover {cursor: pointer}"
				+ "a {padding 4px; margin:6px; border-radius:3px; background-color:#E6E6FA; color:#00001a}"
				+ "table {margin-bottom: 37px}" + "</style>");
		return sb.toString();
	}

	/*
	 * Return the last part of the HTML page.
	 */
	protected String footer() {
		return "</body></html>";
	}

	/*
	 * Given a request, return the name found in the Cookies provided.
	 */
	protected String getCookieValue(HttpServletRequest request, String key) {
		Cookie[] cookies = request.getCookies();

		String name = null;

		if (cookies != null) {
			// for each cookie, if the key is name, store the value
			for (Cookie c : cookies) {
				if (c.getName().equals(key)) {
					name = c.getValue();
				}
			}
		}
		return name;
	}

	/*
	 * Given a request, return the value of the parameter with the provided name or
	 * null if none exists.
	 */
	protected String getParameterValue(HttpServletRequest request, String key) {
		return request.getParameter(key);
	}
}
