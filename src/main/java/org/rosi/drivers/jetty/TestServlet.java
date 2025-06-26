// ========================================================================
// Copyright 1996-2005 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================
/*
 * 
 * Useful links 
 * Airports : https://en.wikipedia.org/wiki/List_of_airports_by_IATA_code:_D
 */
package org.rosi.drivers.jetty ;

import java.io.*;
import java.util.* ;
import java.text.* ;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession ;

import com.mongodb.* ;
import com.mongodb.client.* ;
import com.mongodb.client.result.* ;
import org.bson.*;
import org.bson.types.*;
import org.bson.types.*;
import com.mongodb.client.model.UpdateOptions ;

import java.time.* ;
import java.time.format.* ;


/* ------------------------------------------------------------ */
/** Dump Servlet Request.
 * 
 */
public class TestServlet extends HttpServlet
{
    private String _servletName = "unknown" ;
/* --------------------------------------------------------------------------- */
    public TestServlet( String database )
/* --------------------------------------------------------------------------- */
       throws IllegalArgumentException {
       
       _servletName = database ;
       
       System.out.println("This is servlet : "+database);
    
    }
/* --------------------------------------------------------------------------- */
    public void init(ServletConfig config) throws ServletException
/* --------------------------------------------------------------------------- */
    {
    	super.init(config);
    }
/* --------------------------------------------------------------------------- */
    public void doPut(HttpServletRequest request, HttpServletResponse response) 
/* --------------------------------------------------------------------------- */
        throws ServletException, IOException
    {
        String ourURI = request.getPathInfo() ;
        String [] commands = ourURI.split("/");
        //
        if( commands.length < 2 ){
           response.sendError(
               HttpServletResponse.SC_BAD_REQUEST,
               "Insufficient Number of Arguments in "+_servletName);
           return ;
        }
        
        String command  = commands[1] ;
                
        if( command.equals( "trip" ) ){
 
       }else if( command.equals( "accounting" ) ){

        }else{
           response.sendError(
               HttpServletResponse.SC_BAD_REQUEST,
               "Invalid argument: "+command);
        }
    }
/* --------------------------------------------------------------------------- */
    public void doDelete(HttpServletRequest request, HttpServletResponse response) 
/* --------------------------------------------------------------------------- */
        throws ServletException, IOException
    {
        String uri         = request.getPathInfo() ;
        String [] commands = uri.split("/");
        //
        //   /id/[trip|ticket|accomodation|aconfernece]/<objectId>
        //
        if( ( commands.length < 4 ) || ( ! commands[1].equals("id") ) ){
           response.sendError(HttpServletResponse.SC_NOT_FOUND);
           return ;
        }     

        String collectionString  = commands[2] ;
        String objIdString       = commands[3] ;

        try{

        }catch( Exception jsonEx ){
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                jsonEx.getMessage());        
        }

 
    }
