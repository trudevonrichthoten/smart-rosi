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



public class checktimezone {
   public static void main( String [] args ) throws Exception {
 
     if( args.length < 1 ){
       System.err.println("Usage : ... <ZoneID>");
       System.exit(1);
    }

     ZoneId inZoneId  = ZoneId.of( args[0] ) ;
  
     System.out.println("Zone : "+inZoneId);

   }
}
