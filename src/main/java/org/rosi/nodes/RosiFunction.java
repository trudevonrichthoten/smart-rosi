package org.rosi.nodes ;

import java.util.List ;
import java.util.ArrayList ;
import org.rosi.util.RosiRuntimeException ;

public class RosiFunction extends RosiValue {

    private String _functionName = null ;
    private RosiSection      _section = null ;
    private RosiVectorValue  _args    = null ;
    private boolean _systemFunction   = false ;

    public RosiFunction( String functionName ){
       _functionName = functionName ;
    }
    public RosiFunction( String functionName  , boolean isSystemFunction ){
       _functionName   = functionName ;
       _systemFunction = isSystemFunction ;
    }
    public void setSection( RosiSection section ){
      _section = section ;
    }
    public void setArgumentList( RosiVectorValue args ){
      _args = args ;
    }
    public RosiSection getSection(){ return _section ; }
    public RosiVectorValue getArguments(){ return _args ; }
    public String formatString( String gap ){

  	StringBuffer sb = new StringBuffer() ;

	sb.append( gap ).append( "Function" ).append("\n") ;
        sb.append( gap ).append( "  N : ").append(_functionName).append("\n"); 
        for( RosiValue node : _args.list() ){
	   sb.append( node.formatString( gap + "  A : " )  ) ;
        }
        for( RosiValue node : _section.statements() ){
	   sb.append( node.formatString( gap + "  S : " )  ) ;
        }
        

	return sb.toString() ;
   }
   public String getName(){ return _functionName ; }
   public String getFunctionName(){ return _functionName ; }
   public String toString(){
      return this.formatString("");
   }
   public String getValueAsString(){ 
      return "FUNCTION" ;
   }
}   
