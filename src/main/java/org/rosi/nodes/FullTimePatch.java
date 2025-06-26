package org.rosi.nodes ;

import org.rosi.util.RosiCalendar ;

public class FullTimePatch extends RosiValue implements TimeCheckable  {

   private TimePatch _time = null ;
   private DatePatch _date = null ;
   
   private boolean _cachedResult = false ;
   
   public FullTimePatch( TimePatch timePatch , DatePatch datePatch ){
   
      _time = timePatch ;
      _date = datePatch ;
      
   }
   public boolean isTriggered(){
      return _cachedResult ;
   }
   public boolean getValueAsBoolean(){ 
   
      return _cachedResult ; 
      
   }
   public String toString(){
       return "FullTimePatch : "+_time+" ON "+_date ;
   }

   public String formatString( String gap ){

       StringBuffer sb = new StringBuffer() ;

       sb.append( gap ).append( "Full Time Patch" ).append("\n") ;

       sb.append(gap).append("  T: ").append( _time.toString() ).append("\n");
       sb.append(gap).append("  D: ").append( _date.toString() ).append("\n");


       return sb.toString() ;
  }
  public boolean contains( RosiCalendar calendar ){
       
       return _cachedResult = _time.contains(calendar) && _date.contains(calendar)  ;
       
  }
   
}
