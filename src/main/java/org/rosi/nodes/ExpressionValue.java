package org.rosi.nodes ;

import java.util.Map ;


public class ExpressionValue extends RosiValue /* implements Calculatable<RosiValue> */ {

     private RosiValue _left = null ;
     private RosiValue _right = null ;

     private String _op = null ;

     public ExpressionValue( String op , RosiValue left , RosiValue right ){
         _op    = op ;
	 _left  = left ;
	 _right = right ;
     }
     public ExpressionValue( String op , RosiValue left ){
         _op    = op ;
	 _left  = left ;
     }
     public String getValueType(){ return "E" ; }
     public RosiValue left(){ return _left ; }
     public RosiValue right(){ return _right ; }
     public String getOperation(){ return _op ;}
     public String toString(){

        if( _right == null ){
           return "("+_op+" "+_left+")" ;
        }else{
           return "("+_left+_op+_right+")" ;
        }
     }
     public String formatString(){
	 return formatString("");
     }

      public String formatString( String gap ){

	 StringBuffer sb = new StringBuffer() ;

	 sb.append( gap ).append(_op) ;

	 sb.append("  (").append(getValueType()).append(")\n") ;

	 if( _left != null  )sb.append(left().formatString( gap + "  L: " ) );
	 if( _right != null )sb.append(right().formatString( gap +"  R: " ) );	   

	 return sb.toString() ;
      }
      /*
      private RosiValue resolve( RosiValue value , Map<String,RosiValue> map ){


	  if( value instanceof Calculatable ) {

	      value = ((Calculatable<RosiValue>)value).calculate( map )  ;

	  }else if( value instanceof VariableValue ){

	      String variableName = ((VariableValue)value).getVariableName() ;

	      if( map == null )
		 throw new
		 IllegalArgumentException("Can't resolve symbol : "+variableName+" (no map)");

	      value = map.get( variableName ) ;

	      if( value == null )
		 throw new
		 IllegalArgumentException("Can't resolve symbol : "+variableName);
	  } 

	  return value ;
      }
      
      public RosiValue calculate(  Map<String,RosiValue> map ){

	  if( ( _left == null ) || ( _right == null ) || ( _op == null ) )
	      throw new
	      IllegalArgumentException("Incomplete structure (BUG)");

	  RosiValue leftValue  = resolve( _left , map ) ;
	  RosiValue rightValue = resolve( _right , map ) ;


	  if( _op.equals("|") || _op.equals("&") ){

	        boolean a = leftValue.getValueAsBoolean() ;
		boolean b = rightValue.getValueAsBoolean() ;

		return new BooleanValue( _op.equals("|") ? ( a | b )  : ( a & b ) ) ;		 	    
	  }else if( _op.equals("<") || _op.equals(">") ){

	        float a = leftValue.getValueAsFloat() ;
		float b = rightValue.getValueAsFloat() ;
		return new BooleanValue( _op.equals("<") ? ( a < b )  : ( a > b ) ) ;	

	  }else{
	      throw new
	      IllegalArgumentException("Operation not supportd : "+_op);
	  }
      }
      */
}
