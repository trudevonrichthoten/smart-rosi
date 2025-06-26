package org.rosi.nodes ;

import org.rosi.util.*;

import java.util.Calendar ;
import java.util.List ;
import java.util.ArrayList ;

public class TimePatch extends RosiValue {

    private TimeValue _from = null ;
    private TimeValue _to   = null ;
    
    private List<TimePatch> _list = null ;
    
    public TimePatch(){
       _list = new ArrayList<TimePatch>() ;
    }
    public void addPatch( TimePatch patch ){
        _list.add( patch) ;
    }
    public TimePatch( TimeValue from , TimeValue to ){
        this._from = from ;
	this._to   = to ;
    }
    public TimeValue from(){ return this._from ; }
    public TimeValue to(){ return this._to ; }
    
    public String toString(){
        if( _list != null ){
	   StringBuffer sb = new StringBuffer() ;
	   for( TimePatch patch : _list ){
	      sb.append( patch.toString() ).append( "," ) ;
	   }
	   return sb.toString();
	}else{
           return "["+this._from+"-"+this._to+"]" ;
	}
    }
    public boolean contains( long ourTime ){
    
	   long from = _from.minutesOfDay() ;
	   long to   = _to.minutesOfDay() ;
	   
//	   System.out.println("DEBUG : (TimePatch) "+_from+"("+from+")"+"/"+ourTime+"/"+_to+"("+to+")") ;
	   return ( from <= ourTime ) && ( ourTime <= to ) ;
	       
    }
    public boolean contains( RosiCalendar calendar ){

	long ourTime = calendar.minutesOfDay() ;
    
        if( _from != null ){
		
	   return contains( ourTime ) ;
	
	}else{
	
	   for( TimePatch patch : _list ){
	   
	        if( patch.contains( ourTime ) )return true ;
	   }
	   
	   return false ;
	}
    }
}
