package org.rosi.nodes ;

import org.rosi.util.*;
import org.rosi.compiler.*;
import java.util.*;


public class Rosi2Conditional extends RosiValue {

    private List<AbstractMap.SimpleImmutableEntry<RosiValue,RosiSection>> _list = null ;
    public Rosi2Conditional(){
        _list = new ArrayList<AbstractMap.SimpleImmutableEntry<RosiValue,RosiSection>>() ;
    }
    public void addConditionalSection( RosiValue expr , RosiSection section ){
        _list.add( new AbstractMap.SimpleImmutableEntry<RosiValue,RosiSection>(expr , section) );
    }
    public List<AbstractMap.SimpleImmutableEntry<RosiValue,RosiSection>> getList(){ return _list ; } 
    public String formatString( String gap ){

	StringBuffer sb = new StringBuffer() ;

	sb.append( gap ).append("Condition").append("\n") ;
        if( _list != null ){
           for( Map.Entry<RosiValue,RosiSection> e : _list ){
              sb.append( e.getKey().formatString( gap + "  C : " ) ) ;
              sb.append( e.getValue().formatString( gap + "  S : " ) ) ;

           }
        }

	return sb.toString() ;
    }
}
