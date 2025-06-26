package org.rosi.nodes ;

import java.util.List ;
import java.util.ArrayList ;

public class RosiSection extends RosiVectorValue {
/*
    public void add( RosiAssigment command ){
       super.add( command ) ;
    }
    public void add( RosiConditional command ){
       super.add( command ) ;
    }
    public void add( Rosi2Conditional command ){
       super.add( command ) ;
    }
*/
    public List<RosiValue> statements(){ return super.list() ; }

    public String formatString( String gap ){

  	StringBuffer sb = new StringBuffer() ;

	sb.append( gap ).append( "Section" ).append("\n") ;
        for( RosiValue node : statements() ){

	   sb.append( node.formatString( gap + "   " )  ) ;

        }

	return sb.toString() ;
   }
   public String toString(){
      return this.formatString("");
   }
}   
