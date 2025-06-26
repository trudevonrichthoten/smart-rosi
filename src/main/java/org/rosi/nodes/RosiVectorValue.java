package org.rosi.nodes ;

import java.util.List ;
import java.util.ArrayList ;

public class RosiVectorValue extends RosiValue {

   private List<RosiValue> _list = new ArrayList<RosiValue>() ;
   
   private String _vectorName = null ;
   public RosiVectorValue(String name ){
      _vectorName = name ;
   }
   public RosiVectorValue(){ }

   public void add( RosiValue value ){ _list.add( value ) ; } 

   public List<RosiValue> list() { return _list ; }
   
   public int size(){ return _list.size() ; }

    public String formatString( String gap ){

        String [] className = getClass().getName().split("\\.") ;

        StringBuffer sb = new StringBuffer() ;

        sb.append( gap ) ;

        if( _vectorName != null )sb.append(_vectorName);
        else sb.append("Vector");

        sb.append("(").
           append( className.length == 0 ?
                      getClass().getName() : 
                      className[className.length-1] )
          .append(")\n") ;

        for( RosiValue node : _list ){

           sb.append( node.formatString( gap + "   " )  ) ;

        }

        return sb.toString() ;
   }
/*
   public String formatString( String gap ){

       StringBuffer sb = new StringBuffer() ;

       sb.append( gap ).append( "Vector (unkown)" ).append("\n") ;

       for( RosiValue node : _list ){

	  sb.append( node.formatString( gap + "  V: " )  ) ;

       }

       return sb.toString() ;
  }
*/

}
