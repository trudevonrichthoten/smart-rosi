package org.rosi.drivers.jetty;

import java.io.IOException;
import java.io.File;
 
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request ;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.http.HttpVersion ;
import org.eclipse.jetty.server.Connector ;
import org.eclipse.jetty.servlet.ServletContextHandler;



    public class MyServlet extends DefaultServlet {

    /* ------------------------------------------------------------ */
       public void doGet(HttpServletRequest request, HttpServletResponse response) 
           throws ServletException, IOException
       {
             
             String pathInfo    = request.getPathInfo() ;
             String contextPath = request.getContextPath() ;
             String requestURI  = request.getRequestURI() ;
             String servletPath = request.getServletPath() ;

             System.out.println("MyServlet : requestURI "+requestURI);

             if( requestURI.endsWith("xyz") ){
                 response.sendRedirect("/login.html"); 
                 return ;
             }else{
                 super.doGet(request,response);
             }
       }
    }
