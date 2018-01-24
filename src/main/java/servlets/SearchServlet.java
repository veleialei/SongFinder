package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/*
 * Servlet invoked at login.
 * Creates cookie and redirects to main AddScoresServlet.
 */
public class SearchServlet extends BaseServlet {

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
		// data need to use
		Data data = (Data) getServletConfig().getServletContext().getAttribute(DATA);
		Library lib = (Library) getServletConfig().getServletContext().getAttribute(LIBRARY);
		HttpSession session = request.getSession();

		int pagination;
		int curpage;
		if (request.getParameter("pagination") != null) {
			pagination = Integer.parseInt(request.getParameter("pagination"));
		} else {
			pagination = 25;
		}

		if (request.getParameter("curpage") != null) {
			curpage = Integer.parseInt(request.getParameter("curpage"));
		} else {
			curpage = 1;
		}
		// direct to login page
		if (request.getParameter("login") != null && session.getAttribute(NAME) == null) {
			response.sendRedirect(response.encodeRedirectURL("/login?" + STATUS + "=" + NOT_LOGGED_IN));
			return;
		}

		// logout
		if (request.getParameter("logout") != null && session.getAttribute(NAME) != null) {
			data.changeTime((String) session.getAttribute(NAME));
			session.setAttribute(NAME, null);
			session.setAttribute(PRIVACY, "close");
			request.getSession().invalidate();
			response.sendRedirect(response.encodeRedirectURL("/search"));
			return;
		}

		// go to account
		if (request.getParameter("account") != null && session.getAttribute(NAME) != null) {
			response.sendRedirect(response.encodeRedirectURL("/info?account=" + request.getParameter("account")));
			return;
		}

		// go to artist list
		if (request.getParameter("artist_order") != null) {
			response.sendRedirect(
					response.encodeRedirectURL("/artists_list?artist_order=" + request.getParameter("artist_order"))
							+ "&pagination=10&page=1");
			return;
		}

		// go to last fm top
		if (request.getParameter("lastfmtop") != null) {
			response.sendRedirect(response.encodeRedirectURL("/info?lastfmtop"));
			return;
		}

		// go to song info
		if (request.getParameter("title") != null) {
			response.sendRedirect(response.encodeRedirectURL("/info?title=" + request.getParameter("title")));
			return;
		}

		// set private mode
		if (request.getParameter(SEARCH) != null && request.getParameter(SEARCH).trim().length() > 0) {
			data.addTop(request.getParameter(SEARCH), request.getParameter(TYPE));
			if (session.getAttribute(PRIVACY) == null || session.getAttribute(PRIVACY).equals("close")) {
				data.add((String) session.getAttribute(NAME), request.getParameter(SEARCH), request.getParameter(TYPE));
			}
			response.sendRedirect(response.encodeRedirectURL(
					"/search?search_list=" + request.getParameter("search") + "&type=" + request.getParameter("type")
							+ "&pagination=" + request.getParameter("pagination") + "&curpage=" + curpage));
		}

		if (request.getParameter("likeArtist") != null && session.getAttribute(NAME) != null) {
			data.addLikeArtist((String) session.getAttribute(NAME), (String) request.getParameter("likeArtist"));
		}

		if (request.getParameter("likeSong") != null && session.getAttribute(NAME) != null) {
			data.addLikeSong((String) session.getAttribute(NAME), (String) request.getParameter("likeSong"));
		}

		PrintWriter out = prepareResponse(response);
		out.println(header("Song Finder"));

		out.println("<form action=\"search\" method=\"post\">");

		// account part
		out.println(printAcountInfo(session, request));

		// title
		out.println("<center>");
		out.println("<h1 style=\"border-top: 8px; margin-top:-18px; margin-bottom:0px; font-size:45px\">"
				+ "<img style = \"padding-top:-20px; margin-bottom:-42px\" src = \"https://image.ibb.co/nyYDFG/songfinderlogo2_01.png\" height = 122px;>Song Finder</h1>");
		out.println("</center>");

		// suggestion search
		out.println("<p style=\"padding:8px; font-size:16px; padding-left:16px; margin:0px; margin-bottom:6px; background-color:#00001a; color: #E6E6FA; border-bottom: 1px solid #E6E6FA; width:98.7%;\">"
				+ data.getTopList() + "</p>");

		// search part
		out.println("<center>");
		out.println(printSearchOption());
		out.println("</center>");
		out.println("</form>");

		out.println("<form method=\"post\">");
		out.println("<center>");

