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
import javax.servlet.http.HttpSession ;

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
@SuppressWarnings("unchecked")
public class Document2Servlet extends HttpServlet
{
    private MongoClient               _mongoClient = null ;
    private MongoCollection<Document> _collection  = null ;
    private MongoDatabase             _database    = null ;
    private SimpleDateFormat          _dateFormat      = new SimpleDateFormat("yyyy-MM-dd.HH:mm") ;
    private SimpleDateFormat          _dateOnlyFormat  = new SimpleDateFormat("yyyy-MM-dd") ;
    
    private MongoAuthenticationHandler _authHandler = null ;
    private String _servletPath = null ;

/* --------------------------------------------------------------------------- */
    public Document2Servlet( String database )
/* --------------------------------------------------------------------------- */
       throws IllegalArgumentException {
        System.out.println("This is "+database);

      _mongoClient = new MongoClient();

      _database    = _mongoClient.getDatabase( database );

      _collection  = _database.getCollection("documents");

      _authHandler = new MongoAuthenticationHandler("admin");

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
        //   /id/<id>/<version>
        //
        if( ( commands.length < 3 ) || ( ! commands[1].equals("id") ) ){
           response.sendError(HttpServletResponse.SC_NOT_FOUND);
           return ;
        }
        
        Document account = _authHandler.getAuthenticatedRecord( request ) ;
        if( account == null ){
             response.sendError(HttpServletResponse.SC_FORBIDDEN,"Authentication failed.");
             return ;
        }
        if(  ! _authHandler.isInGroup( account , "documents" )  ){
             response.sendError(
                   HttpServletResponse.SC_FORBIDDEN,
                   "Authorisation failed. (Not in group 'documents')");
             return ;
        }

        String username = account.getString("login");      
        String ID       = commands[2] ;
        String version  = commands.length > 3 ? commands[3] : "1.0" ;
        
        float floatVersion = (float)0.0 ;
        
        try{
            floatVersion = Float.parseFloat(version) ;
        }catch(Exception ee ){
           response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,"Invalid Version Number");
           return ;
        }
            
        long count = _collection.count(new Document( "ID" , ID ) ) ;
        
