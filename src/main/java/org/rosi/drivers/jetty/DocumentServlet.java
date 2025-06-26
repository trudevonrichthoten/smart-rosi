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
import java.text.* ;

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
import com.mongodb.client.model.UpdateOptions ;


/* ------------------------------------------------------------ */
/** Dump Servlet Request.
 * 
 */
public class DocumentServlet extends HttpServlet
{
    private MongoClient               _mongoClient = null ;
    private MongoCollection<Document> _collection  = null ;
    private MongoDatabase             _database    = null ;
    private SimpleDateFormat          _dateFormat   = new SimpleDateFormat("yyyy-MM-dd") ;

    /* ------------------------------------------------------------ */
    public DocumentServlet( String database )
       throws IllegalArgumentException {
        System.out.println("This is "+database);

      _mongoClient = new MongoClient();

      _database    = _mongoClient.getDatabase( database );

      _collection  = _database.getCollection("documents");
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
    public void doPut(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException
    {
        String ourURI = request.getRequestURI() ;
        
        String [] commands = ourURI.split("/");
        
        if( ( commands.length < 4 ) || ( ! commands[2].equals("id") ) ){
           response.sendError(HttpServletResponse.SC_NOT_FOUND);
           return ;
        }
        
        String ID = commands[3] ;
        
        String [] parts = ID.split("\\$");

        if( parts.length > 2 ){
           response.sendError(HttpServletResponse.SC_NOT_FOUND);
           return ;          
        }else if( parts.length == 2 ){
            
           ID = parts[0] ;
           String version = parts[1] ;

           Document primary = _collection.find( new Document( "ID" , ID ) ).first() ;
           
           if( primary == null ){
              response.sendError(HttpServletResponse.SC_NOT_FOUND);
              return ;          
           }    
        
           String newID = ID+"$"+version ;
        
           Document secondary = 
              new Document( "ID" , newID ).
                    append( "version" , version ).
                    append( "root" , primary.get("_id") ) ;
                 
           _collection.insertOne( secondary ) ;
 
        }else{
            
            _collection.insertOne( new Document( "ID" , ID ) ) ;
            
        }
        

    }
    /* ------------------------------------------------------------ */
    public void doDelete(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException
    {
        //ServletOutputStream out = response.getOutputStream();

        String ourURI = request.getRequestURI() ;
        String [] commands = ourURI.split("/");
        System.out.println(" Command ; "+ourURI +" [1] "+ commands[1] ) ;
        if( ( commands.length < 4 ) || ( ! commands[2].equals("id") ) ){
           //out.flush();
           response.sendError(HttpServletResponse.SC_NOT_FOUND);
           return ;
        }
        String ID = commands[3] ;
        
        Document result = 
        
        _collection.findOneAndDelete(  new Document("ID" , ID ) ) ;
        
        if( result == null ){
           //out.flush();
           response.sendError(HttpServletResponse.SC_NOT_FOUND);
           return ;
        }

 
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
           }else if( request.getMethod().equals("GET") ){
               response.setContentType("text/html");
               String uri = request.getRequestURI() ;
               
               if( uri.startsWith("/json/wiki") ){
                  out.println("<html><pre>");
                  processGetWiki(  uri, out ) ;
                  out.println("</pre><hr>");
                  out.println("<address>Copyright Trude von Richthofen (c)</address>");
                  out.println("</html>");
               }else if( uri.startsWith("/json/html") ){
               }else{
                  response.sendError(HttpServletResponse.SC_NOT_FOUND);
                  //out.println("{ \"result\" : \"URI not found : "+request.getRequestURI()+"\"  }" );
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
    
/* ---------------------------------------------------------------*/
    private void createRecord( ServletOutputStream out, 
                               Document record ) 
        throws IOException,JSONException {
/* ---------------------------------------------------------------*/
       
        _collection.insertOne( record ) ;
        out.println( "{ \"record\" :  ");
        out.println( record.toJson() ) ;
        out.println( " , \"result\" : 0 }");
    }
/* ---------------------------------------------------------------*/
    private void versionRecord( ServletOutputStream out, 
                               Document record ) 
        throws IOException,JSONException {
/* ---------------------------------------------------------------*/
       
        String ID = record.getString("ID");
        
        Document primary = _collection.find( new Document( "ID" , ID ) ).first() ;
        
        String version = record.getString("version");
        if( version == null )
            throw new
            IllegalArgumentException( "No version specified");
            
        String newID = ID+"$"+version ;
        
        Document secondary = 
           new Document( "ID" , newID ).
                 append( "version" , version ).
                 append( "root" , primary.get("_id") ) ;
                 
        _collection.insertOne( secondary ) ;
        
        out.println( "{ \"record\" :  ");
        out.println( secondary.toJson() ) ;
        out.println( " , \"result\" : 0 }");
    }
/* ---------------------------------------------------------------*/
    private void deleteRecord( ServletOutputStream out, Document record ) 
       throws IOException,
              JSONException,
              ParseException      {
/* ---------------------------------------------------------------*/

        String ID = record.getString("ID") ;

        Document result = 
        _collection.findOneAndDelete( 
            new Document("ID" , ID )
        ) ;
        
        if( result == null )
          throw new
          IllegalArgumentException("Entry not found : "+ID);


    }
/* ---------------------------------------------------------------*/
    private void setRecord( ServletOutputStream out, Document record ) 
       throws IOException,
              JSONException,
              ParseException      {
/* ---------------------------------------------------------------*/
       
        String dateString = record.getString("date");
        if( dateString != null ){
            Date d = _dateFormat.parse(dateString) ;
            record.put( "date" , d ) ;
            System.out.println("Date converted : ["+dateString+"] : "+d);
        }
        
        String ID = record.getString("ID") ;
        
        Document result = 
        _collection.findOneAndUpdate( 
            new Document("ID" , ID ),
            new Document("$set", record )
        ) ;
        
        if( result == null )
          throw new
          IllegalArgumentException("Entry not found : "+ID);
          
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
    private void processGetWiki( String uri ,ServletOutputStream out ) 
       throws IOException,JSONException {

       FindIterable<Document> it = _collection.find();
       it.sort(new BasicDBObject("date", -1));
       MongoCursor<Document>  d = it.iterator() ;
       // Calendar c = Calendar.getInstance();
       
       out.println( "h1. INDIGO-DataCloud WP4 Documents\n");

       for( int i = 0  ; d.hasNext() ; i++ ){
           
               Document e = d.next() ;
               
               String ID       = e.getString("ID")  ;
               String url      = e.getString("url") ;
               String type     = e.getString("type") ;
               String title    = e.getString("title") ;
               String version  = e.getString("version");
               String contact  = e.getString("contact") ;
               Date   date     = e.getDate("date") ;
               
               if( type == null ){
                   type = "Unkown" ;
               }else if( type.equals("DL") ){
                   type = "Deliverable" ;
               }else if( type.equals("MS") ){
                   type = "Milestone" ;
               }else if( type.equals("TD") ){
                   type = "Technical Document" ;
               }
                              
               String extendedID = ( url == null ) || url.equals("") || url.equals("N.A.") ?
                                   ID : ( "\"" + ID + "\":" + url ) ;
               
               String extendedDate = date == null ? 
                                   "no date" : _dateFormat.format(date);
               
              StringBuffer sb = new StringBuffer() ;
              if( uri.endsWith("wiki1") ){
                  sb.append("|\\5. *").append(title).append("*|\n");
                  sb.append("|").append(extendedID);
                  sb.append("|").append(version) ;
                  sb.append("|").append(type);
                  sb.append("|").append(contact);
                  sb.append("|").append(extendedDate);
                  sb.append("|\n");
               }else{
                  sb.append("h2. ").append(title).append("\n\n");
                  sb.append("|_. *ID* |_. *Version* |_. *Type* |_. *Contact* |_. *Date*|\n");
                  sb.append("|").append(extendedID);
                  sb.append("|").append(version) ;
                  sb.append("|").append(type);
                  sb.append("|").append(contact);
                  sb.append("|").append(extendedDate);
                  sb.append("|\n\n");
               }
                
               out.print(sb.toString());
               //if( i > 0 ) out.println(",");
               //out.println(e.toJson()) ;
       }
       out.println("");

        
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
          String commandString = in_document.getString("command")  ;
          if( commandString == null ){ 
              
                throw new
                IllegalArgumentException("No command found");
              
          }else if( commandString.equals("create") ){
              
                 Document record = (Document)in_document.get("record") ;
                 
                 if( record != null ){
                   createRecord( out , record ) ;
                 }else{
                   throw new
                   IllegalArgumentException("Record not found in command : "+commandString);
                 }
          }else if( commandString.equals("set") ){
                 Document record = (Document)in_document.get("record") ;
                 if( record != null ){
                   setRecord(  out ,record ) ;
                 }else{
                   throw new
                   IllegalArgumentException("Record not found in command : "+commandString);
                 }
          }else if( commandString.equals("version") ){
                 Document record = (Document)in_document.get("record") ;
                 if( record != null ){
                   versionRecord(  out ,record ) ;
                 }else{
                   throw new
                   IllegalArgumentException("Record not found in command : "+commandString);
                 }
          }else if( commandString.equals("delete") ){
                 Document record = (Document)in_document.get("record") ;
                 if( record != null ){
                   deleteRecord(  out ,record ) ;
                 }else{
                   throw new
                   IllegalArgumentException("Record not found in command : "+commandString);
                 }
          }else if( commandString.equals("list") ){
             FindIterable<Document> it = _collection.find(
               /*  new BasicDBObject("root.parent.id",  id ) */
             );

             MongoCursor<Document>  d = it.iterator() ;

             out.println( "{ \"list\" : [ ");
             for( int i = 0  ; d.hasNext() ; i++ ){
               Document e = d.next() ;
               if( i > 0 ) out.println(",");
               out.println(e.toJson()) ;
             }
             out.println("] , \"result\" : 0 }");

          }else{
                throw new
                IllegalArgumentException("No such command : "+commandString);
          }
 /*            
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

       */

       }catch( MongoWriteException mwe){
           out.println( "{ \"result\" : -1 , \"msg\" : \""+mwe.getMessage()+"\" }");
           System.err.println("Exception in loop : "+mwe);
           WriteError error = mwe.getError() ;
           System.err.println("Error in loop : "+error);
           int code = error.getCode() ;
           if( error.getCode() == 11000 ){
              out.println( "{ \"result\" : "+code+" , \"msg\" : \"Entry already exists\" }");
           }else{
              out.println( "{ \"result\" : -1 , \"msg\" : \""+mwe.getMessage()+"\" }");
           }
       }catch( Exception eeii){
           out.println( "{ \"result\" : -1 , \"msg\" : \""+eeii.getMessage()+"\" }");
           System.err.println("Error in loop : "+eeii);
           eeii.printStackTrace();
       }
       //System.out.println("Sending Message : "+obj.toString());

    }
    
}
