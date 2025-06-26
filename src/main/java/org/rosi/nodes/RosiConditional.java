package org.rosi.nodes ;

import org.rosi.util.*;


public class RosiConditional extends RosiValue {

    private VariableValue _variable = null ;
    private RosiProgram   _program  = null ;

    public RosiConditional( VariableValue var , RosiProgram program ){
        _variable = var ;
        _program  = program ;
    }
    public String formatString( String gap ){

	StringBuffer sb = new StringBuffer() ;

	sb.append( gap ).append("Condition").append("\n") ;
 	sb.append( _variable.formatString( gap + "  C: " )  ) ;
 	sb.append(  _program.formatString( gap + "  P: " )  ) ;

	return sb.toString() ;
    }
    public VariableValue getCondition(){ return _variable ; }
    public RosiProgram getProgram(){ return _program ; }
}
