package servlets;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/*
 * Servlet invoked at login.
 * Creates cookie and redirects to main ListServlet.
 */
public class VerifyUserServlet extends BaseServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// VerifyUser does not accept GET requests. Just redirect to login with error
		// status.
		response.sendRedirect(response.encodeRedirectURL("/login?" + STATUS + "=" + ERROR));
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		if (request.getParameter("home") != null) {
			response.sendRedirect(response.encodeRedirectURL("/search"));
			return;
		}
		String name_in = request.getParameter("name_in");
		String password_in = request.getParameter("password_in");
		String name_up = request.getParameter("name_up");
		String password_up_1 = request.getParameter("password_up_1");
		String password_up_2 = request.getParameter("password_up_2");
		
		if ((name_in == null || name_in.trim().equals("") || password_in == null || password_in.trim().equals("")) 
			&&	(name_up == null || name_up.trim().equals("") || password_up_1 == null || password_up_1.trim().equals("")
			|| password_up_2 == null || password_up_2.trim().equals(""))
				) {		
						
			response.sendRedirect(response.encodeRedirectURL("/login?" + STATUS + "=" + "empty_input"));
			return;
		}
		
		
		HttpSession session = request.getSession();

		// map id to name and userinfo
		Data data = (Data) getServletConfig().getServletContext().getAttribute(DATA);
		// we assume no username conflicts and provide no ability to register for our
		// service!
		if((name_in!=null && name_in.trim().length()>0) && !data.containsName(name_in)) {
			response.sendRedirect(response.encodeRedirectURL("/login?" + STATUS + "=" + "wrong_signin"));
			return;
		} else if ((name_in!=null && name_in.trim().length()>0) && !data.verifyName(name_in, password_in)) {
			response.sendRedirect(response.encodeRedirectURL("/login?" + STATUS + "=" + "wrong_signin"));
			return;
		} else if (data.containsName(name_up)) {
			response.sendRedirect(response.encodeRedirectURL("/login?" + STATUS + "=" + "duplicated_username"));
			return;
		} else if (!password_up_1.equals(password_up_2)) {
			response.sendRedirect(response.encodeRedirectURL("/login?" + STATUS + "=" + "password_mismatch"));
			return;
		} 
		
		
		if(name_in!=null && name_in.trim().length()>0) {
			session.setAttribute(NAME, name_in);			
		}else {
			session.setAttribute(NAME, name_up);
			data.addUser(name_up, password_up_1);
		}
		
		session.setAttribute(PRIVACY, "close");
		data.updateTotalVisit((String) session.getAttribute(NAME));
		data.updateLoginTime((String) session.getAttribute(NAME));
		// redirect to list
		response.sendRedirect(response.encodeRedirectURL("/search"));		
	}
}
