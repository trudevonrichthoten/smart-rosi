package org.rosi.nodes ;

import java.util.Map ;


public interface Calculatable<T> {

    T calculate( Map<String,RosiValue> map ) ;
    
}