		// pagintation out.println(lib.listToHtml(search, type));
		List<String> list = lib.contentList(request.getParameter("search_list"), request.getParameter("type"));
		if (!list.isEmpty()) {
			out.println(printSearchList(list, request, pagination, curpage));
		}
		out.println("</center>");
		out.println("</form>");
		out.println(footer());
	}

	//print search result list;
	private String printSearchList(List<String> list, HttpServletRequest request, int pagination, int curpage) {
		StringBuilder sb = new StringBuilder();
		int size = list.size();
		int bottom = 1;
		int top = 1;
		if (request.getParameter("pagination") == null) {
			pagination = 5;
		} else {
			pagination = Integer.parseInt(request.getParameter("pagination"));
		}

		if (size > pagination) {
			if (size / pagination != 0)
				top = size / pagination + 1;
			else
				top = size / pagination;
		}
		
		sb.append(list.get(0));
		sb.append("<h4 style=\"width:74%; margin:0px\">Search Type : " + request.getParameter(TYPE) + " ; Item Per Page : " + pagination + " ;</h4>");
		
		if(list.size()>1) {
			for (int i = pagination * (curpage - 1) + 1; i > 0 && i <= pagination * (curpage) && i < list.size(); i++) {
				sb.append(list.get(i));
			}
			sb.append("<div><h4 style= \"display:inline-block; margin:0px\">View: </h4>"
					+ "<a name =\"pagination\" value = \"5\" href='search?" + "search_list=" + request.getParameter("search_list") 
					+ "&type=" + request.getParameter("type") + "&pagination=5&page=1'><p>5</p></a>" 
					
					+ "<a name =\"pagination\" value = \"10\" href='search?" + "search_list=" + request.getParameter("search_list") 
					+ "&type=" + request.getParameter("type") + "&pagination=10&page=1'><p>10</p></a>" 
					
					+ "<a name =\"pagination\" value = \"25\" href='search?" + "search_list=" + request.getParameter("search_list")
					+ "&type=" + request.getParameter("type") + "&pagination=25&page=1'><p>25</p></a>"
					
					+ "<h4  style=\"margin:0px; display:inline-block\">Per Page</h4>");
			sb.append("</div>");

			if (curpage < top) {
				sb.append("<a style=\"display:inline-block\" name =\"nextpage\" value = \"next\" "
						+ "href='search?search_list=" + request.getParameter("search_list") + "&type="
						+ request.getParameter("type") + "&pagination=" + request.getParameter("pagination") + "&curpage="
						+ (curpage + 1) + "'><p style=\"padding: 0px\"> next </p></a>");
			} else {
				curpage = top;
			}
			sb.append("<h4 style= \"margin:0px; display:inline-block\">page " + curpage + " of total " + top + "</h4>");

			if (curpage > bottom) {
				sb.append("<a style=\"margin-top:10px; display:inline-block\" name =\"nextpage\" value = \"next\" "
						+ "href='search?search_list=" + request.getParameter("search_list") + "&type="
						+ request.getParameter("type") + "&pagination=" + request.getParameter("pagination") + "&curpage="
						+ (curpage - 1) + "'><p style=\"padding: 0px\"> prev </p></a>");
			} else {
				curpage = bottom;
			}
		}
		
		return sb.toString();
	}

	//print account information
	private String printAcountInfo(HttpSession session, HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div style=\"padding-left:10px; background-color:#00001a \">"
				+ "<button style=\"display:inline-block; background-color:#00001a; color: #E6E6FA; margin-bottom:0px\" name=\"login\" value=\"login\">Login</button>"
				+ "<button style=\"display:inline-block; margin-bottom:0px; background-color:#00001a; color: #E6E6FA;\" name=\"logout\" value=\"logout\">Logout</button>"
				+ "<button style=\"display:inline-block; margin-bottom:0px; background-color:#00001a; color: #E6E6FA;\" name=\"account\" value=\""
				+ (String) session.getAttribute(NAME) + "\">Account</button>");
		sb.append(
				"<button name =\"home\" value = \"search\" style=\"float:right; margin-top:12px; margin-right:12px\" class=\"fa fa-home\"></button>");
		if (request.getParameter("login") != null && session.getAttribute(NAME) != null) {
			sb.append("<p style=\"font-size:14px; color:#FDF5E6; display:inline-block; margin-bottom:0px\">"
					+ (String) session.getAttribute(NAME) + ", please logout first</p></div>");
		} else if (session.getAttribute(NAME) != null) {
			sb.append("<p style=\"font-size:14px; color:#FDF5E6; display:inline-block; margin-bottom:0px\">Hello "
					+ (String) session.getAttribute(NAME) + ", welcome to Song Finder!</p></div>");
		} else {
			sb.append(
					"<p style=\"font-size:14px; color:#FDF5E6; display:inline-block; margin-bottom:0px\">You can login or create an account</p></div>");
		}
		return sb.toString();
	}

	//print search option
	private String printSearchOption() {
		StringBuilder sb = new StringBuilder();
		sb.append("<div>");
		sb.append("<p>Search type:</p><select style=\"margin-right: 24px\" type=\"text\"name = \"type\">"
				+ "<option value=\"artist\">Artist</option>" + "<option value=\"title\">Title</option>"
				+ "<option value=\"tag\">Tag</option></select> ");

		sb.append("<p>Item Per Page:</p><select style=\"margin-right: 14px\" type=\"text\"name = \"pagination\">"
				+ "<option value=\"5\">5</option>" + "<option value=\"10\">10</option>"
				+ "<option value=\"25\">25</option></select> ");

		sb.append("<p>Query:</p><input type=\"text\" name=\"search\"><input type=\"submit\" value=\"Enter\">");

		// artist list part
		sb.append("<p style=\"display:inline-block; margin-left:24px\">View All Artists:</p>"
				+ "<button style=\"display:inline-block; \" name=\"artist_order\" value=\"name\">By Name</button>"
				+ "<button style=\"display:inline-block;\" name=\"artist_order\" value=\"playcount\">By Playcount</button>");

		// current trend part
		sb.append("<p style=\"display:inline-block; margin-left:24px\">Current Trend in Last.fm:</p>"
				+ "<button style=\"display:inline-block;\" name=\"lastfmtop\" value=\"top\">Go</button>");
		sb.append("</div>");
		return sb.toString();
	}
}