package org.rosi.execution.modules ;

import java.util.* ;
import java.text.SimpleDateFormat ;
import java.io.* ;
import java.net.InetSocketAddress;

import org.rosi.util.*;
import org.rosi.execution.*;
import org.rosi.drivers.jetty.*;

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



public class JettyActionModule extends RosiModule {

   private RosiCommandProcessor _commandProcessor = null ; 
   private ModuleContext        _context     = null ;
   private SimpleDateFormat     _sdf         = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
   private File                 _file        = null ;
   private PatternTranslator    _inFilter    = null ;
   private PatternTranslator    _outFilter   = null ;
   private long                 _sleepMillis = 10000L ;

    private int _port = 8080 ;

    private String _filesystemRoot = "." ;
    private String _serviceMapping = "/*" ;
    private String _setterDirectory = "/tmp" ;
    private String _getterDirectory = "/tmp" ;



   public JettyActionModule( String moduleName , ModuleContext context  )
      throws Exception {

      super(moduleName,context);

      _context = context ;

      log( "Started");
 
      String portName  = context.get("port" , true ); 
      _serviceMapping  = context.get("mapping" , true ) ;
      _setterDirectory = context.get("setterDirectory" , true ) ;
      _getterDirectory = context.get("getterDirectory" , true ) ;
      _filesystemRoot  = context.get("staticRoot" , true ) ;

      _port = Integer.parseInt( portName ) ;

      log( "Context created with the following settings ");
      log( "              port " +_port);
      log( "        staticRoot " +_filesystemRoot);
      log( "           mapping " +_serviceMapping);
      log( "   setterDirectory " +_setterDirectory);
      log( "   getterDirectory " +_getterDirectory);
/*
      String sleepTime = context.get("reveiveSMSUpdateTime");
      if( sleepTime != null ){
          _sleepMillis = Long.parseLong(sleepTime ) * 1000L ;
      }
      log( "Receive SMS Update Time set to : "+_sleepMillis+" millis");
*/
      String filterFile = context.get( "inFilterFile" ) ;
      if( filterFile != null ){
         File f = new File( filterFile ) ;
         if( ! f.exists() )
           throw new
           IllegalArgumentException("'inFilterFile' file not found : "+ filterFile ) ;

         _inFilter = new PatternTranslator( f ) ;
      }
      filterFile = context.get( "outFilterFile" ) ;
      if( filterFile != null ){
         File f = new File( filterFile ) ;
         if( ! f.exists() )
           throw new
           IllegalArgumentException("'outFilterFile' file not found : "+ filterFile ) ;

         _outFilter = new PatternTranslator( f ) ;
      }

      start() ;

   } 
   public void start() throws Exception {

        //Server server = new Server( new InetSocketAddress( "rosi-control-3" , _port ) );
        Server server = new Server(  _port  );

        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(false);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });
        resource_handler.setRedirectWelcome(true);
        resource_handler.setResourceBase( _filesystemRoot );


        ServletHolder holder = new ServletHolder(
            new FilesystemServlet( _getterDirectory , _setterDirectory )
        ) ;

        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping( holder , _serviceMapping );

        Handler [] x = { resource_handler , handler } ;
        //Handler [] x = { resource_handler , new DefaultHandler() } ;

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
   private Thread _networkToRosiThread = null ;
   private Thread _rosiToNetworkThread = null ;
   private synchronized void shutdown(){

       if( _networkToRosiThread != null )_networkToRosiThread.interrupt() ;
       _networkToRosiThread = null ;

       if( _rosiToNetworkThread != null )_rosiToNetworkThread.interrupt() ;
       _rosiToNetworkThread = null ;

   }
   public synchronized void run(){
/* 
      _networkToRosiThread = 
      new Thread(
          new Runnable(){
              public void run(){
                 runNetworkToRosi() ;
              }
          }
      ); 
      _networkToRosiThread.start() ;

      _rosiToNetworkThread = 
      new Thread(
          new Runnable(){
              public void run(){
                 runRosiToNetwork() ;

              }
          }
      ) ;
      _rosiToNetworkThread.start() ;
*/
   }
   public void runNetworkToRosi(){
   }
   public void runRosiToNetwork(){

       log(" started : RosiToNetwork");

       while( ! Thread.interrupted() ){

          try{

             RosiCommand c  = take() ;
 
             if( ! ( c instanceof RosiSetterCommand ) ){
                 errorLog("Received an unexpected command type : "+c.getClass().getName() ) ;
                 continue ;
             }

             RosiSetterCommand command = new RosiSetterCommand( (RosiSetterCommand) c ) ; 

             debug("Jetty received from dispatcher : "+command);
             if( _inFilter != null ){

                String [] sub = _inFilter.translate( command.getKey()+":"+command.getValue() ) ;

                if( ( sub == null ) || ( sub.length == 0 ) )continue ;

                for( int i = 0 ; i < sub.length ; i++ ){
                  debug( "SUB["+i+"] : >"+sub[i]+"<");
                }
                command.setKey( sub[0] ) ;
                if( sub.length > 1 )command.setValue( sub[1] ) ;
 
                if( sub.length > 2 ){
                }else{
                } 
 
             }

          }catch(InterruptedException ieee ){
             errorLog("runRosiToNetwork: Was interrupted and stopped" ) ;
             break ;
          }catch(Exception eee ){
             errorLog( "runRosiToNetwork: Got exeception in main loop: "+eee ) ;
             eee.printStackTrace();
             break ;
          }
       }

       errorLog("runRosiToNetwork requests shutdown");
       shutdown() ;

   }
   public void setCommandProcessor( RosiCommandProcessor commandProcessor ){
     _commandProcessor = commandProcessor ;
   }
}
