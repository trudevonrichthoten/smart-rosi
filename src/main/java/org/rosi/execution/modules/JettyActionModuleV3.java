package org.rosi.execution.modules ;

import java.util.* ;
import java.text.SimpleDateFormat ;
import java.io.* ;
import java.net.InetSocketAddress;

import org.rosi.util.*;
import org.rosi.execution.*;
import org.rosi.drivers.jetty.*;
import org.rosi.drivers.generic.*;

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
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.resource.PathResource;



public class JettyActionModuleV3 extends GenericModule implements XGenericDriver {

   private RosiCommandProcessor _commandProcessor = null ; 
   private ModuleContext        _context     = null ;
   private SimpleDateFormat     _sdf         = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
   private File                 _file        = null ;
   private PatternTranslator    _inFilter    = null ;
   private PatternTranslator    _outFilter   = null ;
   private long                 _sleepMillis = 10000L ;

    private int _port = 8080 ;

    private String _filesystemRoot = "." ;
    private String _dataRoot       = "/data" ;
    private String _dirServletMapping       = "/dir" ;
    private String _memoryServletMapping    = "/mem" ;
    private String _setterDirectory = "/tmp" ;
    private String _getterDirectory = "/tmp" ;
    private MemoryKeyValueManager  _memory  = new MemoryKeyValueManager() ;


   public JettyActionModuleV3( String moduleName , ModuleContext context  )
      throws Exception {

      super(moduleName,context);

      _context = context ;

      setThreads( true , false , false ) ;
      setDriver(this);

      log( "Started");
 
      String portName        = context.get("port" , true ); 
      _dirServletMapping     = context.get("directoryServletMapping" , true ) ;
      _memoryServletMapping  = context.get("memoryServletMapping" , true ) ;

      _setterDirectory = context.get("setterDirectory" , true ) ;
      _getterDirectory = context.get("getterDirectory" , true ) ;
      _filesystemRoot  = context.get("staticRoot" , true ) ;
      _dataRoot        = context.get("dataRoot" , true ) ;

      _port = Integer.parseInt( portName ) ;

      log( "Context created with the following settings (V3) ");
      log( "              port " +_port);
      log( "        staticRoot " +_filesystemRoot);
      log( "          dataRoot " +_dataRoot);
      log( "       dir mapping " +_dirServletMapping);
      log( "    memory mapping " +_memoryServletMapping);
      log( "   setterDirectory " +_setterDirectory);
      log( "   getterDirectory " +_getterDirectory);

      start() ;

   } 
   /* 
    * From abstract class GenericModule
    */
   public void initialize() throws Exception {
   }
   public boolean handleException( Exception e ) {
      return true ;
   }
   /*
    * Implementing generic driver.
    */
   public void update() throws Exception {
   } 
   public List<String> getDeviceNames()  throws Exception {
       throw new Exception("Not supported");
   }
   public Map<String,String> getDeviceAttributes( String deviceName ) throws Exception {
       throw new Exception("Not supported");
   }
   public void setDeviceAttribute( String deviceName , String attribute , String value ) 
     throws Exception {
 
       _memory.add( attribute , value ) ;

   }
   public void start() throws Exception {

        //Server server = new Server( new InetSocketAddress( "rosi-control-3" , _port ) );
        Server server = new Server(  _port  );

        ResourceHandler resource_handler1 = new ResourceHandler();
        resource_handler1.setDirectoriesListed(false);
        resource_handler1.setWelcomeFiles(new String[]{ "index.html" });
        resource_handler1.setRedirectWelcome(true);
        // resource_handler1.setResourceBase( _filesystemRoot );

        ContextHandler context1 = new ContextHandler();
        context1.setContextPath( "/" );
        context1.setBaseResource( new PathResource(new File(_filesystemRoot)) );
        context1.setHandler(resource_handler1);

        ResourceHandler resource_handler2 = new ResourceHandler();
        resource_handler2.setDirectoriesListed(false);
        resource_handler2.setWelcomeFiles(new String[]{ "index.html" });
        resource_handler2.setRedirectWelcome(true);
        // resource_handler2.setResourceBase( _filesystemRoot );

        ContextHandler context2 = new ContextHandler();
        context2.setContextPath( "/data" );
        context2.setBaseResource( new PathResource(new File(_dataRoot)) );
        context2.setHandler(resource_handler2);


        ServletHolder holder1 = new ServletHolder(
            new FilesystemServlet( _getterDirectory , _setterDirectory )
        ) ;
        ServletHolder holder2 = new ServletHolder(
            new MemoryServlet( JettyActionModuleV3.this , _memory )
        ) ;

        ServletHandler handler = new ServletHandler();

        handler.addServletWithMapping( holder1 , _dirServletMapping );
        handler.addServletWithMapping( holder2 , _memoryServletMapping );

        Handler [] x = { context1 , context2 , handler , new DefaultHandler() } ;

        HandlerList list = new HandlerList() ;
        list.setHandlers(x);

        server.setHandler(list);

        server.start();
   }
   private String buildMessage( String key , String value ){
  
      StringBuffer sb = new StringBuffer() ;
 
      sb.append(_sdf.format( new Date() )).append(";").
         append(key).append(";").
         append(value).append(";");

      return sb.toString();

   } 
}
