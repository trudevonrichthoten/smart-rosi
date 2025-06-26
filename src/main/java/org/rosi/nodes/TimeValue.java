package org.rosi.nodes ;


public class TimeValue extends RosiValue {

    private int _minutes = 0 ;
    private int _hours   = 0 ;
    
    public TimeValue( String str ){
        String [] x = str.split(":") ;
	_hours   = Integer.parseInt(x[0]) ;
	_minutes = Integer.parseInt(x[1]);
    }
    public long minutesOfDay(){
        return 60*_hours+_minutes ;
    }
    public String toString(){
       return  _hours+":"+_minutes ;
    }
 
}
