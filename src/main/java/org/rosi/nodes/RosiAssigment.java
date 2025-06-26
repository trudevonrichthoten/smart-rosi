package org.rosi.nodes ;

import org.rosi.util.*;

import java.io.* ;
import java.util.* ;


public class RosiAssigment extends RosiValue {

     private String    _name  = null ;
     private RosiValue _value = null ;
     private VariableValue _variableNameValue = null ;

     public RosiAssigment( String variableName , RosiValue value ){
      
         _name = variableName ;
	 _value = value ;
         throw new IllegalArgumentException("NOT SUPPORTED ANY MORE : RosiAssigment(String,RosiValue)");
     }

     public RosiAssigment( VariableValue variableNameValue , RosiValue value ){
         _variableNameValue = variableNameValue ;
	 _value = value ;
         _name  = variableNameValue.getValueAsString() ;
     }
     public String getVariableName(){ return _name  ; }
     public VariableValue getVariableNameValue(){ return _variableNameValue ; }
     public RosiValue getAssigment(){ return _value ; }
     public RosiValue getRightSide(){ return _value ; }

     public String formatString( String gap ){

	StringBuffer sb = new StringBuffer() ;

	sb.append( gap ).append("Assigment").append("\n") ;
        sb.append( _variableNameValue.formatString( gap + "  V: " ) ) ;
 	sb.append( _value.formatString( gap + "  E: " )  ) ;

	return sb.toString() ;
    }
    public String toString(){
       return formatString("") ;
    }

}
