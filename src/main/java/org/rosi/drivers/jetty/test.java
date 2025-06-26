package org.rosi.drivers.jetty ;

import com.mongodb.* ;
import com.mongodb.client.* ;
import com.mongodb.client.result.* ;
import org.bson.*;
import org.bson.types.*;

/*
 *  Project :
        create time
        title
        description
      [parent project]
      [ ? array of subprojects]
      
    Meeting
        title
        description
        creation date/time
      length
      URL Reference

    Job
        title
        description
        creation date/time
      Deadline
      Next Deadline Alarm
      Expected time to finish (minutes/hours)
      Next Work Slot Scheduled & Length 
 *
 *
 *
 *
 *
 */
public class test {

   public static void main( String [] args ) throws Exception {


      MongoClient mongoClient = new MongoClient();

      MongoDatabase database = mongoClient.getDatabase("trude");

      MongoCollection<Document> collection = database.getCollection("test");

      String command = args[0] ;
      if( command.equals("insert") ){
      Document doc = Document.parse( args[1] ) ; 
            collection.insertOne(doc); 
            System.out.println(" ObjectID : "+doc.getObjectId("_id"));
      }else if( command.equals("update") ){
            Document up = Document.parse( args[2] ) ; 
            System.out.println(" Document : "+up.toString());
            System.out.println(" Document : "+up.get("sub").toString());
            UpdateResult res = collection.replaceOne(new Document( "_id" , new ObjectId( args[1] ) ) , up); 
            System.out.println(" Result : "+res);
      }
/*

      Document doc = new Document( "name", "MongoDB").
                           append( "type", "database").
                           append( "count", 1). 
                           append( "created", new BsonDateTime( System.currentTimeMillis()) ) ;


      System.out.println(" Document : "+doc);

      collection.insertOne( doc ) ;

      System.out.println(" Document : "+doc);

      for( Object o : doc.values() ){
         System.out.println(" Object : "+o);
         System.out.println(" Class  : "+o.getClass().getName());
      }

      System.out.println(" ObjectID : "+doc.getObjectId("_id"));


      Document myDoc = collection.find( 
              new BasicDBObject("_id",  doc.getObjectId("_id") )
      ).first();

      System.out.println(" Result Document : "+myDoc);

      FindIterable<Document> it = collection.find();
      MongoCursor<Document> d = it.iterator() ;
      for(  ; d.hasNext() ; ){
         for( String s : x.keySet() ){
            if( s.equals("root") ){
            }
            System.out.println(s);
         }

        Document x = d.next() ; 
        Document y  = (Document)x.get("root");
        String s = y.getString("title");
        System.out.println(" OBJEXXT : "+y.getClass().getName());
        System.out.println(" String :  "+s);
      }
   
      for( Document d : it.iterator() ){
        System.out.println(" "+d.toString());
      } 
*/
  }
}
/*
> db.test.find( { created : ISODate("2016-02-28T08:39") } )
{ "_id" : ObjectId("56d2b4ca5be2a7e8584c4ff6"), "created" : ISODate("2016-02-28T08:39:00Z") }
*/