        if( count == 0 ){
            
            Document record = 
                new Document( "ID" , ID ).
                      append( "version" , version ).
                      append( "owner"   , username  ) ;      
                 
            _collection.insertOne( record ) ;
            
        }else{
            
           FindIterable<Document> it = _collection.find(new Document( "ID" , ID ) ) ;
            
           MongoCursor<Document>  d = it.iterator() ;
           
           Document masterDocument = null ;
           
           float maxVersion = (float)0.0 ;
           
           for( int i = 0  ; d.hasNext() ; i++ ){
           
               Document e = d.next() ;
               if( e.getObjectId("root") == null ){
                  masterDocument = e ;
               }
               String versionString = e.getString("version");
               if( versionString != null ){
                  try{
                     float fx = Float.parseFloat( versionString ) ;
                     maxVersion = Math.max( maxVersion , fx ) ;
                  }catch(Exception ee){
                    response.sendError(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Illegal Version Number stored in DB: "+versionString);
                    return ;
                  }
               }
           }
           if( masterDocument == null ){
              response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
              return ;
           }
           if( maxVersion >= floatVersion ){
             response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Version lower or equals current max version: "+maxVersion);
             return ;
           }
           
           Document record = 
                new Document( "ID" , ID ).
                      append( "version" , version ) ;
                      
           if( _collection.find( record ).first() != null ){
             response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Record already exists : "+ID+"["+version+"]");
             return ;
           }
                      
           record = 
                new Document( "ID" , ID ).
                      append( "version" , version ).
                      append( "owner"   , username  ) ;
                      
           record.put( "root" , masterDocument.get("_id") ) ;
                 
           _collection.insertOne( record ) ;
        }
    }
/* --------------------------------------------------------------------------- */
    public void doDelete(HttpServletRequest request, HttpServletResponse response) 
/* --------------------------------------------------------------------------- */
        throws ServletException, IOException
    {
        //   /id/<id>/<version>

        String ourURI = request.getPathInfo() ;
        
        String [] commands = ourURI.split("/");
        
        if( ( commands.length < 4 ) || ( ! commands[1].equals("id") ) ){
           response.sendError(HttpServletResponse.SC_NOT_FOUND);
           return ;
        }

        Document account = _authHandler.getAuthenticatedRecord( request ) ;
        if( account == null ){
             response.sendError(HttpServletResponse.SC_FORBIDDEN,"Authentication failed.");
             return ;
        }
        if(  ! _authHandler.isInGroup( account , "documents" )  ){
             response.sendError(
                   HttpServletResponse.SC_FORBIDDEN,
                   "Authorisation failed. (Not in group 'documents')");
             return ;
        }

        String username = account.getString("login");      
        String ID       = commands[2].toUpperCase() ;
        String version  = commands[3] ;
        
        Document toberemoved = 
             new Document("ID" , ID ).append( "version" , version ) ;
             
        Document record = _collection.find( toberemoved ).first() ;       
        if( record == null ){ 
              response.sendError(HttpServletResponse.SC_NOT_FOUND);
              return ;
        }
        boolean isAdmin = _authHandler.isInGroup( account , "admin" ) ;
        if( ! isAdmin ){
            
           String owner = record.getString("owner") ;
           
           if( owner == null ){
              response.sendError(HttpServletResponse.SC_FORBIDDEN,
              "Document doesn't have owner, you need to be 'admin' to remove'" );
              return ;
           }
           
           if( ! owner.equals(username) ){
              response.sendError(HttpServletResponse.SC_FORBIDDEN,
              "Not authorized : Not owner." );
              return ;
           }
           
        }
        
        if( record.getObjectId("root") == null ){
            
            long count = _collection.count(new Document( "ID" , ID ) ) ;
            if( count > 1 ){
                
              response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                 "Is master record" );
              return ;
              
            }else{
               Document result = 
        
               _collection.findOneAndDelete(  
                     new Document("ID" , ID ).append( "version" , version )
                                    ) ;
        
               if( result == null ){
                 response.sendError(HttpServletResponse.SC_NOT_FOUND);
                 return ;
               }
                
            }
           
        }else{
        
            Document result = 
        
            _collection.findOneAndDelete(  
                  new Document("ID" , ID ).append( "version" , version )
                                    ) ;
        
           if( result == null ){
              response.sendError(HttpServletResponse.SC_NOT_FOUND);
              return ;
           }

        }
 
    }
    private void setServletName( HttpServletRequest request ){
        if( _servletPath != null )return ;
        
        String servletPath = request.getServletPath() ;
        if( servletPath != null ){
           String [] c = servletPath.split("/") ;
           if( c.length >= 3 ){
              _servletPath = c[2] ;
           }
        }
    }
/* -------------------------------------------------------------------------- */
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
/* -------------------------------------------------------------------------- */
        throws ServletException, IOException
    {

        setServletName(request) ;
                
        String uri         = request.getPathInfo() ;
        
        if( uri == null ){
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return ;
        }
        
        String [] commands = uri.split("/");
        //
        //   /id/<id>/<version>
        //
        if( commands.length < 2 ){
           response.sendError(HttpServletResponse.SC_NOT_FOUND);
           return ;
        }

        String ID      = ( commands.length > 2 ) ? commands[2].toUpperCase() : null ;
        String version = ( commands.length > 3 ) ? commands[3] : null ;
        
        if( commands[1].equals("id") ){
            
             doGetMetadata( request , response , ID , version ) ;
             
        }else if( commands[1].equals("data")  ){
            
            if( ID == null ){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,"No ID");
                return ;
             }
             doGetRedirect( request , response , ID , version ) ;
             
        }else{
           response.sendError(HttpServletResponse.SC_NOT_FOUND, "Illegal function : "+commands[1] );
        }     
        
        return ;
        //    
        
    }

/* -------------------------------------------------------------------------- */
    public void doGetRedirect(
            HttpServletRequest request, 
            HttpServletResponse response,
            String ID ,
            String version  ) 