/* -------------------------------------------------------------------------- */
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
/* -------------------------------------------------------------------------- */
        throws ServletException, IOException
    {
        
        String uri         = request.getPathInfo() ;
        String contextPath = request.getContextPath() ;
        String requestURI  = request.getRequestURI() ;
        String getServletPath = request.getServletPath() ;
        
        StringBuffer sb = new StringBuffer();
        
        printHeader(sb, null );
        printRequest(request,sb);
        
        String [] commands = uri == null ? new String[]{ ""} : uri.split("/");
        String command   = commands.length < 2 ? "" : commands[1] ;
        //
        //
        
        if( uri == null ){
           printMessage(sb, "pathInfo (uri) is null");      
        }else if( command.equals( "" )  ){
           printMessage(sb, "pathInfo length : "+commands.length);      
        }else if( command.equals( "bad" )  ){
            
                response.sendError(
                   HttpServletResponse.SC_BAD_REQUEST,
                   "Bad request from : "+_servletName);
                
        }else if( command.endsWith( "silent" )  ){
            
            if( ! checkAAI( request , response , "role" ) )return ;
            printMessage(sb, "Silent authenticated !");
            
        }else if( command.endsWith( "logoff" )  ){
           HttpSession session = request.getSession(true) ;
           if( session != null )session.invalidate() ;
           
       }else if( command.endsWith( "session" )  ){
            
           HttpSession session = request.getSession(false) ;
            //session.invalidate() ;
            //response.sendRedirect("/login.html"); 
            
            
        }else{
           printMessage(sb, "Not found : >"+command+"<");      
        }
        printTrailer(sb);
        ServletOutputStream out = response.getOutputStream();
        out.println( sb.toString() ) ;
        out.flush() ;

   }        
   public void printHeader( StringBuffer sb , String message  ) 
      throws  ServletException, IOException {
          
            sb.append("<html><head><title>").append(_servletName).append("</title></head>\n");
            sb.append("<body class=\"test\"><h1class=\"test\">");
            sb.append("<h1>Servlet Name : ").append(_servletName).append("</h1>");
            if(message!=null)sb.append("<h2 class=\"test\">Message : ").append(message).append("</h2>");
   }
   public void printTrailer( StringBuffer sb ) 
      throws  ServletException, IOException {
            sb.append("</body></html>");
   }
   public void printMessage( StringBuffer sb , String message ) 
      throws  ServletException, IOException {
            sb.append("<h2 class=\"test\">").append(message).append("</h2>\n");
   }
   public void printRequest( HttpServletRequest request , StringBuffer sb ){
       
        String pathInfo    = request.getPathInfo() ;
        String contextPath = request.getContextPath() ;
        String requestURI  = request.getRequestURI() ;
        String servletPath = request.getServletPath() ;
                       
        sb.append("<table border=1 class=\"servlet-params\">\n");
        sb.append("<tr class=\"servlet-params\">\n");
           sb.append("<td class=\"servlet-params\">pathInfo</td>\n");
           sb.append("<td class=\"servlet-params\">\n").
              append(pathInfo).
              append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr class=\"servlet-params\">\n");
           sb.append("<td class=\"servlet-params\">contextPath</td>\n");
           sb.append("<td class=\"servlet-params\">\n").
              append(contextPath).
              append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr class=\"servlet-params\">\n");
           sb.append("<td class=\"servlet-params\">requestURI</td>\n");
           sb.append("<td class=\"servlet-params\">\n").
              append(requestURI).
              append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr class=\"servlet-params\">\n");
           sb.append("<td class=\"servlet-params\">servletPath</td>\n");
           sb.append("<td class=\"servlet-params\">\n").
              append(servletPath).
              append("</td>\n");
        sb.append("</tr>\n");
        HttpSession session = request.getSession(false) ;    
        sb.append("<tr class=\"servlet-params\">\n");
           sb.append("<td class=\"servlet-params\">Session</td>\n");
           sb.append("<td class=\"servlet-params\">\n").
              append(session==null?"none":session.toString()).
              append("</td>\n");
        sb.append("</tr>\n");
        Enumeration<String> en = request.getHeaderNames() ;
        while( en.hasMoreElements() ){
           String name = en.nextElement() ;
           String value = request.getHeader(name) ;
           sb.append("<tr class=\"servlet-params\">\n");
               sb.append("<td class=\"servlet-params\">Header: ").append(name).append("</td>\n");
               sb.append("<td class=\"servlet-params\">\n").
                  append(value==null?"[null]":value).
                  append("</td>\n");
            sb.append("</tr>\n");
         }
        sb.append("</table>\n");
        return ;
   }
/* -------------------------------------------------------------------------- */
    private boolean checkAAI( HttpServletRequest request, 
                              HttpServletResponse response ,
                              String requiredRole             )
