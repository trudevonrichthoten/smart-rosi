package org.rosi.drivers.jetty ;

import java.io.*;
import java.util.* ;
import java.text.* ;
import static java.util.Arrays.asList;

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
@SuppressWarnings("unchecked")
public class MongoAuthenticationHandler
/* ------------------------------------------------------------ */
{
    private MongoClient                _mongoClient = null ;
    private MongoCollection<Document>  _accounts    = null ;
    
    private MongoAuthenticationHandler _authHandler = null ;

/* --------------------------------------------------------------------------- */
    public MongoAuthenticationHandler( String database )
/* --------------------------------------------------------------------------- */
       throws IllegalArgumentException {
           
        System.out.println("MongoAuthenticationHandler : "+database);

      _mongoClient = new MongoClient();
      
      _accounts    = _mongoClient.getDatabase( database ).getCollection("accounts");
      
      _authHandler = this ;

    }
/* ---------------------------------------------------------------------------- */
    public Document checkAuthentication( String username , String password )
/* ---------------------------------------------------------------------------- */
    {
        Document account = new Document("login" , username ) ;
             
        Document record = _accounts.find( account ).first() ;
        
        if( record == null )return null ;
        
        String pwd = record.getString("password");
        
        return ( pwd != null ) && pwd.equals(password) ? record : null ;
    }
/* -------------------------------------------------------------------------- */
    public String getAuthenticatedPrinciple(  HttpServletRequest request )
/* -------------------------------------------------------------------------- */
    
        throws ServletException, IOException
    {
        HttpSession session = request.getSession(false) ;
        if( session != null ){
            
            String principle = (String)session.getAttribute("username") ;
            
            return principle ;
            
        }
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
                
                if( checkAuthentication( values[0] , values[1] ) != null )return values[0] ;
                
            }
            
        }
        
        return null ;
        
    }
    
/* -------------------------------------------------------------------------- */
    public boolean requestNotPermitted( 
         HttpServletRequest request, 
         HttpServletResponse response ,
         String requiredRole             )
/* -------------------------------------------------------------------------- */
    
        throws ServletException, IOException
    {
        HttpSession session = request.getSession(false) ;
        if( session != null )return false ;
        
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
                
                if( checkAuthentication( values[0] , values[1] ) != null )return false ;
                
            }
            
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        
        return true ;
        
    }
/* ---------------------------------------------------------------------------- */
    public Document userAdd( String username )
/* ---------------------------------------------------------------------------- */
       throws EntryAlreadyExistsException {

       Document newAccount = new Document( "login" , username ) ;
             
       newAccount = _accounts.find( newAccount ).first() ;
       if( newAccount != null )
           throw new
           EntryAlreadyExistsException("Username already exists : "+username);

       newAccount = 
                   new Document().
                   append( "login"    , username ).
                   append( "password" , "" ).
                   append( "email"    , "" ).
                   append( "fullname" , username ).
                   append( "groups"   , asList( "public" ) );
                   
       _accounts.insertOne( newAccount ) ;     

       return newAccount ;
    }
/* ---------------------------------------------------------------------------- */
    public void userDelete( String username )
/* ---------------------------------------------------------------------------- */
       throws EntryNotFoundException {
           
       Document deleteAccount = new Document( "login" , username ) ;
             
       deleteAccount = _accounts.findOneAndDelete( deleteAccount ) ;
       
       if( deleteAccount == null )
           throw new
           EntryNotFoundException("No such user : "+username);
    }
/* ---------------------------------------------------------------------------- */
    public void userUpdate( String account , Document posted )
/* ---------------------------------------------------------------------------- */
       throws EntryNotFoundException, IllegalArgumentException {

        Document result = _accounts.find( new Document("login" , account) ).first() ;
        if( result == null )
                throw new
                EntryNotFoundException("Account not found: "+account);
          
        Document update = new Document() ;

        String [] keys = new String[]{ "email" , "fullname" , "password" } ;
        
        for( int i = 0 ; i < keys.length ; i++ ){
            String value = posted.getString(keys[i]);
            if( value != null )update.append( keys[i] , value ) ;
        }
        if( ! update.isEmpty() ){
            Document doc =
               _accounts.findOneAndUpdate(
                    new Document("login" , account) ,
                    new Document("$set"  , update )
            ) ;
            if( doc == null )
                throw new
                EntryNotFoundException("Account not found: "+account);
        }
        
        Document group  = posted.get( "group" , org.bson.Document.class) ;
        if( group == null )return ;

        String groupMethod = group.getString("method") ;
        if( groupMethod == null )
            throw new
            IllegalArgumentException("No method specified");
       
        List<String> groupList = (List<String>)group.get( "groups" , java.util.List.class ) ;

        if( groupList == null )
            throw new
            IllegalArgumentException("No group specified");

        update = new Document() ;

        if( groupMethod.equals("add") ){
            List<String> original = (List<String>)result.get("groups") ;
            for( String c : groupList ){
                if( ! original.contains(c) )original.add(c) ;
            }
            update.append( "groups" , original ) ;
        }else if( groupMethod.equals("remove") ){
            List<String> original = (List<String>)result.get("groups") ;
            for( String c : groupList ){
                original.remove(c) ;
            }
            update.append( "groups" , original ) ;
        }else if( groupMethod.equals("set") ){
            update.append( "groups" , groupList ) ;
        }else{
            throw new
            IllegalArgumentException("Bad method: "+groupMethod);
        }
        result =
           _accounts.findOneAndUpdate(
                new Document("login" , account) ,
                new Document("$set"  , update )
            ) ;
        if( result == null )
                throw new
                EntryNotFoundException("Account not found: "+account);
                
    }
/* ---------------------------------------------------------------------------- */
    public Document userGetRecord( String username ){
/* ---------------------------------------------------------------------------- */
    
         Document search = new Document( "login" , username ) ;
                  
         return _accounts.find( search ).first() ;

}
/* ---------------------------------------------------------------------------- */
    public MongoCursor<Document> userGetRecords( ){
/* ---------------------------------------------------------------------------- */
    
         Document search = new Document() ;
                  
         FindIterable<Document> it = _accounts.find( search ) ;
         return it.iterator() ;
         
}

/* ---------------------------------------------------------------------------- */
    public boolean isInGroup( Document record , String groupName ) 
/* ---------------------------------------------------------------------------- */
    {
        List<String> groupList = (List<String>)record.get( "groups" , java.util.List.class ) ;
        return ( groupList != null ) && 
               ( groupList.contains(groupName) || 
                 groupList.contains("admin" ) 
               );
    }
/* -------------------------------------------------------------------------- */
    public Document getAuthenticatedRecord( HttpServletRequest request )
/* -------------------------------------------------------------------------- */
    
        throws ServletException, IOException
    {
        HttpSession session = request.getSession(false) ;
        if( session != null ){
            return (Document)session.getAttribute("account") ;
        }
        
        String authorization = request.getHeader("Authorization") ;
        if( authorization != null ){
            
            if(  authorization.startsWith("Basic") ){
    
                String base64Credentials = authorization.substring("Basic".length()).trim();
                String credentials = 
                     new String(   Base64.getDecoder().decode(base64Credentials)
                                    /* , Charset.forName("UTF-8") */
                               );
                
                String[] values = credentials.split(":",2);
                //System.out.println("User : "+values[0]+" Password : "+values[1] );
                
                return _authHandler.checkAuthentication( values[0] , values[1] );
            }
            
        }
        
        return null ;
        
    }
    
}
