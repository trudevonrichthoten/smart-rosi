package org.rosi.nodes ;

import java.util.List ;
import java.util.ArrayList ;

public class RosiFunctionCall extends RosiValue {

    private String          _functionName = null ;
    private RosiVectorValue _arguments    = new RosiVectorValue() ;

    public RosiFunctionCall( String functionName ){
       _functionName = functionName ;
    }
    public RosiFunctionCall( RosiFunction func ){
       _functionName = func.getName() ;
    }
    public void setArguments( RosiVectorValue arguments ){
       _arguments = arguments ;
    }
    public String formatString( String gap ){

  	StringBuffer sb = new StringBuffer() ;

	sb.append( gap ).append( "FunctionCall" ).append("\n") ;
        sb.append( gap ).append( "  N : ").append(_functionName).append("\n"); 
        if( _arguments != null )
           sb.append( _arguments.formatString( gap + "  A : ") ) ;

	return sb.toString() ;
   }
   public RosiVectorValue getArguments(){ return _arguments ; }
   public String getName(){ return _functionName ; }
   public String getFunctionName(){ return _functionName ; }
   public String toString(){
      return this.formatString("");
   }
   public String getValueAsString(){ 
      return _functionName ;
   }
}   
