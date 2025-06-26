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
import static java.util.Arrays.asList;


/* ------------------------------------------------------------ */
/** 
 *      Admin Servlet Request.
 */
/* ------------------------------------------------------------ */
public class AdminServlet extends HttpServlet
/* ------------------------------------------------------------ */
{
    private String                     _servletName = "unknown" ;    
    private MongoAuthenticationHandler _authHandler = null ;

/* --------------------------------------------------------------------------- */
    public AdminServlet( String database )
       throws IllegalArgumentException {
/* --------------------------------------------------------------------------- */
       
       _servletName = database ;
       
       System.out.println("Admin Database : "+database);

       _authHandler = new MongoAuthenticationHandler( database ) ;
    
    }
/* --------------------------------------------------------------------------- */
    public void init(ServletConfig config) throws ServletException
/* --------------------------------------------------------------------------- */
    {
    	super.init(config);
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
        
        String [] commands = uri == null ? new String[]{ ""} : uri.split("/");
        String command   = commands.length < 2 ? "" : commands[1] ;
        //
        // 
        if( command.equals("") ){
            
            response.sendRedirect("/static/index.html");
            
        }else if( command.equals( "logout" ) || command.equals( "logoff" ) ){
            
           HttpSession session = request.getSession(false) ;
           if( session != null )session.invalidate() ;
           
        }else if( command.equals( "info" )  ){
            
            
           StringBuffer sb = new StringBuffer();
        
           printHeader(sb, null );
           printRequest(request,sb);     
           printTrailer(sb);
           ServletOutputStream out = response.getOutputStream();
           out.println( sb.toString() ) ;
           out.flush() ;
            
        }else if( command.equals( "account" )  ){
            
            String account = commands.length < 3 ? "*" : commands[2] ;
     
            Document record = _authHandler.getAuthenticatedRecord( request ) ;
            if( record == null ){
                   response.reset() ;
                   response.sendError(HttpServletResponse.SC_FORBIDDEN,"Authentication failed.");
                   return ;
            }
            
            String username = record.getString("login") ;
            if( username == null ){
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return ;
            }
            
            boolean isAdminGroup = _authHandler.isInGroup( record , "admin" ) ;
            
            StringBuffer sb = new StringBuffer() ;

            if( account.equals("*") ){
                
                if( isAdminGroup ){
                    
                    MongoCursor<Document> cursor = _authHandler.userGetRecords() ;
                    
                    sb.append( "[ " ) ;
                    for( int i = 0 ; cursor.hasNext() ; i++ ){
                        Document d = cursor.next() ;
                        d.remove("password") ;
                        if( i != 0 )sb.append( " ,\n" ) ;
                        sb.append( d.toJson() ) ;
                    }
                    sb.append( "]\n" ) ;
                    
                }else{
                    
                    Document doc = _authHandler.userGetRecord(username) ;

                    sb.append( "[ " ) ;
                    if( doc != null ){
                        doc.remove("password") ; 
                        sb.append( doc.toJson() ) ;
                    }
                    sb.append( "]\n" ) ;
                }
           }else{
             
                if( account.equals("this") )account = username ;
                
                if( ( ! isAdminGroup ) && ( ! username.equals(account) ) ){
                   response.sendError(HttpServletResponse.SC_FORBIDDEN,"Permission Denied.");
                   return ;
                }
                Document doc = _authHandler.userGetRecord(account) ;
             
                if( doc == null ){
                   response.sendError(HttpServletResponse.SC_NOT_FOUND,"Account not found: "+account);
                   return ;
                }
                doc.remove("password") ;
                sb.append( doc.toJson() ) ;
            }

            response.setContentType("application/json");
            response.setStatus( HttpServletResponse.SC_OK ) ;

            ServletOutputStream out = response.getOutputStream();
            out.println(sb.toString());
            out.flush();

        }else if( command.equals( "jinfo" )  ){
            
            
           StringBuffer sb = new StringBuffer();
        
           HttpSession session = request.getSession(false) ;
           if( session != null ){
              String username = (String)session.getAttribute("username");
              String role     = (String)session.getAttribute("role");
              sb.append("[\n{ \"auth\" : \"true\" }");
              sb.append(",\n{ \"session\" : \"").append(session.toString()).append("\" }");
              sb.append(",\n{ \"username\" : \"").append(username).append("\" }");
              sb.append(",\n{ \"role\" : \"").append(role).append("\" }");
           }else{
              sb.append("[\n{ \"auth\" : \"false\" }");
           }

           Enumeration<String> en = request.getHeaderNames() ;
           while( en.hasMoreElements() ){
              String name = en.nextElement() ;
              String value = request.getHeader(name) ;
              sb.append(",\n{ \"").append(name).append("\" : ");
              sb.append("\"").append(value==null?"[null]":value).append("\" }");
           }
           sb.append("\n]\n");

           
           response.setContentType("application/json");
           response.setStatus(HttpServletResponse.SC_OK);
           
           ServletOutputStream out = response.getOutputStream();
           out.println( sb.toString() ) ;
           out.flush() ;
            
        }

   }        
/* ---------------------------------------------------------------------------- */
    public void doPut(HttpServletRequest request, HttpServletResponse response) 
/* ---------------------------------------------------------------------------- */
        throws ServletException, IOException
    {
        String uri         = request.getPathInfo() ;
        String [] commands = uri == null ? new String[0] : uri.split("/");
        //
        //    create account :  PUT /apps/admin/account/<name>
        //
        if( ( commands.length > 2 ) && commands[1].equals("account") ){
        /* ----------------------------------------------------------- */ 
            

             Document record = _authHandler.getAuthenticatedRecord( request ) ;
             if( record == null ){
                   response.reset() ;
                   response.sendError(HttpServletResponse.SC_FORBIDDEN,"Authentication failed.");
                   return ;
             }
             if( ! _authHandler.isInGroup( record , "admin" ) ){
                  response.sendError(HttpServletResponse.SC_FORBIDDEN,"Not in admin group");
                  return ;
             }
             String account = commands[2] ;
             
             try{
                   _authHandler.userAdd( account ) ;
             }catch( EntryAlreadyExistsException e ){
                   response.reset() ;
                   response.sendError(HttpServletResponse.SC_CONFLICT,e.getMessage());
                   return ;
             }
             response.setStatus(HttpServletResponse.SC_OK);
        }else{
            response.sendError(HttpServletResponse.SC_NOT_FOUND,"Not found : "+uri);
            return ;            
        }
   }    
/* ---------------------------------------------------------------------------- */
    public void doDelete(HttpServletRequest request, HttpServletResponse response) 
/* ---------------------------------------------------------------------------- */
        throws ServletException, IOException
    {
        String uri         = request.getPathInfo() ;
        String [] commands = uri == null ? new String[0] : uri.split("/");
        //
        //    delete account :  DELETE /apps/admin/account/<name>
        //
        if( ( commands.length > 2 ) && commands[1].equals("account") ){
        /* ----------------------------------------------------------- */ 
            

             Document record = _authHandler.getAuthenticatedRecord( request ) ;
             if( record == null ){
                   response.reset() ;
                   response.sendError(HttpServletResponse.SC_FORBIDDEN,"Authentication failed.");
                   return ;
             }

             String username = record.getString("login") ;
             if( username == null ){
                   response.reset() ;
                   response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                   return ;
             }
             
             String account = commands[2] ;

             if( ! ( username.equals(account) || _authHandler.isInGroup( record , "admin" ) ) ){
                  response.sendError(HttpServletResponse.SC_FORBIDDEN,"Not authorized.");
                  return ;
             }
             try{
                   _authHandler.userDelete( account ) ;
             }catch( EntryNotFoundException e ){
                   response.reset() ;
                   response.sendError(HttpServletResponse.SC_CONFLICT,"Account not found.");
                   return ;
             }

             response.setStatus(HttpServletResponse.SC_OK);
        }else{
            response.sendError(HttpServletResponse.SC_NOT_FOUND,"Not found : "+uri);
            return ;            
        }
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
                  ( _authHandler.checkAuthentication( username , password ) != null )
                  )
              ){
            
               response.reset() ;
               response.sendRedirect( "/static/login.html");
               return ;
               
            }
            
            HttpSession session = request.getSession(true) ;
            session.setAttribute("username",username);
            session.setAttribute("role","*");

            //    username=<username>
            //    password=<password>
            //    redirect=http://../static/login.html?...&r=/silent&...
            //
            String goToLocation = map.get("redirect") ;
            //
            if( goToLocation != null ){
                
                 goToLocation = java.net.URLDecoder.decode( goToLocation ,"UTF-8") ;
                 // System.out.println("Goto Location Request : "+goToLocation);
                 String [] locationArray = goToLocation.split("\\?");
                 if( locationArray.length > 1 ){
                     String URIpart   = locationArray[0] ;
                     String parameter = locationArray[1] ;
                     
                     Map<String,String> m = stringToMap(parameter) ;

                     String redirectString = m.get("r") ;
                     if( redirectString != null ){
                         // System.out.println("REDIRECT : "+redirectString);
                         response.sendRedirect( redirectString ) ;
                         return ;
                     }
                 }
            }
            response.sendRedirect( "/" ) ;
            return ;
        }else if( ( commands.length > 1 ) && commands[1].equals("jlogin") ){
        /* ................................................................. */
 
            Document doc = getPostedJson( request ) ;
            if( doc != null ){
                
                String username = doc.getString("username") ;
                String password = doc.getString("password");
           
                Document authDoc = null ;
                if( ! (
                      ( username != null ) && 
                      ( password != null ) &&
                      ( ( authDoc = _authHandler.checkAuthentication( username , password ) ) != null )
                      )
                  ){
                
                   response.reset() ;
                   response.sendError(HttpServletResponse.SC_FORBIDDEN);
                   return ;
                   
                }
                
                HttpSession session = request.getSession(true) ;
                session.setAttribute("account",authDoc);
                session.setAttribute("username",username);
                session.setAttribute("role","*");
                
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_OK);
                
                return ;
            }
        }else if( ( commands.length > 1 ) && commands[1].equals("jlogout") ){
        /* ................................................................. */
        
           HttpSession session = request.getSession(false) ;
           if( session != null )session.invalidate() ;
           
        }else if( ( commands.length > 1 ) && commands[1].equals("account") ){
        /* ................................................................. */
        
             Document record = _authHandler.getAuthenticatedRecord( request ) ;
             if( record == null ){
                   response.reset() ;
                   response.sendError(HttpServletResponse.SC_FORBIDDEN,"Authentication failed.");
                   return ;
             }

             String username = record.getString("login") ;
             if( username == null ){
                   response.reset() ;
                   response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                   return ;
             }
             
             String account = commands[2] ;

             boolean isAdminGroup = _authHandler.isInGroup( record , "admin" ) ;
             
             if( ! ( username.equals(account) || isAdminGroup ) ){
                  response.sendError(HttpServletResponse.SC_FORBIDDEN,"Not authorized.");
                  return ;
             }
             //
             //   ...userAdd( String account ) ;
             //   ...userDelete( String account ) ;
             //   ...userUpdateMetadata( String account , Document update )
             //   ...userUdpateGroups( String , Document groupDocument );
             //
             try{
                 
                Document posted = getPostedJson(request) ;
                Document group  = posted.get( "group" , org.bson.Document.class) ;
                
                if( ( group != null ) && ! isAdminGroup ){
                    response.sendError(HttpServletResponse.SC_FORBIDDEN,"Not authorized.");
                    return ;
                }

                try{
                    _authHandler.userUpdate( account , posted ) ;
                }catch(EntryNotFoundException enf ){
                    response.sendError(HttpServletResponse.SC_NOT_FOUND,enf.getMessage());
                    return ;
                }catch(IllegalArgumentException iat ){
                   response.sendError(HttpServletResponse.SC_BAD_REQUEST,iat.getMessage());
                    return ;
                }
                response.setStatus(HttpServletResponse.SC_OK);
                return ;

             }catch(Exception ee ){
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,ee.getMessage());
                return ;
             }
             
             
        }else{
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return ;
        }
 

    }
