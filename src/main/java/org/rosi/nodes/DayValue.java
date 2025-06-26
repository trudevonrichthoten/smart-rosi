package org.rosi.nodes ;

import org.rosi.util.* ;

import java.util.List ;
import java.util.ArrayList ;

public class DayValue extends RosiValue {


   private static int DV_SUNDAY    = (1<<0) ;
   private static int DV_MONDAY    = (1<<1) ;
   private static int DV_TUESDAY   = (1<<2) ;
   private static int DV_WEDNESDAY = (1<<3) ;
   private static int DV_THURSDAY  = (1<<4) ;
   private static int DV_FRIDAY    = (1<<5) ;
   private static int DV_SATURDAY  = (1<<6) ;

   static private String [] _dayNames = {
       "Sunday" ,
       "Monday" , 
       "Tuesday" ,
       "Wednesday" ,
       "Thursday" ,
       "Friday" ,
       "Saturday" 
   } ;

   public static int checkDayValue( String str ){
      int day = 0 ;
      for( int i = 0 ; i < _dayNames.length ; i++ ){
          if( _dayNames[i].equals(str) ){
	     day = ( 1 << i ) ;
	  }
      }    
      return day ;
   }

   private int _day   = 0 ;

   public DayValue( String str ){
      this( checkDayValue(str) ) ;
   }
   public DayValue( int day ){
      _day = day ;
   }
   public DayValue(){
       
   }
   public void addDay( DayValue day ){
      _day = _day | day._day ;
   }
   public void addDay( int day ){
      _day = _day | day ;
   }
   public boolean contains( RosiCalendar calendar ){
   
       int dayOfWeek = calendar.getDayOfWeek() ;
 
//        System.out.println("DEBUG : days : "+Integer.toHexString(_day)+" "+dayOfWeek);
      
       int dayMask = 1 << ( dayOfWeek - 1 ) ;
       
       return ( dayMask & _day ) != 0 ;
   }
   public int days(){ return _day ; }
   public String toString(){
   
      StringBuffer sb = new StringBuffer() ;
      
      for( int i = 0 ; i < _dayNames.length ; i++ ){
      
          int n = 1 << i ;
	  
	  if( ( _day & n ) > 0 )sb.append(_dayNames[i] ).append(",");
      }
      
      return sb.toString() ;
   }

}
