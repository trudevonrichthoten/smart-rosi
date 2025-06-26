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

import org.rosi.util.MemoryKeyValueManager ;
import org.rosi.util.RosiCommand ;
import org.rosi.util.RosiSetterCommand ;
import org.rosi.execution.RosiModule ;
import org.rosi.execution.ModuleContext ;

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
public class MemoryServlet extends HttpServlet
{
    private MemoryKeyValueManager _memory = null ;
    private RosiModule            _module = null ;
    private boolean               _allowSetter       = true ;
    private boolean               _forwardIncoming   = true ;
    private boolean               _addMessageLocally = true ;

    /* ------------------------------------------------------------ */
    public MemoryServlet( RosiModule module , MemoryKeyValueManager keyValueManager )
       throws IllegalArgumentException {

           _module = module ;
           _memory = keyValueManager ;

           ModuleContext context = _module.getContext();

           String value = context.get("memory.allowSetter");
           if( ( value != null ) && ( value.equals("true" ) ) )_allowSetter = true ;
           value = context.get("memory.forwardIncoming");
           if( ( value != null ) && ( value.equals("true" ) ) )_forwardIncoming = true ;
           value = context.get("memory.addMessageLocally");
           if( ( value != null ) && ( value.equals("true" ) ) )_addMessageLocally = true ;
           _module.log("memory.allowSetter: "+_allowSetter);
           _module.log("memory.forwardIncoming: "+_forwardIncoming);
           _module.log("memory.addMessageLocally: "+_addMessageLocally);
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


        String servletPath = request.getServletPath() ;
        String ourURI      = request.getRequestURI() ;

        _module.debug( "ServletPath:"+servletPath+";URI:"+ourURI) ;

        String uri = servletPath.length() == ourURI.length() ?
                         "" :
                         ourURI.substring( servletPath.length() );

        _module.debug("Effective URI : "+uri);

        String [] commands = uri.split("/");
 
        List<String> list = new ArrayList<String>() ; 
        for( int i = 0 ; i < commands.length ; i++ ){
           _module.debug( "Command["+i+"] : >"+commands[i]+"<" ) ; 
           if( commands[i].length() > 0 )list.add(commands[i]);
        }
        if( list.size() < 1 ){
           response.setStatus(HttpServletResponse.SC_NOT_FOUND);
           response.getOutputStream().flush();
           return ;
        }
        ServletOutputStream out = response.getOutputStream();
        String command = list.get(0) ;

        _module.debug("Executing Command : "+command);

        if( command.equals( "json" ) ){
           if( request.getMethod().equals("POST") ){
               response.setContentType("application/json");
               BufferedReader reader = request.getReader() ;
               String in = null ;
               StringBuffer sb = new StringBuffer();
               while( ( in = reader.readLine() ) != null )sb.append(in);
               _module.debug("POST : "+sb.toString());
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

           out.println("<html><body>");
           out.println("<table border=1>");
           out.println("<tr><td>ContextPath</td><td>"+request.getContextPath()+"</td></tr>");
           out.println("<tr><td>RequestURL</td><td>"+request.getRequestURL()+"</td></tr>");
           out.println("<tr><td>RequestURI</td><td>"+request.getRequestURI()+"</td></tr>");
           out.println("<tr><td>ServletPath</td><td>"+request.getServletPath()+"</td></tr>");
           out.println("<tr><td>Command</td><td>"+command+"</td></tr>");
           out.println("</table>");
           out.println("<address>Copyright Trude von Richthofen (c)</address>");
           out.println("</body></html>");
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

       _module.debug("Message Received : "+in); 
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
       _module.debug("Sending Message : "+obj.toString());
       out.println(obj.toString());

    }
    private void processPostEList( JSONArray list ) 
           throws IOException,JSONException {

       Map<String,String> flagMap = _memory.getSimpleMap();

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

              }else{
                 throw new
                 IllegalArgumentException("Unsupported method : "+method);
              }
              x.put( "result" , 0 ) ;

           }catch( JSONException notfound ){
              _module.errorLog("JSONException : "+notfound.getMessage());
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
                     _module.log("Setting : "+name+" -> "+value ) ;
                  }else if( method.equals("get") ){
                     String value = getAttribute( name ) ;
                     x.put( "value" , value );
                     _module.log("Getting : "+name+" -> "+value ) ;
                  }else{
                     throw new
                     IllegalArgumentException("Unsupported method : "+method);
                  }
                  x.put( "result" , 0 ) ;
               }catch( JSONException notfound ){
                  _module.errorLog("JSONException : "+notfound.getMessage());
                  x.put( "result" , -1 ) ;
                  x.put( "errorMessage" , notfound.getMessage() ) ;
               }
           }

    }
    private void setAttribute( String name , String value ){

       if( ! _allowSetter )return ;

       RosiCommand command = new RosiSetterCommand(name, value);

       if( _forwardIncoming ){
           try{
               _module.put( command ) ;
           }catch(Exception ee ){
               _module.errorLog( "Problem sending message: "+command);
           }
       }

       if( _addMessageLocally )_memory.add( name , value ) ;


    }
    private String getAttribute( String name ) throws JSONException  {
       String value =  _memory.get(name) ;
       if( value == null )
          throw new
          JSONException("Value not found!");
       return value ;
    }
    
 
    
}
