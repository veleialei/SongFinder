package servlets;

import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class SongFinderServer {
	private static final Server server = new Server(8140);	
	/**
	 * @param args
	 */

	public static void main(String[] args) throws Exception {
		// create a ServletHander to attach servlets
		ServletContextHandler servhandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		server.setHandler(servhandler);

		servhandler.addEventListener(new ServletContextListener() {

			public void contextDestroyed(ServletContextEvent sce) {
				// TODO Auto-generated method stub
			}

			// 在这里的哦，builder了data
			public void contextInitialized(ServletContextEvent sce) {
				sce.getServletContext().setAttribute(BaseServlet.DATA, new Data());
				sce.getServletContext().setAttribute(BaseServlet.LIBRARY, new Library());
			}
		});
		
		servhandler.addServlet(ArtistListServlet.class, "/artists_list");
		servhandler.addServlet(LoginServlet.class, "/login");
		servhandler.addServlet(VerifyUserServlet.class, "/verifyuser");
		servhandler.addServlet(SearchServlet.class, "/search");		
		servhandler.addServlet(InfoServlet.class, "/info");
		servhandler.addServlet(FavoriteListServlet.class, "/favorite_list");
		
		// set the list of handlers for the server
		server.setHandler(servhandler);
		// start the server
		server.start();
		server.join();
	}
    
	//shutdown the server
	//reference link:
	//http://www.randomactsofsentience.com/2012/01/stopping-andor-restarting-embedded.html
	public static void shutdown(HttpServletResponse response) {
		response.setStatus(202);
	    response.setContentType("text/plain");
		try {
			response.flushBuffer();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    try {
	      // Stop the server.
	      new Thread() {
	        @Override
	        public void run() {
	          try {
	            server.stop();
	          } catch (Exception e) { 
	            System.out.println("Some error: " + e.getMessage());
	          }
	        }
	      }.start();
	    } catch (Exception e) {
	    	System.out.println("Some error: " + e.getMessage());
	    	return;
	    }
	    return;
	}
}