/* -------------------------------------------------------------------------- */
        throws ServletException, IOException
    {
        FindIterable<Document> it = null ;
        
        if( version == null ){
            
           Document sortDocument = 
                   new Document( "ID"      , 1 ).
                         append( "version" , -1 ) ;
                         
           it = _collection.find(  new Document( "ID" , ID  ) )  ;
           it = it.sort( sortDocument ) ;
           
        }else{
           it = _collection.find( 
                      new Document( "ID"      , ID ).
                            append( "version" , version)
                           )  ;
            
        }
        Document result = it.first() ;
        
        if( result == null ){
           response.sendError(HttpServletResponse.SC_NOT_FOUND);
           return ;
        }
        
        String redirectURL = result.getString("url");
        
        if( ( redirectURL == null ) || ( redirectURL.equals("") ) ){
           response.sendError(HttpServletResponse.SC_NOT_FOUND,"No URL specified.");
           return ;
        }
        response.sendRedirect( redirectURL ) ;
        
        return ;
    }
    private class DocSelector {
        List<String> _areaList   = null ;
        Set<String> _doctypeHash = null ;
        boolean _allAreas = false , _allDoctypes = false ;
        
        private DocSelector( String selectBase64 ) throws IllegalArgumentException {
            String selectJson = new String( Base64.getDecoder().decode(selectBase64) ) ;
            Document selectDoc = Document.parse(selectJson);
            System.out.println("Selector : "+selectDoc.toJson());
            _areaList    = (List<String>)selectDoc.get("area");
            _allAreas = ( _areaList.size() == 1 ) && _areaList.get(0).equals("*") ;
            
            List<String> l = (List<String>)selectDoc.get("doctype") ;
            _allDoctypes = ( l.size() == 1 ) && l.get(0).equals("*");
            if( ! _allDoctypes )_doctypeHash = new HashSet<String>( l ) ;
            System.out.println("AllAreas : "+_allAreas+"; allDocs : "+_allDoctypes+
                 "; AreaList : "+_areaList+"; Doc Hash : "+_doctypeHash ) ;
        }
        private boolean isSelected( Document doc ){
            
            String type    = doc.getString("type");
            String section = doc.getString("section") ;
            
            if( type == null )return _allDoctypes ;
            
            if( section == null )return _allAreas ;
            
            type    = type.toUpperCase() ;
            section = section.toUpperCase() ;
            
            boolean isOk = false ;
            if( ! _allAreas ){
               for( String x : _areaList ){
                   if( section.indexOf(x) > -1 ){
                      isOk = true ;
                      break ;
                   }
               }
               if( ! isOk )return false ;
            }
            
            if( _allDoctypes )return true ;
            
            return _doctypeHash.contains( type ) ;
        }
    }
/* -------------------------------------------------------------------------- */
    public void doGetMetadata(
            HttpServletRequest request, 
            HttpServletResponse response,
            String ID ,
            String version   ) 
/* -------------------------------------------------------------------------- */
        throws ServletException, IOException
    {
       String outputType  = request.getParameter("type") ;
       String sorting     = request.getParameter("sort");

       if( outputType == null )outputType = "json" ;

       FindIterable<Document> it = null ;
       boolean dontSort = false ;
       
       if( ( ID == null ) ||   ID.equals("*") ){
           it = _collection.find() ;
       }else if( ( version == null ) ||  version.equals("*") ){
           it = _collection.find(new Document( "ID" , ID ) ) ;
           dontSort = true ;
       }else{
           dontSort = true ;
           it = _collection.find( 
                      new Document( "ID"      , ID ).
                            append( "version" , version)
                           )  ;
       }
       if( it.first() == null ){
          response.sendError(HttpServletResponse.SC_NOT_FOUND);
          return ;
       }
       
       Document sortDocument = new Document( "ID" , 1 ).append( "version" , 1 ) ;
       if( ! (  dontSort || outputType.equals("json" ) ) ) {
           if( ( sorting == null )  || ( sorting.equals("id") ) ){
           }else if( sorting.equals("date")  ){
               sortDocument = new Document( "date" , 1 ) ;
           }else if( sorting.equals("xdate") ){
               sortDocument = new Document( "date" , -1 ) ;
           }else if( sorting.equals("title") ){
               sortDocument = new Document( "title" , 1 ) ;
           }else if( sorting.equals("type")  ){
               sortDocument = new Document( "type" , 1 ) ;
           }
       }
           
       it = it.sort( sortDocument ) ;
       
       /*
        * prepare selection
        */
       String selectString = request.getParameter("select");
       DocSelector selector = selectString != null ? new DocSelector( selectString ) : null ;
       
       MongoCursor<Document>  d = it.iterator() ;
       
       List<Document> list = new ArrayList<Document>() ;
       
       ServletOutputStream out = response.getOutputStream();
       
       for( int i = 0  ; d.hasNext() ; i++ ){
       
           Document e = d.next() ;
           
           
           ObjectId id = e.getObjectId("root") ;
           if( id != null ){
              Document rootDoc = _collection.find( 
                      new Document( "_id" , id ) 
                      ).first() ;
              e = mergeDocuments( rootDoc , e ) ;
           }
           
           if( ! ( ( selector == null ) || selector.isSelected(e) ) )continue ;

           list.add(e) ;
           
       }

       //if( list.size() == 0 ){
       //   response.sendError(HttpServletResponse.SC_NOT_FOUND);
       //   return ;
       // }

       if( outputType.equals("json" ) ){
           
           response.setContentType("application/json");
           Document result = new Document( "list" , list ) ;
           out.println( result.toJson() ) ;
           
       }else if( outputType.equals("html") ){
           
           response.setContentType("text/html");
           printDocumentListHtml( outputType , list , out ) ;
           
       }else if( outputType.startsWith("wiki") ){  
           
           response.setContentType("text/html");
           printDocumentListWiki( outputType , list , out ) ;
           
       }else{
          response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unkown rendering type : "+outputType);
          return ;
       }
       out.flush() ;
    }
