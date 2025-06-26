package org.rosi.drivers.jetty ;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

public class HelloServlet extends HttpServlet {

   private String __init = "Not set yet" ;  

   public HelloServlet( ) {
   }
   public HelloServlet( String initParam ) {

        __init = initParam ;

   }
   public void doGet( HttpServletRequest request,
                              HttpServletResponse response ) throws ServletException,
                                                            IOException
   {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("<h1>Hello from HelloServlet : "+__init+"</h1>");
   }
}

