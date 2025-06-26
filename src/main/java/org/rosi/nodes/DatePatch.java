package org.rosi.nodes ;

import org.rosi.util.*;

import java.util.Calendar ;
import java.util.List ;
import java.util.ArrayList ;

public class DatePatch extends RosiValue {

   private int _day   = 0 ;
   private int _month = 0 ;
   private int _year  = 0 ;
   
   private DatePatch _from = null ;
   private DatePatch _to   = null ;
   
   private List<DatePatch> _list = null ;
   
   private DayValue _days = null ;
    
   public DatePatch(){
       _list = new ArrayList<DatePatch>() ;
   }
   public DatePatch( DatePatch from , DatePatch to ){
      this._from = from ;
      this._to   = to ;
   }
   public boolean isDateValue(){
      return ( _from==null ) && ( _list == null ) ;
   }
   public DatePatch from(){ return _from ; }
   public DatePatch to(){ return _to ; }
   public DatePatch( String str ){
   
       String [] vec = str.split("/") ;
       
       if( vec.length > 0 ){
          _day = Integer.parseInt( vec[0] ) ;
       }
       if( vec.length > 1 ){
          _month = Integer.parseInt( vec[1] ) ;
       }
       if( vec.length > 2 ){
          _year = Integer.parseInt( vec[2] ) ;
       }
   }
   public void addDays( DayValue days ){
      _days = days ;
   }
   public void addPatch( DatePatch patch ){
       _list.add(patch);
   }
    public String toString(){
    
	StringBuffer sb = new StringBuffer() ;
        
	if( _list != null ){
	
	   for( DatePatch patch : _list ){
	      sb.append( patch.toString() ).append( "," ) ;
	   }
	   if( _days != null )
	   sb.append( _days.toString() ) ;
	   
	}else if( isDateValue() ){
		   
	   sb.append( _day ).append( "/" ) ;
	   sb.append( _month ) ;
	   if( _year > 0 )sb.append( "/" ).append( _year );
	   
	   
	}else if( _from == _to ){
	
	   sb.append( "[" ).append( this._from.toString() ).
	      append( "]" ) ;
	
	}else{
	 
	   sb.append( "[" ).append( this._from.toString() ).
	      append( "-" ).append( this._to.toString() ).
	      append( "]" ) ;
	      	   
	}

	return sb.toString();

    }
    public long getMillisByDate( int year , int month , int day , boolean isTheEnd ){
    
       Calendar c = Calendar.getInstance() ;

       c.set( Calendar.YEAR         , year  ) ;
       c.set( Calendar.MONTH        , month-1 ) ;
       c.set( Calendar.DAY_OF_MONTH , day   ) ;
       
       if( isTheEnd ){
	  c.set( Calendar.HOUR_OF_DAY , 23 ) ;
	  c.set( Calendar.MINUTE      , 59 ) ;
	  c.set( Calendar.SECOND      , 59 ) ;
	  c.set( Calendar.MILLISECOND , 999 ) ;
       }else{
	  c.set( Calendar.HOUR_OF_DAY , 0 ) ;
	  c.set( Calendar.MINUTE      , 0 ) ;
	  c.set( Calendar.SECOND      , 0 ) ;
	  c.set( Calendar.MILLISECOND , 0 ) ;
       }
       long result = c.getTimeInMillis() ;
       //System.out.println("DEBUG : (DatePatch) "+year+"/"+month+"/"+day+"/"+isTheEnd+":"+result);
       return result ;
    }
    public boolean contains( RosiCalendar calendar ){
    
       if( ( _days != null ) && ( _days.contains( calendar ) ) )return true ;
       
       if( _list != null ){
       
           for( DatePatch patch : _list ){
	       if( patch.contains( calendar ) )return true ;
	   }
	   return false ;
       
       }
 
       if( _from != null ){
       
          long from = getMillisByDate( _from._year , _from._month , _from._day , false ) ;
	  
	  DatePatch toPatch = _to == null ? _from : _to ;
	  
	  long to = getMillisByDate( toPatch._year , toPatch._month , toPatch._day , true ) ;

          long checkTime = calendar.getTimeInMillis() ;
	  
	  //System.out.println("DEBUG : (DatePatch) : "+from+"/"+checkTime+"/"+to);
	  
	  return ( from <= checkTime ) && ( checkTime <= to ) ;
       
       }  
       
       throw new
       IllegalArgumentException("BUG DatePatch doesn't cnotain valid date patch");  
    }


}
