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
import com.mongodb.* ;
import com.mongodb.client.* ;
import com.mongodb.client.result.* ;
import org.bson.*;
import org.bson.types.*;
import org.bson.types.*;


/* ------------------------------------------------------------ */
/** Dump Servlet Request.
 * 
 */
public class MongoServlet extends HttpServlet
{
    private MongoClient               _mongoClient = null ;
    private MongoCollection<Document> _collection  = null ;
    private MongoDatabase             _database    = null ;

    /* ------------------------------------------------------------ */
    public MongoServlet( String database )
       throws IllegalArgumentException {
        System.out.println("This is "+database);

      _mongoClient = new MongoClient();

      _database    = _mongoClient.getDatabase( database );

      _collection  = _database.getCollection("project");
/*
      Document doc = new Document( "name", "MongoDB").
                           append( "type", "database").
                           append( "count", 1). 
                           append( "created", new BsonDateTime( System.currentTimeMillis()) ) ;

*/

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

        System.out.println(" MIME : "+request.getContentType());
        System.out.println(" URI : "+request.getRequestURI());
/*
        if( ! request.getContentType().equals("application/x-www-form-urlencoded") ){
            InputStream instream =  request.getInputStream();
            int n = 0 , sum = 0 ;
            byte [] buffer = new byte[1024];

            while( ( n = instream.read(buffer) ) > -1 ){
                 sum += n ;
            }
            System.out.println(" Received : "+sum ) ;
            return ;
        }
*/
/*
        try{
        Thread.sleep(1000);
        }catch(Exception eeee ){}
*/
        String command = commands[1] ;

        System.out.println(" Command : "+command);

        if( command.equals( "json" ) ){
           if( request.getMethod().equals("POST") ){

               response.setContentType("application/json");
               /*
                * Build json document.
                */
               BufferedReader reader = request.getReader() ;
               String in = null ;
               StringBuffer sb = new StringBuffer();
               while( ( in = reader.readLine() ) != null )sb.append(in);
               System.out.println("POST received : "+sb.toString());
               try{
                  processPostJson( sb.toString() , out ) ;
               }catch( JSONException jsonEx ){
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
    private void insertRecord( ServletOutputStream out, Document record ) throws IOException,JSONException {
        _collection.insertOne( record ) ;
        out.println( "{ \"record\" :  ");
        out.println( record.toJson() ) ;
        out.println( " , \"result\" : 0 }");
    }
    private void updateRecord( ServletOutputStream out , Document record ) throws IOException,JSONException {
        ObjectId id = record.getObjectId("_id") ;
        UpdateResult result =  _collection.replaceOne(
                 new Document( 
                     "_id" ,
                     id ) ,
                 record ) ;
        long r = result.getModifiedCount();
        if( r != 1 )
           throw new
           IllegalArgumentException("Couldn't be updated : "+record.getObjectId("_id"));
        out.println( "{ \"result\" : 0 }");
    }
    private void processPostJson( String in , ServletOutputStream out ) throws IOException,JSONException {

       System.out.println("Message Received : "+in); 

       JSONTokener tok = new JSONTokener(in) ;
       JSONObject  obj = (JSONObject)tok.nextValue();

       //  we only expect a single document.
       //
       // migration to the new Document interface (as we are using Mongo anyway).
       //
       Document in_document = Document.parse(in);
       try{
          String commandString = null ;
          if(  ( commandString = in_document.getString("command") ) != null ){ 
             if( commandString.equals("set") ){
                 Document record = (Document)in_document.get("record") ;
                 if( record != null ){
                   updateRecord( out , record ) ;
                 }else{
                   throw new
                   IllegalArgumentException("Record not found in command : "+commandString);
                 }
             }else if( commandString.equals("insert") ){
                 Document record = (Document)in_document.get("record") ;
                 if( record != null ){
                   insertRecord(  out ,record ) ;
                 }else{
                   throw new
                   IllegalArgumentException("Record not found in command : "+commandString);
                 }
             }else{
                throw new
                IllegalArgumentException("No such command : "+commandString);
             }
          }else if(  obj.has("type") ){ 

             String typeString = obj.getString("type") ;

             FindIterable<Document> it = _collection.find(
                 new BasicDBObject("root.type",  typeString )
             );
             MongoCursor<Document>  d = it.iterator() ;

             out.println( "{ \"list\" : [ ");
             for( int i = 0  ; d.hasNext() ; i++ ){
               Document e = d.next() ;
               if( i > 0 ) out.println(",");
               out.println(e.toJson()) ;
             }
             out.println("] , \"result\" : 0 }");

          }else if(  obj.has("parent") ){ 
             String parentIdString = obj.getString("parent");
             ObjectId id = new ObjectId( parentIdString ) ;

             FindIterable<Document> it = _collection.find(
                 new BasicDBObject("root.parent.id",  id )
             );

             MongoCursor<Document>  d = it.iterator() ;

             out.println( "{ \"list\" : [ ");
             for( int i = 0  ; d.hasNext() ; i++ ){
               Document e = d.next() ;
               if( i > 0 ) out.println(",");
               out.println(e.toJson()) ;
             }
             out.println("] , \"result\" : 0 }");

          }else if(  obj.has("name") ){ 

             String typeString = obj.getString("name");

             Document dc  = _collection.find( 
                 new BasicDBObject("root.name",  typeString )
             ).first() ;

             out.println( "{ \"record\" :  ");
             out.println( dc.toJson() ) ; 
             out.println( " , \"result\" : 0 }");
          }


       }catch( Exception eeii){
           out.println( "{ \"result\" : -1 , \"msg\" : \""+eeii.getMessage()+"\" }");
           System.err.println("Error in loop : "+eeii);
           eeii.printStackTrace();
       }
       //System.out.println("Sending Message : "+obj.toString());

    }
    
}
