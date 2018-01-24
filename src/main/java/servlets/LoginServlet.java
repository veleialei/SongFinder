package servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/*
 * Allows a user to log in
 */
public class LoginServlet extends BaseServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Data data = (Data) getServletConfig().getServletContext().getAttribute(DATA);
		HttpSession session = request.getSession();

		// if user is logged in, redirect

		String status = getParameterValue(request, STATUS);

		boolean statusok = status != null && (status.equals("empty_input") || status.equals("wrong_signin") 
				|| status.equals("duplicated_username") || status.equals("password_mismatch")) ? false : true;

		// output text box requesting user name
		PrintWriter out = prepareResponse(response);
		
		out.println(header("Login Page"));
		out.println("<center>");
		// if the user was redirected here as a result of an error
		if (!statusok) {
			out.println("<h1><i class=\"fa fa-user\"></i>Invalid Request to Signin/Signup</h1>");
		}else {
			out.println("<h1><i class=\"fa fa-user\"></i>Please Signin/Signup</h1>");
		}

		out.println("<form name=\"name\" action=\"verifyuser\" method=\"post\">");
		out.println(
				"<button name=\"home\" value = \"search\" style=\"float:right; margin-top:-70px; margin-right:10px\" class=\"fa fa-home\"></button>");

		//login part
		out.println("<center>");
		out.println("<h2>Sign In</h2>");
		out.println("</center>");

		out.println("<div>Username:");
		out.println("<input style=\"margin-right:90px;\" type=\"text\" name=\"name_in\"/></div>");
		out.println("<div>Password:");
		out.println("<input style=\"margin-right:88px\" type=\"password\" name=\"password_in\"/></div>");
		out.println("<div><input type=\"submit\" value=\"Signin\"/></div>");
		if(status.equals("wrong_signin")) {
			out.println("<h4>Your password or username is wrong<h4/>");
		}
		out.println("<hr />");

		//register part
		out.println("<center>");
		out.println("<h2>Sign Up</h2>");
		out.println("</center>");

		out.println("<div>Username:");
		out.println("<input style=\"margin-right:90px;\" type=\"text\" name=\"name_up\"/></div>");
		out.println("<div>Password:");
		out.println("<input style=\"margin-right:88px;\" type=\"password\" name=\"password_up_1\"/></div>");
		out.println("Re-enter Password:");
		out.println("<input style=\"margin-right:160px;\" type=\"password\" name=\"password_up_2\"/></div>");
		out.println("<div><input type=\"submit\" value=\"Signup\"/></div>");
		if(status.equals("password_mismatch")) {
			out.println("<h4>The passwords you enter is not the same<h4/>");
		}else if(status.equals("duplicated_username")){
			out.println("<h4>The username is in use, please choose another name<h4/>");
		}
		out.println("</form");

		out.println(footer());
	}
}
