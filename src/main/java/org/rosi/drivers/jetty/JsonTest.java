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

import net.sf.json.*;
import net.sf.json.util.*;

public class JsonTest {

 public static void main( String [] args )  throws Exception 
    {

    String in = args[0] ;

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
                  }else if( method.equals("get") ){
                     if( name.equals("empty") ){
                     }else{
                       JSONArray array = new JSONArray() ;
                          JSONObject y = new JSONObject() ;
                          y.put("name" , "livingroom.heater" ) ;
                          y.put("value" , "34.0" ) ;
                       array.add( y );
                          y.put("name" , "livingroom.desired" ) ;
                          y.put("value" , "30.0" ) ;
                       array.add( y );
                       x.put( "list" , array ) ; 
                     }
                  }else{
                     throw new
                     IllegalArgumentException("Unsupported method : "+method);
                  }
               }catch( JSONException notfound ){
                  System.out.println("JSONException : "+notfound.getMessage());
               }
           }
           obj.put( "result" , 0 ) ;
       }catch( Exception eeii){
           obj.put( "result" , -1 ) ;
           obj.put( "errorMessage" , eeii.getMessage() ) ;
       }
       System.out.println("Sending Message : "+obj.toString());

   } 
}
