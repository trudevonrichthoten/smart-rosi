 package org.rosi.nodes ;

 
 public class RosiReturnStatement extends RosiValue {
 
    private RosiValue _expression = null ;
    public RosiReturnStatement( RosiValue value ){
        _expression = value ;
    }
    public String getValueType(){ return "R" ; }
/*
    public String getValueAsString(){ return _expression.getValueAsString() ; }
    
    public boolean getValueAsBoolean(){ return _expression.getValueAsBoolean() ; }
    
    public float getValueAsFloat(){ return _expression.getValueAsFloat() ; }

    public int getValueAsNumber(){ return _expression.getValueAsNumber() ; }
*/
    public String formatString( String gap ){

        StringBuffer sb = new StringBuffer() ;

        sb.append( gap ).append("Return").append("\n") ;
        sb.append(_expression.formatString( gap + "  E: ") ).append("\n") ;

        return sb.toString() ;
    }
    public RosiValue getExpression(){ return _expression ; }
    /* 
    public int compareTo( RosiValue value ){
    
       int fvalue = 0 ;
       if( value instanceof NumberValue ){
       
           fvalue = ((NumberValue)value)._int ;
	   
       }else if( value instanceof FloatValue ){
       
           fvalue = (int) ((FloatValue)value).getValueAsFloat() ;
	   
       }else{
          return super.compareTo(value);
       }
 
       return fvalue == this._int ? 0 : ( fvalue > this._int ? -1 : 1 ) ;

    }
    */



 }
