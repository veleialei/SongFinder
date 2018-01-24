package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class FavoriteListServlet extends BaseServlet {

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
		String name = (String) session.getAttribute(NAME);	
		
		if(request.getParameter("dislikeArtist") != null && name != null) {
			data.dislikeArtist(name, request.getParameter("dislikeArtist"));
		}
		
		if(request.getParameter("dislikeSong") != null && name != null) {
			data.dislikeSong(name, request.getParameter("dislikeSong"));
		}		
		
		if (request.getParameter("home") != null) {
			response.sendRedirect(response.encodeRedirectURL("/search"));
			return;
		}		
		out.println(header("Favorite List"));
		out.println("<center>");		
		String type = request.getParameter("type");
		if (name != null && type!=null && type.equals("artist")) {
			artistList(out,session,data,request);
		}
		if (name != null && type!=null && type.equals("song")) {
			songList(out,session,data,request);
		}
		out.println("</center>");
	}
	
	//print favorite artistlist 
	private void artistList(PrintWriter out, HttpSession session, Data data, HttpServletRequest request) {	
		List<String> print = data.printLikeArtist((String)session.getAttribute(NAME));
		int size = print.size();
		int curpage = 1;
		int number = 5;
		if(request.getParameter("pagination")!=null) {
			number = Integer.parseInt(request.getParameter("pagination"));
		}
		if(request.getParameter("page")!=null) {
			curpage = Integer.parseInt(request.getParameter("page"));
		}
		out.println("<form action=\"favorite_list?type=artist&pagination="+number+"&page="+curpage+"\" method=\"post\">");
		out.println("<h1><i class=\"fa fa-music\"></i>Favorite Artist List</h1>");
		out.println("<button name=\"home\" value = \"search\" style=\"float:right; margin-top:-70px; margin-right:10px\" class=\"fa fa-home\"></button>");	
		int bottom = 1;
		int top = 1;
		if (size % number == 0)
			top = size / number;
		else
			top = size / number + 1;
		StringBuilder builder = new StringBuilder();
		builder.append(	"<table style=\"width:85%\"><tr>"
				+ "<th>Artist Name</th><th style=\"width:35%\">Playcount</th>"
				+ "<th style=\"width:25%\">More Info</th>"
				+ "<th style=\"width:12%\">Delete</th></tr>");			
		for (int i = number * (curpage - 1); i < number * (curpage) && i < print.size(); i++) {
			builder.append(print .get(i));
		}
		out.println(builder.toString());
		out.println("<div>");
		out.println("<h4 style= \"display:inline-block\">View: </h4>"
			+ "<a name =\"pagination\" value = \"5\" href='favorite_list?type=artist&pagination=5&page=1'><p>5</p></a>"
			+ "<a name =\"pagination\" value = \"10\" href='favorite_list?type=artist&pagination=10&page=1'><p>10</p></a>"
			+ "<a name =\"pagination\" value = \"25\" href='favorite_list?type=artist&pagination=25&page=1'><p>25</p></a>"
			+ "<h4  style=\"display:inline-block\">Per Page</h4>");
		out.println("</div>");
		
		if (curpage < top) {
			out.println(
					"<a style=\"margin-top:10px; display:inline-block\" name =\"nextpage\" value = \"next\" href='favorite_list?type=artist&pagination=" + number + "&page=" + (curpage + 1)
					+ "'><p style=\"padding: 0px\"> next </p></a>");
		}
		out.println("<h4 style= \"display:inline-block\">page " +curpage+ " of total "+ top + "</h4>");		
		if (curpage > bottom) {
			out.print(
					"<a style= \"margin-top:10px; display:inline-block\"  name =\"prevpage\" value = \"prev\" href='favorite_list?type=artist&pagination=" + number + "&page=" + (curpage - 1)
					+ "'><p style=\"padding: 0px\"> prev </p></a>");
		}		
		out.println("</center>");	
		out.println("</form>");
	}

	//print favorite songlist 
	private void songList(PrintWriter out, HttpSession session, Data data, HttpServletRequest request) {
		out.println("<form action=\"favorite_list?type=song\" method=\"post\">");
		out.println("<h1><i class=\"fa fa-music\"></i>Favorite Song List</h1>");
		out.println("<button name=\"home\" value = \"search\" style=\"float:right; margin-top:-70px; margin-right:10px\" class=\"fa fa-home\"></button>");	
		List<String> print = data.printLikeSong((String)session.getAttribute(NAME));
		int size = print.size();
		int curpage = 1;
		int number = 5;
		if(request.getParameter("pagination")!=null) {
			number = Integer.parseInt(request.getParameter("pagination"));
		}
		if(request.getParameter("page")!=null) {
			curpage = Integer.parseInt(request.getParameter("page"));
		}
		int bottom = 1;
		int top = 1;
		if (size % number == 0)
			top = size / number;
		else
			top = size / number + 1;
		StringBuilder builder = new StringBuilder();
		builder.append(	"<table style=\"width:85%\"><tr>"
				+ "<th>Song Title</th>"
				+ "<th style=\"width:35%\">Artist</th>"
				+ "<th style=\"width:25%\">More Info</th>"
				+ "<th style=\"width:12%\">Delete</th></tr>");		
		for (int i = number * (curpage - 1); i < number * (curpage) && i < print.size(); i++) {
			builder.append(print.get(i));
		}
		out.println(builder.toString());
		out.println("<div>");
		out.println("<h4 style= \"display:inline-block\">View: </h4>"
			+ "<a name =\"pagination\" value = \"5\" href='favorite_list?type=song&pagination=5&page=1'><p>5</p></a>"
			+ "<a name =\"pagination\" value = \"10\" href='favorite_list?type=song&pagination=10&page=1'><p>10</p></a>"
			+ "<a name =\"pagination\" value = \"25\" href='favorite_list?type=song&pagination=25&page=1'><p>25</p></a>"
			+ "<h4  style=\"display:inline-block\">Per Page</h4>");
		out.println("</div>");

		if (curpage < top) {
			out.println(
					"<a style=\"margin-top:10px; display:inline-block\" name =\"nextpage\" value = \"next\" href='favorite_list?type=song&pagination=" + number + "&page=" + (curpage + 1)
					+ "'><p style=\"padding: 0px\"> next </p></a>");
		}
		out.println("<h4 style= \"display:inline-block\">page " +curpage+ " of total "+ top + "</h4>");		
		if (curpage > bottom) {
			out.print(
					"<a style= \"margin-top:10px; display:inline-block\"  name =\"prevpage\" value = \"prev\" href='favorite_list?type=song&pagination=" + number + "&page=" + (curpage - 1)
					+ "'><p style=\"padding: 0px\"> prev </p></a>");
		}	
		if(request.getParameter("page")!=null) {
			curpage = Integer.parseInt(request.getParameter("page"));
		}
		if(request.getParameter("pagination")!=null) {
			number = Integer.parseInt(request.getParameter("pagination"));
		}
		out.println("</center>");	
		out.println("</form>");
	}
}
