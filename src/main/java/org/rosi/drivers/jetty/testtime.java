import java.util.* ;
import java.text.* ;

import java.time.* ;
import java.time.format.* ;

import com.mongodb.* ;
import com.mongodb.client.* ;
import com.mongodb.client.result.* ;

import org.bson.*;
import org.bson.types.*;
import org.bson.types.*;



public class testtime {
   public static void main( String [] args ) throws Exception {
 
     if( args.length < 2 ){
       System.err.println("Usage : ... <ZoneID> <ParsePattern> <tiemstring> <NewZoneID>");
       System.exit(1);
    }
/*
  SimpleDateFormat dfin   = new SimpleDateFormat( args[0] ) ;
  SimpleDateFormat dfout  = new SimpleDateFormat( args[2] ) ;
  Date dd =  dfin.parse( args[1] ) ;
  
  System.out.println( "Date : "+dd  ); 
  System.out.println( "Date : "+dfout.format( dd ) ); 
*/

   //  ZoneId inZoneId  = ZoneId.of( args[0] ) ;
     ZoneId outZoneId = ZoneId.of( args[2] ) ;

     DateTimeFormatter formatter    = DateTimeFormatter.ofPattern( args[0] );
  //   DateTimeFormatter formatterIn  = formatter.withZone( inZoneId ) ;
   //  DateTimeFormatter formatterOut = formatter.withZone( outZoneId ) ;

    LocalDateTime x = LocalDateTime.parse( args[1] , /* formatter */ DateTimeFormatter.ISO_LOCAL_DATE_TIME ) ; 

 //    Instant utc = x.toInstant() ;
 //    System.out.println("Instant : "+utc);


//       ZonedDateTime y = utc.atZone( outZoneId ) ; 
//       ZonedDateTime y = x.withZoneSameInstant( outZoneId ) ; 
         ZonedDateTime y = x.atZone(outZoneId ) ; 

     System.out.println(" Result : "+y);
     System.out.println(" Result : "+y);

     Instant t = y.toInstant();
     Date d = new Date( t.toEpochMilli() ) ;
     System.out.println("Date : "+d );

/*
    MongoClient               _mongoClient = null ;
    MongoCollection<Document> _collection  = null ;
    MongoDatabase             _database    = null ;

      _mongoClient = new MongoClient();

      _database    = _mongoClient.getDatabase( "timetest" );

      _collection  = _database.getCollection("times");


      Document doc =
       new Document("date-string",  args[2] )
                    .append("date",  d );

     _collection.insertOne(doc);
*/

   }
}
