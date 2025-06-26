package org.rosi.nodes ;
import java.util.Random ;

public class RosiTimerDevice extends RosiDevice {

     
    private String  _functionName = null ;
    private long    _delay        = 0 ;
    private boolean _repeat       = false;

    public RosiTimerDevice( String deviceName , long delay ){

        super( "timer" , deviceName ) ;

        _delay = delay ;

    }
    public String getValueType(){ return "T" ; }

    public void setRepeat( boolean repeat ){
       _repeat = repeat ;
    }
    public boolean isRepeat(){ return _repeat ; }
    public String getFunctionName(){ return _functionName ; }
    public void setFunctionName( String functionName ){
       _functionName = functionName ;
    }
    public String formatString( String gap ){

	StringBuffer sb = new StringBuffer() ;

	sb.append( super.formatString(gap) ) ;
	sb.append( gap ).append("  D : ").append(_delay).append(" seconds\n");
	sb.append( gap ).append("  M : ").append(_repeat?"repeat":"once").append("\n");
	sb.append( gap ).append("  F : ").append(_functionName).append("\n");

	return sb.toString() ;
   }
   public String toString(){
       return super.toString() +" [d="+_delay+";m="+(_repeat?"repeat":"once")+";f="+_functionName+";";
   }
}
