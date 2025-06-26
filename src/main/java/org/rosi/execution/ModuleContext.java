package org.rosi.execution ;

import java.util.Map ;

public class ModuleContext {

    private Map<String,String> _propertyMap = null ;
    private String _name = null ;

    public ModuleContext( String name ,  Map<String,String> propertyMap ){
       _name        = name ;
       _propertyMap = propertyMap ;
    }
    public String getName(){
       return _name ;
    }
    public Map<String,String> getProperties(){
       return _propertyMap ;
    }
    public String get( String name ){
       return _propertyMap.get(name);
    }
    public String get( String name , boolean insist ) 
      throws IllegalArgumentException
    {
       String value =  _propertyMap.get(name);
       if( insist && ( value == null ) )
         throw new
         IllegalArgumentException(
            "Module "+_name+" needs '"+name+"' to be defined" 
         ) ;

       return value ;
    }
}