/* ---------------------------------------------------------------------------- */
    private FindIterable<Document> getSelectedDocuments( String selection ){
/* ---------------------------------------------------------------------------- */

       FindIterable<Document> it = _collection.find() ;
       Document sortDocument = new Document( "ID" , 1 ).append( "version" , 1 ) ;
       it = it.sort( sortDocument ) ;
       
       MongoCursor<Document>  d = it.iterator() ;
       List<Document> list = new ArrayList<Document>() ;
              
       for( int i = 0  ; d.hasNext() ; i++ ){
       
           Document e = d.next() ;
           ObjectId id = e.getObjectId("root") ;
           if( id != null ){
              Document rootDoc = _collection.find( 
                      new Document( "_id" , id ) 
                      ).first() ;
              e = mergeDocuments( rootDoc , e ) ;
           }
           list.add(e) ;
           
       }
       

       return null ;
    }
/* ---------------------------------------------------------------------------- */
    private Document mergeDocuments( Document root , Document child ){
/* ---------------------------------------------------------------------------- */
      String [] el = { "url" , "title" , "type" , "contact" , "section" } ;
      
      for( int i = 0 ; i < el.length ; i++ ){
         String url = child.getString( el[i] ) ;
         if( ( url == null ) || ( url.equals("") ) )child.put(el[i],root.getString( el[i] )) ;
      }
      
      return child ;
    }
/* ---------------------------------------------------------------------------- */
    private void printDocumentListWiki( 
                    String mode , 
                    List<Document>  list ,
                    ServletOutputStream out )
/* ---------------------------------------------------------------------------- */
           throws IOException {
               
        
        out.println("<html><head><title>WP4 Documents</title></head><body><pre>");
        out.println("h1. INDIGO-DataCloud WP4 related documents\n");

        for( Document d : list ){

           try{
              out.print( renderDocumentWiki( mode , d ) ) ;
           }catch(Exception ee ){
               
           }
        
        }
        out.println("</pre></body></html>");
    }
    
    /*---------------------------------------------*/
    private boolean isSelected( Document record ,  
                         String [] sections , 
                         Set<String> docTypes   ){
    /*---------------------------------------------*/
    
        String sec = record.getString("section") ;
        
        if( ( sec == null ) || ( sec.trim().equals("") ) )return false ;
        
        Set<String> section_set = new HashSet<String>(Arrays.asList(sec.toUpperCase().split(","))); 
        
        String dt = record.getString("type");
        if( dt  == null )return false ;
        
        dt = dt.toUpperCase() ;
        
        for( int i = 0 ; i < sections.length ; i++){
            
            if( section_set.contains( sections[i] ) ){
                
                 if( docTypes.contains( dt ) )return true ;
            }
               
        }
        return false ;
    }
/* ---------------------------------------------------------------------------- */
    private void printDocumentListHtml( 
                  String mode , 
                  List<Document>  list ,
                  ServletOutputStream out    )
/* ---------------------------------------------------------------------------- */
           throws IOException {
               
        
        out.println( renderDocumentHtmlStart( mode ) );
        
        for( Document d : list ){

           try{
              out.print( renderDocumentHtml( mode , d ) ) ;
           }catch(Exception ee ){
               
           }
        
        }
        out.println( renderDocumentHtmlEnd( mode ) );
    }
