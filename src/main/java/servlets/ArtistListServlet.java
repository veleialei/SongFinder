package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ArtistListServlet extends BaseServlet {

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
		// get data
		Data data = (Data) getServletConfig().getServletContext().getAttribute(DATA);
		Library lib = (Library) getServletConfig().getServletContext().getAttribute(LIBRARY);
		HttpSession session = request.getSession();
		PrintWriter out = prepareResponse(response);

		String order = request.getParameter("artist_order");
		
		int size = lib.getArtistByName().size();
		int curpage = 1;
		int number = 10;
		int bottom = 1;
		int top = 1;
		if (size % number != 0)
			top = size / number + 1;
		else
			top = size / number;	
		
		// go to artist detail information
		if (request.getParameter("info") != null) {
			response.sendRedirect(response.encodeRedirectURL("/info?" + "artist" + "=" + request.getParameter("info")));
			return;
		}

		//back to /search
		if (request.getParameter("home") != null) {
			response.sendRedirect(response.encodeRedirectURL("/search"));
			return;
		}

		//get pagination number
		if (request.getParameter("pagination") != null) {
			number = Integer.parseInt(request.getParameter("pagination"));
		}
		
		//get current page
		if (request.getParameter("page") != null) {
			if (Integer.parseInt(request.getParameter("page")) > bottom
					&& Integer.parseInt(request.getParameter("page")) < top + 1) {
				curpage = Integer.parseInt(request.getParameter("page"));
			}
		}

		//add to favorite list
		if (request.getParameter("like") != null && session.getAttribute(NAME)!=null) {
			data.addLikeArtist((String)session.getAttribute(NAME), (String)request.getParameter("like"));
		}
		
		// title and headers
		out.println(header("Artists List"));
		out.println("<center>");
		out.println("<h1><i class=\"fa fa-music\"></i>Artists List</h1>");
		out.println("</center>");

		// create list according to order
		out.println("<form action=\"artists_list\" method=\"post\">");
		out.println("<center>");
		out.println(
				"<button name=\"home\" value = \"search\" style=\"float:right; margin-top:-70px; margin-right:10px\" class=\"fa fa-home\"></button>");
		out.println("<div>");
		out.println("<h4 style= \"display:inline-block\">View: </h4>"
			+ "<a name =\"pagination\" value = \"25\" href='artists_list?artist_order=" + order + "&pagination=5&page=1'><p>5</p></a>"
			+ "<a name =\"pagination\" value = \"25\" href='artists_list?artist_order=" + order + "&pagination=10&page=1'><p>10</p></a>"
			+ "<a name =\"pagination\" value = \"25\" href='artists_list?artist_order=" + order + "&pagination=25&page=1'><p>25</p></a>"
			+ "<h4  style=\"display:inline-block\">Per Page</h4>");
		out.println("</div>");
		
		//calculate top
		if (size % number != 0)
			top = size / number + 1;
		else
			top = size / number;	
		//show next
		if (curpage < top) {
			out.println(
					"<a style=\"margin-top:10px; display:inline-block\" name =\"nextpage\" value = \"next\" href='artists_list?artist_order="
							+ order + "&pagination=" + number + "&page=" + (curpage + 1)
							+ "'><p style=\"padding: 0px\"> next </p></a>");
		}
		//show prev
		out.println("<h4 style= \"display:inline-block\">page " +curpage+ " of total "+ top + "</h4>");		
		if (curpage > bottom) {
			out.print(
					"<a style= \"margin-top:10px; display:inline-block\"  name =\"prevpage\" value = \"prev\" href='artists_list?artist_order="
							+ order + "&pagination=" + number + "&page=" + (curpage - 1)
							+ "'><p style=\"padding: 0px\"> prev </p></a>");
		}		
		out.println("</center>");
		out.println("</form>");
		
		//print the artislt list
		out.println("<form method=\"post\">");
		out.println("<center>");
		StringBuilder builder = new StringBuilder();
		//print according to name
		if (order.equals("name")) {
			builder.append(
					"<table style=\"width:85%\"><caption style=\"text-align:center; margin-bottom: 6px\">Here are All artists in the library sorted by name</caption>"
				  + "<tr><th>Artist Name</th><th style=\"width:35%\">Playcount</th><th style=\"width:25%\">More Info</th><th style=\"width:12%\">Like</th></tr>");
			//Go to Lib
			List<String> list = lib.getArtistListbyName();
			for (int i = number * (curpage - 1); i < number * (curpage) && i < list.size(); i++) {
				builder.append(list.get(i));
			}
		}
		//print according to paycount
		else if (order.equals("playcount")) {
			builder.append(
					"<table style=\"width:85%\"><caption style=\"text-align:center; margin-bottom: 6px\">Here are All artists in the library sorted by playcount</caption>"
							+ "<tr><th>Artist Name</th><th style=\"width:35%\">Playcount</th><th style=\"width:13%\">More Info</th><th style=\"width:12%\">Like</th></tr>");
			//Go to Lib
			List<String> list = lib.getArtistListbyPlaycount();
			for (int i = number * (curpage - 1); i < number * (curpage); i++) {
				builder.append(list.get(i));
			}
		}
		out.println(builder.toString());
		out.println("</center>");
		out.println("</form>");
		out.println(footer());
	}
}