/* -------------------------------------------------------------------------- */
    
        throws ServletException, IOException
    {
        HttpSession session = request.getSession(false) ;
        if( session != null )return true ;
        
        String client = request.getHeader("User-Agent") ;
        System.out.println("Agent : "+client);
        
        String authorization = request.getHeader("Authorization") ;
        if( authorization != null ){
            
            System.out.println("Auth : "+authorization );        
            if(  authorization.startsWith("Basic") ){
    
                String base64Credentials = authorization.substring("Basic".length()).trim();
                String credentials = 
                     new String(   Base64.getDecoder().decode(base64Credentials) /* ,
                                   Charset.forName("UTF-8") */
                               );
                String[] values = credentials.split(":",2);
                System.out.println("User : "+values[0]+" Password : "+values[1] );
                
                if( values[1].equals("rosi") )return true ;
            }
            
        }
        response.reset() ;
        System.out.println("NOT AUTHENTICATDED");
        if( ( client.indexOf("Chrome")  > -1 ) ||
            ( client.indexOf("Mozilla") > -1 ) ||
            ( client.indexOf("Safari")  > -1 )    ){
           response.sendRedirect("/static/login.html?r="+request.getRequestURI());
        }else{
           response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
        
        return false ;
        
    }
/* ---------------------------------------------------------------------------- */
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
/* ---------------------------------------------------------------------------- */
        throws ServletException, IOException
    {
        String uri         = request.getPathInfo() ;
        String [] commands = uri.split("/");
        //
        //    L O G I N
        //
        if( ( commands.length > 1 ) && commands[1].equals("login") ){
        /* ----------------------------------------------------------- */ 
        
            Map<String,String> map = getPostedMap( request ) ;

            String username = map.get("username");
            String password = map.get("password");
          
            if( ! (
                  ( username != null ) && 
                  ( password != null ) &&
                  checkAuthentication( username , password )
                  )
              ){
            
               response.reset() ;
               response.sendRedirect( "/static/login.html");
               return ;
               
            }
            
            HttpSession session = request.getSession(true) ;

            //    username=<username>
            //    password=<password>
            //    redirect=http://../static/login.html?...&r=/silent&...
            //
            String goToLocation = map.get("redirect") ;
            //
            if( goToLocation != null ){
                
                 goToLocation = java.net.URLDecoder.decode( goToLocation ,"UTF-8") ;
                 System.out.println("Goto Location Request : "+goToLocation);
                 String [] locationArray = goToLocation.split("\\?");
                 if( locationArray.length > 1 ){
                     String URIpart   = locationArray[0] ;
                     String parameter = locationArray[1] ;
                     
                     Map<String,String> m = stringToMap(parameter) ;

                     String redirectString = m.get("r") ;
                     if( redirectString != null ){
                         System.out.println("REDIRECT : "+redirectString);
                         response.sendRedirect( redirectString ) ;
                         return ;
                     }
                 }
            }
            response.sendRedirect( "/" ) ;
            return ;
        }
 

    }
/* ---------------------------------------------------------------------------- */
    private boolean checkAuthentication( String username , String password ){
/* ---------------------------------------------------------------------------- */
           return password.equals("rosi");
    }
/* ---------------------------------------------------------------------------- */
    private Map<String,String> getPostedMap( HttpServletRequest request )
     throws IOException {
/* ---------------------------------------------------------------------------- */
            BufferedReader reader = request.getReader() ;
            String in = null ;
            StringBuffer sb = new StringBuffer();
            while( ( in = reader.readLine() ) != null )sb.append(in);
            
            System.out.println("POST : "+sb.toString());
            
            return stringToMap( sb.toString() ) ;
    }
/* ---------------------------------------------------------------------------- */
    private Map<String,String>  stringToMap( String in ){
/* ---------------------------------------------------------------------------- */
        
        String [] x = in.split("&");
        Map<String,String> map = new HashMap<String,String>() ;
        for( int i = 0 ; i < x.length ; i++ ){
            String [] y = x[i].split("=");
            if( y.length == 1 )map.put( y[0] , "*" ) ;
            if( y.length > 1 )map.put( y[0] , y[1] ) ;
        }
        return map ;
        
    }

}
