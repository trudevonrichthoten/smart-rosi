package org.rosi.util ;

import java.util.* ;
import org.rosi.nodes.RosiSection ;
import org.rosi.nodes.RosiFunction ;

public class AtTool {

   private Map<Long,RosiSection> _map = new TreeMap<Long,RosiSection>();
   public AtTool(){

   } 
   public void addSection( RosiSection section , long diff ){
      _map.put( Long.valueOf(System.currentTimeMillis()+diff) , section ) ;
     Map.Entry<Long,RosiSection> first = null ;
     for( Map.Entry<Long,RosiSection> e : _map.entrySet() ){
         first = e ; break ;
     }
     System.out.println(" First : "+first.getKey() ) ;  
   }
   public String toString(){
     StringBuffer sb= new StringBuffer() ;
     for( Map.Entry<Long,RosiSection> e : _map.entrySet() ){
        sb.append("Time     : ").append(e.getKey()).append("\n") ;
        sb.append(e.getValue().formatString("")).append("\n");
     }
     return sb.toString();
   }
   public static void main( String [] args ) throws Exception {
      AtTool at = new AtTool() ;
/*
      at.addSection( new RosiFunction("hallo") , 1000L ) ;
      at.addSection( new RosiFunction("hallo") , 4000L ) ;
      at.addSection( new RosiFunction("hallo") , 50L ) ;
*/
      System.out.println(at.toString());
   } 
}