/* ---------------------------------------------------------------------------- */
    private Document getPostedJson( HttpServletRequest request )
    throws IOException {
/* ---------------------------------------------------------------------------- */
        
        BufferedReader reader = request.getReader() ;
        String in = null ;
        StringBuffer sb = new StringBuffer();
        
        while( ( in = reader.readLine() ) != null )sb.append(in);
        
        String jsonInput = sb.toString() ;
            
        return Document.parse(jsonInput) ;
          
    }
/* ---------------------------------------------------------------------------- */
    private boolean isBrowser( HttpServletRequest request ){
/* ---------------------------------------------------------------------------- */
        String client = request.getHeader("User-Agent") ;
                
        return ( client != null ) &&
               (
                   ( client.indexOf("Chrome")  > -1 ) ||
                   ( client.indexOf("Mozilla") > -1 ) ||
                   ( client.indexOf("Safari")  > -1 )   
               ) ;
               
    }
/* ---------------------------------------------------------------------------- */
    private Map<String,String> getPostedMap( HttpServletRequest request )
/* ---------------------------------------------------------------------------- */
     throws IOException 
    {
            BufferedReader reader = request.getReader() ;
            String in = null ;
            StringBuffer sb = new StringBuffer();
            while( ( in = reader.readLine() ) != null )sb.append(in);
            
            System.out.println("POST : "+sb.toString());
            
            return stringToMap( sb.toString() ) ;
    }
