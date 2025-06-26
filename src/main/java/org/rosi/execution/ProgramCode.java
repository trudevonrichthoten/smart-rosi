package org.rosi.execution ;

import org.rosi.nodes.* ;
import org.rosi.util.*;
import org.rosi.compiler.* ;

import java.io.* ;
import java.util.* ;



public class ProgramCode {

      private Map<String,RosiFunction>
           _functions     = new HashMap<String,RosiFunction>();
      private List<FullTimePatch>
           _timePatches   = new ArrayList<FullTimePatch>() ;

      public void addFunction( RosiFunction function ){
         _functions.put( function.getName() , function );
      }
      public RosiFunction getFunction( String functionName , boolean expectResolving )
         throws RosiRuntimeException {
         RosiFunction func =  _functions.get( functionName ) ;
         if( ( func == null ) && expectResolving )
            throw new
            RosiRuntimeException("Function : <"+functionName+"> not found.");
         return func ;
      }
      public void addTimePatch( FullTimePatch patch ){
         _timePatches.add( patch );
      }
      public List<FullTimePatch> timePatchList(){
         return _timePatches ;
      }
      public void setTime( RosiCalendar calendar )  {

          for( FullTimePatch entry : this.timePatchList() ){
              /*
               * we don't need the result now, but it is cached
               * and used as soon as someone is calling the
               * TimePatch.
               */
              boolean result = entry.contains( calendar )  ;
          }

      }
      public void setTime()  throws Exception  {
  
          setTime( new RosiCalendar() ) ;

      }
      public String toString(){
       StringBuffer sb = new StringBuffer();
       for( Map.Entry e : _functions.entrySet() ){
            sb.append( "  ").append(e.getKey() ).append("\n");
       }  
       return sb.toString();
      }
}