/* ---------------------------------------------------------------------------- */
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
/* ---------------------------------------------------------------------------- */
        throws ServletException, IOException
    {

        String uri         = request.getPathInfo() ;
        String [] commands = uri.split("/");
        //
        //   /id/<id>/<version>
        //
        if( ( commands.length < 4 ) || ( ! commands[1].equals("id") ) ){
           response.sendError(HttpServletResponse.SC_NOT_FOUND);
           return ;
        }     

        Document account = _authHandler.getAuthenticatedRecord( request ) ;
        if( account == null ){
             response.sendError(HttpServletResponse.SC_FORBIDDEN,"Authentication failed.");
             return ;
        }
        if(  ! _authHandler.isInGroup( account , "documents" )  ){
             response.sendError(
                   HttpServletResponse.SC_FORBIDDEN,
                   "Authorisation failed. (Not in group 'documents')");
             return ;
        }

        String username = account.getString("login");      

        BufferedReader reader = request.getReader() ;
        String in = null ;
        StringBuffer sb = new StringBuffer();
        
        while( ( in = reader.readLine() ) != null )sb.append(in);
        
        String jsonInput = sb.toString() ;
                
        try{

            String ID      = commands[2].toUpperCase() ;
            String version = commands[3] ;

            Document result = 
                _collection.find( 
                    new Document("ID" , ID ).append( "version" , version) 
                ).first() ;
            if( result == null ){
               response.sendError(
                   HttpServletResponse.SC_NOT_FOUND,"Not found : "+ID+"["+version+"]");
               return ;
            }

            String owner = result.getString("owner") ;
            owner = owner == null ? "%%%%%" : owner ;
            if( ! ( owner.equals(username) || _authHandler.isInGroup( account , "admin" ) ) ){
               response.sendError(
                   HttpServletResponse.SC_FORBIDDEN,"Not authorized : Not owner.");
               return ;
            }
            
            Document in_document = Document.parse(jsonInput);

            in_document = normalizeDocument( in_document ) ;
            
            result = 
                _collection.findOneAndUpdate( 
                    new Document("ID" , ID ).append( "version" , version) ,
                    new Document("$set", in_document )
                ) ;
        
            if( result == null ){
               response.sendError(HttpServletResponse.SC_NOT_FOUND);
               return ;
            }

        }catch( Exception jsonEx ){
            jsonEx.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,jsonEx.getMessage());        
        }

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
    private Document normalizeDocument( Document record )
/* ---------------------------------------------------------------*/
      throws ParseException {
          
        Object dateObject = record.get("date");
        if( dateObject instanceof String ){
            String dateString = (String)dateObject ;
            if( ( dateString != null ) && ! dateString.equals("") ){
                Date date = _dateFormat.parse(dateString+"."+"12:00") ;
                record.put( "date" , date ) ;
            }
        }
        
        return record ;
    }
/* ---------------------------------------------------------------*/
    private Map<String,String> convertDocument( Document e )
/* ---------------------------------------------------------------*/
      throws ParseException {
        
        Map<String,String> map = new HashMap<String,String>() ;
 
        String ID       = e.getString("ID")  ;
        String version  = e.getString("version");
        
        map.put( "ID" , e.getString("ID") ) ;
        map.put( "version" , e.getString("version") ) ;
        
        String type = e.getString("type") ;
        if( type == null ){
           type = "Other" ;
        }else if( type.equals("DL") ){
           type = "Deliverable" ;
        }else if( type.equals("MS") ){
           type = "Milestone" ;
        }else if( type.equals("TD") ){
           type = "Technical Document" ;
        }else if( type.equals("PR") ){
           type = "Presentation" ;
        }else if( type.equals("AB") ){
           type = "Abstract" ;
        }else if( type.equals("PP") ){
           type = "Paper" ;
        }else if( type.equals("PO") ){
           type = "Poster" ;
        }
        map.put( "type" , type ) ;
        
        Date   date     = e.getDate("date") ;
        map.put( "date" , date == null ? "no date" : _dateFormat.format(date) );

        map.put( "url" , e.getString("url") ) ;
        
        String title = e.getString("title") ;
        map.put( "title" , title == null ? "no title" : title ) ;
        
        String contact  = e.getString("contact") ;
        map.put( "contact" , title == null ? "no contact" : contact ) ;
        
        map.put( "root" , e.get("root") == null ? null : "LINK" ) ;
        
        return map ;
    }
