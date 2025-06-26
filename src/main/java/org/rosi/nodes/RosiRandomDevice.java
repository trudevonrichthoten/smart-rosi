package org.rosi.nodes ;
import java.util.Random ;

public class RosiRandomDevice extends RosiDevice {

     
    private Random _random = new Random( System.currentTimeMillis() ) ;
    private int    _mode        = -1 ;
    private long   _nextTrigger = 0L ;
    private int    _trueMin  = 0  , _trueMax = 120 ;
    private int    _falseMin = 0 , _falseMax = 120 ;

    public RosiRandomDevice( String deviceName ){

        super( "random" , deviceName ) ;

        getValueAsBoolean() ;

    }
    public String getValueType(){ return "B" ; }
    public String getRosiType(){ return "R" ; }

    public void setTrueInterval( int min , int max ){
       _falseMin = _trueMin = min ;
       _falseMax = _trueMax = max ;
    }
    public void setFalseInterval( int min , int max ){
       _falseMin = min ;
       _falseMax = max ;
    }
    private long getNextTrigger( int min , int max ){

       return ( min + _random.nextInt( max - min + 1 ) ) * 1000L +
               System.currentTimeMillis() ;
    }
    public String getValueAsString(){
       return ""+getValueAsBoolean() ;
    }
    public boolean getValueAsBoolean(){
       if( _mode < 0 ){

          _mode = 1 ; 

          _nextTrigger = getNextTrigger( _trueMin , _trueMax ); 
             
       }else if( _mode == 0 ){ /* False */
          
          if( remainingMillis() <= 0 ){
              _nextTrigger = getNextTrigger( _trueMin , _trueMax ); 
              _mode = 1 ;
          }

       }else if( _mode > 0 ) { /* True */

          if( remainingMillis() <= 0  ){
              _nextTrigger = getNextTrigger( _falseMin , _falseMax ); 
              _mode = 0 ;
          }
 
       }

       return _mode > 0 ;
    }
    private long remainingMillis(){

        long del = _nextTrigger -  System.currentTimeMillis() ;
	return del < 0L ? 0L : del ;
    }
    private long remainingSeconds(){ return remainingMillis() / 1000L ; }
    public String formatString( String gap ){

	StringBuffer sb = new StringBuffer() ;

	sb.append( super.formatString(gap) ) ;
        sb.append( gap ).append("  D: (").append(_trueMin).append(",").append(_trueMax).append("/").
                                          append(_falseMin).append(",").append(_falseMax).append(")\n") ;
	sb.append( gap ).append("  S: ").append(getValueAsBoolean()).append(" (").append(remainingSeconds()).append(")\n");

	return sb.toString() ;
   }
   public String toString(){
       return super.toString() +" [state "+getValueAsBoolean()+"] {"+remainingSeconds()+"}" ;
   }
}
