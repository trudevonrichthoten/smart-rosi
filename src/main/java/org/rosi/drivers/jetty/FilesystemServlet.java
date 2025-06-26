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
import java.util.regex.*;

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
public class FilesystemServlet extends HttpServlet
{

    private File  _getterDir = null ;
    private File  _setterDir = null ;

    /* ------------------------------------------------------------ */
    public FilesystemServlet( String getterDirectory , String setterDirectory )
       throws IllegalArgumentException {

       _setterDir = new File( setterDirectory ) ;
       if( ! _setterDir.isDirectory() )
          throw new
          IllegalArgumentException("Setter directory not found or not a directory: "+setterDirectory);  

       _getterDir = new File( getterDirectory ) ;
       if( ! _getterDir.isDirectory() )
          throw new
          IllegalArgumentException("getter directory not found or not a directory: "+getterDirectory);  

    }
    /* ------------------------------------------------------------ */
    public void init(ServletConfig config) throws ServletException
    /* ------------------------------------------------------------ */
    {
    	super.init(config);
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

        ServletOutputStream out = response.getOutputStream();

        String ourURI = request.getRequestURI() ;
        String [] commands = ourURI.split("/");

        if( commands.length < 2 ){
           out.flush();
           return ;
        }

        String command = commands[1] ;

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
               }catch( Exception jsonEx ){
                  out.println("{ \"result\" : \"-1\" , \"errorMessage\" : \""+jsonEx.getMessage()+"\" }");
               }
           }else{
               out.println("{ \"result\" : \"Illegal Method : "+request.getMethod()+"\"  }" );
           }
        }else{

           response.setContentType("text/html");

           out.println("<html>");
           out.println("<h1>Hello Trude</h1>");
           out.println("<h2>"+request.getContextPath()+"</h2>");
           out.println("<h2>"+request.getRequestURL()+"</h2>");
           out.println("<h2>"+request.getRequestURI()+"</h2>");
           out.println("<h2>"+request.getServletPath()+"</h2>");
           out.println("<h2> $"+command+"$</h2>");
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

          if( obj.containsKey( "list" ) ){
              processPostList( obj.getJSONArray("list")  );
          }else if( obj.containsKey( "elist" )){
              processPostEList( obj.getJSONArray("elist") );
          }else{            
            throw new
            IllegalArgumentException("No list entry found in message");
          }
          obj.put( "result" , 0 ) ;
       }catch( Exception eeii){
           obj.put( "result" , -1 ) ;
           obj.put( "errorMessage" , eeii.getMessage() ) ;
       }
       System.out.println("Sending Message : "+obj.toString());
       out.println(obj.toString());

    }
    private void processPostEList( JSONArray list ) 
           throws IOException,JSONException {

       Map<String,String> flagMap = getAllFlags();

       for( int i = 0 ; i < list.size() ; i++ ){

           JSONObject x = (JSONObject)list.get(i) ;

           try{

              String method = x.getString("method");
              String name   = x.getString("name");
              if( method.equals("get") ){
                  
                 Pattern   pattern = Pattern.compile( name );
                 JSONArray array = new JSONArray();

                 for( Map.Entry<String,String> entry : flagMap.entrySet() ){
                    if( pattern.matcher( entry.getKey() ).matches() ){
                         JSONObject e = new JSONObject();
                         e.put( "name" , entry.getKey() ) ;
                         e.put( "value" , entry.getValue() ) ;
                         array.add( e );
                    }
                 }
 
                 x.put( "list" , array );
            //     System.out.println("Getting : "+name+" -> "+value ) ;
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
    }
    private void processPostList( JSONArray list ) throws IOException,JSONException {

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

    }
    private static int SETTER = 1 ; 
    private static int GETTER = 2 ; 
    private File nameToPath( String name , int mode ){

       String [] names = name.split("/") ;

       if( names.length == 1 )
          return new File( mode == SETTER  ? _setterDir : _getterDir , name ) ;
       
       String path = "" ;
       int i = 1 ;
       for( ; i < ( names.length - 1 ) ; i++ )path += ( names[i] + "/" ) ;
       path +=  names[i] ;

       if( names[0].equals("config" ) ){
          return new File( _setterDir , path ) ;
       }else if( names[0].equals("flags") ){
          return new File( _getterDir , path ) ;
       }
       throw new
       IllegalArgumentException("Internel Error : neither setter nor getter");

    } 
    private void setAttribute( String name , String value ){

       File f = nameToPath( name , SETTER ) ;
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
    private Map<String,String> getAllFlags(){
       Map<String,String> map = new HashMap<String,String>() ;   
       File [] flagFiles = _getterDir.listFiles() ;
       for( int i = 0 ; i < flagFiles.length ; i++ ){

          File f = flagFiles[i] ;
          
          try{
             String key = f.getName() ;
             map.put( key ,  _getAttribute(f) ) ;
          }catch( IOException ee ){
             System.err.println("IOError for ("+f+") : "+ee.getMessage());
             continue ;
          }
       }
       return  map ;
    }
    private String _getAttribute( File f ) throws IOException {

       BufferedReader br = new BufferedReader(new FileReader(f)) ;

       try{

          String in = br.readLine() ;
          if( in == null )
            throw new
            IllegalArgumentException("Internal problem reading : "+f.getName() );

          return in.trim() ;

       }finally{
          try{  br.close() ; }catch(Exception ce){}
       }

    }
    private String getAttribute( String name )  {

       File f = nameToPath( name , GETTER ) ;

       if( ( ! f.exists() ) || ( ! f.isFile() ) )
          throw new
          IllegalArgumentException("Argument not found : "+name );
       try{

          return _getAttribute( f ) ;

       }catch(IOException ee ){
          System.err.println("IOError : "+ee.getMessage());
          throw new
          IllegalArgumentException("Internal problem reading : "+name );
       }

    }
    
 
    
}