/* ---------------------------------------------------------------------------- */
    private Map<String,String>  stringToMap( String in )
/* ---------------------------------------------------------------------------- */
    {
        String [] x = in.split("&");
        Map<String,String> map = new HashMap<String,String>() ;
        for( int i = 0 ; i < x.length ; i++ ){
            String [] y = x[i].split("=");
            if( y.length == 1 )map.put( y[0] , "*" ) ;
            if( y.length > 1 )map.put( y[0] , y[1] ) ;
        }
        return map ;
        
    }
/* -------------------------------------------------------------------------- */
//            H E L P E R
/* -------------------------------------------------------------------------- */
   public void printHeader( StringBuffer sb , String message  ) 
/* .......................................................................... */
      throws  ServletException, IOException {
          
            sb.append("<html><head><title>").append(_servletName).append("</title></head>\n");
            sb.append("<body class=\"test\"><h1class=\"test\">");
            sb.append("<h1>Servlet Name : ").append(_servletName).append("</h1>");
            if(message!=null)sb.append("<h2 class=\"test\">Message : ").append(message).append("</h2>");
   }
   public void printTrailer( StringBuffer sb ) 
/* .......................................................................... */
      throws  ServletException, IOException {
            sb.append("</body></html>");
   }
   public void printMessage( StringBuffer sb , String message ) 
/* .......................................................................... */
      throws  ServletException, IOException {
            sb.append("<h2 class=\"test\">").append(message).append("</h2>\n");
   }
   public void printRequest( HttpServletRequest request , StringBuffer sb ){
/* .......................................................................... */
       
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
}
