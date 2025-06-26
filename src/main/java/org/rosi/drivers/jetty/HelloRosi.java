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

package org.rosi.drivers.jetty ;

import java.io.*;
import java.util.* ;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.*;
import net.sf.json.util.*;
/* ------------------------------------------------------------ */
/** Dump Servlet Request.
 * 
 */
public class HelloRosi extends HttpServlet
{

    private static boolean __active = false ;
    private static String  __filesystemRootPath = null ;
    private static File    __rootDirectory      = null ;
    /* ------------------------------------------------------------ */
    public HelloRosi( String filesystemPath ){
        __filesystemRootPath = filesystemPath ;
    }
    /* ------------------------------------------------------------ */
    public void init(ServletConfig config) throws ServletException
    /* ------------------------------------------------------------ */
    {
    	super.init(config);

        System.out.println( "--------- Rosi Init ----------------------" );
        /* ------------------------------------------------------------ */
        if( __filesystemRootPath == null ){

           for( Enumeration<String> e =  config.getInitParameterNames() ; e.hasMoreElements();) {

             String name = e.nextElement() ;
             System.out.println("  "+name+" : "+config.getInitParameter( name ) ) ; 

           }
           __filesystemRootPath = config.getInitParameter( "FilesystemPath" );
           if( __filesystemRootPath == null ){

              System.err.println("ERROR : FilesystemPath not specified, servlet disabled");
              __active = false ;

           }

        }
        __rootDirectory = new File( __filesystemRootPath ) ;
        if( ( ! __rootDirectory.isDirectory() ) || ( ! __rootDirectory.exists() ) ){
           System.err.println("ERROR : FilesystemPath is not a directory or doesn't exist.");
           __active = false ;
        }
        System.out.println( "-----------------------------------" );
        __active = true ;
    }

    /* ------------------------------------------------------------ */
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException
    {
        doGet(request, response);
    }

