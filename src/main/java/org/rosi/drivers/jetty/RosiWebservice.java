//
//  ========================================================================
//  Copyright (c) 1995-2016 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//
 
package org.rosi.drivers.jetty;
 
import java.io.IOException;
 
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

 
public class RosiWebservice {

    private int _port = 8080 ;

    private String _filesystemRoot = "." ;
    private String _serviceMapping = "/*" ;
    private String _setterDirectory = "/tmp" ;
    private String _getterDirectory = "/tmp" ;

    public static void main( String[] args ) throws Exception
    {

        if( args.length < 5 ){
             System.err.println("Usage : ... <port> <mapping> <resourceDir> <setterDir> <getterDir>");
             System.exit(4);
        }

        int port = Integer.parseInt( args[0] ) ;
        String mapping     = args[1] ;
        String resourceDir = args[2] ;
        String setterDir   = args[3] ;
        String getterDir   = args[4] ;
   
        RosiWebservice service = new RosiWebservice() ;

        service.setPort( port ) ; 
        service.setMapping( mapping ) ;
        service.setResourceDirectory( resourceDir ) ;
        service.setSetterDirectory( setterDir ) ;
        service.setGetterDirectory( getterDir ) ;

        service.start() ;

    }

    public void setPort( int port ){ _port = port ; }
    public void setSetterDirectory( String dir ){ _setterDirectory = dir ; }
    public void setGetterDirectory( String dir ){ _getterDirectory = dir ; }
    public void setMapping( String mapping ){ _serviceMapping = mapping ; }
    public void setResourceDirectory( String dir ){ _filesystemRoot = dir ; }

    public void start() throws Exception {

        Server server = new Server(_port);
 
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        resource_handler.setResourceBase( _filesystemRoot );

 
        ServletHolder holder = new ServletHolder( 
            new FilesystemServlet( _getterDirectory , _setterDirectory ) 
        ) ;

        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping( holder , _serviceMapping );

        Handler [] x = { resource_handler , handler } ;

        HandlerList list = new HandlerList() ;
        list.setHandlers(x);
 
        server.setHandler(list);

        server.start();
 
        //server.join();
    }
}