/* ---------------------------------------------------------------*/
    private String renderDocumentWiki( String mode , Document e )
/* ---------------------------------------------------------------*/
      throws ParseException {
          
      Map<String,String> map = convertDocument( e ) ;
        
      String url = map.get("url");
      String ID  = map.get("ID");
      
      String extendedID = ( url == null )     || 
                           url.equals("")     || 
                           url.equals("N.A.") ?
                           ID : ( "\"" + ID + "\":" + url ) ;
             
      StringBuffer sb = new StringBuffer() ;
          
      String link = map.get("root");
      
      if( mode.equals("wiki1") ){
          if( link == null )sb.append("|\\5. *").append(map.get("title")).append("*|\n");
          sb.append("|").append(extendedID);
          sb.append("|").append(map.get("version")) ;
          sb.append("|").append(map.get("type"));
          sb.append("|").append(map.get("contact"));
          sb.append("|").append(map.get("date").substring(0,10));
          sb.append("|\n");
       }else{
          if( link == null ){
             sb.append("\nh2. ").append(map.get("title")).append("\n\n");
             sb.append("|_. *ID* |_. *Version* |_. *Type* |_. *Contact* |_. *Date*|\n");
          }
          sb.append("|").append(extendedID);
          sb.append("|").append(map.get("version")) ;
          sb.append("|").append(map.get("type"));
          sb.append("|").append(map.get("contact"));
          sb.append("|").append(map.get("date"));
          sb.append("|\n");
       }
       
       return sb.toString();
    }
/* ---------------------------------------------------------------*/
    private String renderDocumentHtmlStart( String mode ){
/* ---------------------------------------------------------------*/
      StringBuffer sb = new StringBuffer() ;
      sb.append("<html>\n") ;
      sb.append("<head>\n") ;
      sb.append("<title>Listing</title>\n") ;
      if( _servletPath != null )
           sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/static/").
              append(_servletPath).append("/css/documents2.css\">\n");
      sb.append("</head>\n") ;
      sb.append("<body class=\"html-listing\">\n") ;
      sb.append("<h1 class=\"html-listing\">INDIGO Document Database</h1>");
      sb.append("<h2 class=\"html-listing\">Workpackage 4</h2>");
      sb.append("<table class=\"html-listing\">\n") ;
      /*
          sb.append("<th class=\"html-listing\">\n") ;
          sb.append("</th>\n") ;
      */
      sb.append("<tbody class=\"html-listing\">\n") ;
      return sb.toString();
}
/* ---------------------------------------------------------------*/
    private String renderDocumentHtmlEnd( String mode ){
/* ---------------------------------------------------------------*/
      StringBuffer sb = new StringBuffer() ;
      sb.append("</table>\n") ;
      sb.append("<hr><address>(c) Trude von Richthofen [").
         append( ( new Date()).toString() ).
         append("]</address>");
      sb.append("</body>\n") ;
      sb.append("</html>\n") ;
      return sb.toString();
}
/* ---------------------------------------------------------------*/
    private String renderDocumentHtml( String mode , Document e )
/* ---------------------------------------------------------------*/
      throws ParseException {
          
      Map<String,String> map = convertDocument( e ) ;
        
      String url = map.get("url");
      String ID  = map.get("ID");
      
      String extendedID = ( url == null )     || 
                           url.equals("")     || 
                           url.equals("N.A.") ?
                           ID : 
                           ( "<a class=\"html-listing\" href=\""+url+"\">" + ID + "</a>") ;
      
      String tdPre = "<td class=\"html-listing\">" ;
      String trPre = "<tr class=\"html-listing\">" ;
      
      StringBuffer sb = new StringBuffer() ;
      
      String link = map.get("root");
      
      sb.append(trPre);
      if( link == null ){
          sb.append("<th class=\"html-listing\" colspan=5>").
             append(map.get("title")).
             append("</th>\n").         
             append("</tr>\n").
             append(trPre);
      }
      sb.append(tdPre).append(extendedID).append("</td>");
      sb.append(tdPre).append(map.get("version")).append("</td>") ;
      sb.append(tdPre).append(map.get("type")).append("</td>");
      sb.append(tdPre).append(map.get("contact")).append("</td>");
      sb.append(tdPre).append(map.get("date")).append("</td>");
      
      sb.append("</tr>\n");
       
      return sb.toString();
   }
  
}