    /* ------------------------------------------------------------ */
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException
    {

        String base = request.getContextPath() + request.getServletPath() ;

        int baseLength = base.length() ;
        int URILength  = request.getRequestURI().length() ;
  
        String ourURI = URILength <= baseLength ? "" : request.getRequestURI().substring( baseLength ) ;

        System.out.println("Base :  "+base);
        System.out.println("RequestURI :  "+request.getRequestURI() );
         
        ServletOutputStream out = response.getOutputStream();

        ourURI = request.getRequestURI() ;
        String [] commands = ourURI.split("/");

        if( commands.length < 2 ){
           out.flush();
           return ;
        }

        String command = commands[2] ;
        System.out.println(" Comamnd : "+command);
        if( command.equals( "json" ) ){
           if( request.getMethod().equals("POST") ){
               response.setContentType("application/json");
               BufferedReader reader = request.getReader() ;
               String in = null ;
               StringBuffer sb = new StringBuffer();
               while( ( in = reader.readLine() ) != null )sb.append(in);
               System.out.println("POST : "+sb.toString());
               try{
                  processPostJson( sb.toString() , out ) ;
               }catch( JSONException jsonEx ){
                  out.println("{ \"result\" : \"-1\" , \"errorMessage\" : \""+jsonEx.getMessage()+"\" }");
               }
           }else{
               out.println("{ \"result\" : \"Illegal Method : "+request.getMethod()+"\"  }" );
           }
        }else if( command.equals( "webresult" ) ){
           response.setContentType("text/html");
           if( request.getMethod().equals("POST") ){
               BufferedReader reader = request.getReader() ;
               String in = null ;
               StringBuffer sb = new StringBuffer();
               while( ( in = reader.readLine() ) != null )sb.append(in);
               dumpString( out, "POST" , sb.toString() ) ; 
            }else{
               dumpString( out,  "GET" , request.getQueryString() ) ; 
            }
        }else if( command.equals( "web" ) ){

           response.setContentType("text/html");
           out.println("<html>");
           out.println("<h1>Hello Rosi.</h1>");
           out.println("<form action=\"/otto/rosi/webresult/submit.doit\" method=\"post\">");
           out.println("  First name:<br>");
           out.println("  <input type=\"text\" name=\"firstname\"><br>");
           out.println("  Last name:<br>");
           out.println("  <input type=\"text\" name=\"lastname\">");
           out.println(" <br>");
           out.println("  <input type=\"radio\" name=\"gender\" value=\"male\" checked> Male<br>");
           out.println("  <input type=\"radio\" name=\"gender\" value=\"female\"> Female<br>");
           out.println("  <input type=\"radio\" name=\"gender\" value=\"other\"> Other");
           out.println(" <br>");
           out.println("  <input type=\"submit\" value=\"Submit\">");

           out.println("</form>");
           out.println("</html>");

        }else{

           response.setContentType("text/html");

           out.println("<html>");
           out.println("<h1>Hello Trude</h1>");
           out.println("<h2>"+request.getContextPath()+"</h2>");
           out.println("<h2>"+request.getRequestURL()+"</h2>");
           out.println("<h2>"+request.getRequestURI()+"</h2>");
           out.println("<h2>"+request.getServletPath()+"</h2>");
           out.println("<h2> $"+command+"$</h2>");
           out.println("<a href=\"/x.html\">This is a link</a>");
           out.println("<hr>");
           out.println("<address>Copyright Trude von Richthofen (c)</address>");
           out.println("</html>");
        }
        out.flush();
    }
    private void dumpString( ServletOutputStream out , String header , String content )
       throws IOException {
       out.println("<html>");
       out.println("<h1>"+header+"</h1>");
       out.println("<pre>");
       out.println(content);
       out.println("</pre>");
       out.println("</html>");
    }
    private void processPostJson( String in , ServletOutputStream out ) throws IOException,JSONException {

       System.out.println("Message Received : "+in); 
       JSONTokener tok = new JSONTokener(in) ;
       JSONObject obj = (JSONObject)tok.nextValue();

       try{


          JSONArray list = obj.getJSONArray("list");
          if( list == null )
            throw new
            IllegalArgumentException("No list entry found in message");

          for( int i = 0 ; i < list.size() ; i++ ){

               JSONObject x = (JSONObject)list.get(i) ;

               try{
                  String method = x.getString("method");
                  String name   = x.getString("name");
                  if( method.equals("set") ){
                     String value  = x.getString("value");
                     setAttribute( name , value ) ;
                     System.out.println("Setting : "+name+" -> "+value ) ;
                  }else if( method.equals("get") ){
                     String value = getAttribute( name ) ;
                     x.put( "value" , value );
                     System.out.println("Getting : "+name+" -> "+value ) ;
                  }else{
                     throw new
                     IllegalArgumentException("Unsupported method : "+method);
                  }
                  x.put( "result" , 0 ) ;
               }catch( JSONException notfound ){
                  System.out.println("JSONException : "+notfound.getMessage());
                  x.put( "result" , -1 ) ;
                  x.put( "errorMessage" , notfound.getMessage() ) ;
               }
           }
           obj.put( "result" , 0 ) ;
       }catch( Exception eeii){
           obj.put( "result" , -1 ) ;
           obj.put( "errorMessage" , eeii.getMessage() ) ;
       }
       System.out.println("Sending Message : "+obj.toString());
       out.println(obj.toString());

    }

    private void setAttribute( String name , String value ){

       File f = new File( __rootDirectory , name ) ;
       try{

          PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(f)));

          try{
             pw.println(value) ;
          }finally{
             try{  pw.close() ; }catch(Exception ce){}
          }
       }catch(IOException ee ){
          System.err.println("IOError : "+ee.getMessage());
       }

    }
    private String getAttribute( String name )  {

       File f = new File( __rootDirectory , name ) ;
       if( ( ! f.exists() ) || ( ! f.isFile() ) )
          throw new
          IllegalArgumentException("Argument not found : "+name );
       try{

          BufferedReader br = new BufferedReader(new FileReader(f)) ;

          try{

             String in = br.readLine() ;
             if( in == null )
               throw new
               IllegalArgumentException("Internal problem reading : "+name );

             return in.trim() ;

          }finally{
             try{  br.close() ; }catch(Exception ce){}
          }
       }catch(IOException ee ){
          System.err.println("IOError : "+ee.getMessage());
          throw new
          IllegalArgumentException("Internal problem reading : "+name );
       }

    }
    
 
    
}
