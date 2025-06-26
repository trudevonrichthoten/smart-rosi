package org.rosi.execution ;

import org.rosi.nodes.* ;
import org.rosi.util.*;
import org.rosi.compiler.* ;

import java.io.* ;
import java.util.* ;


public class ProgramRegister {

   private Map<String,RosiValue>        _globals          = new HashMap<String,RosiValue>() ;

   public ProgramRegister(){
   }
   public boolean checkIfGlobal( String name ){
      return _globals.containsKey(name);
   }
   public Map<String,RosiValue> getGlobals(){ return _globals ; }

   public Set<Map.Entry<String,RosiValue>> entrySet(){ 
/*           -----------------------------------------*/
       return _globals.entrySet() ; 
   }
   public Map<String,RosiValue> sorted(){  
/* ---------------------------------------*/
       return new TreeMap<String,RosiValue>(_globals) ; 
   }
/**
  *   Managing the Sensors
  *   --------------------
  *
  */
   private RosiValue getSensorValue( String sensorName ) throws IllegalArgumentException {
/* ---------------------------------------------------------------------------------------*/
  
       RosiValue value = _globals.get( sensorName ) ;
       if( value == null )
          throw new
          IllegalArgumentException( "Sensor not found : "+sensorName ) ;

       if( ! (value instanceof RosiSensorDevice ) )
           throw new
           IllegalArgumentException( "Not a sensor : "+sensorName ) ;

       return ((RosiSensorDevice)value).getValue() ;
   }
   /**
     *
     */
   public List<String> setSensorValue( String sensorName , String sensorValue ) throws RosiRuntimeException {
/* --------------------------------------------------------------------------------------------------------*/

       RosiSensorDevice device = getSensor( sensorName );

       device.setSensorValue( sensorValue ) ;

       return ((RosiSensorDevice)device).functions() ;

   }

   public void setSensor( String sensorName , String sensorValue ) throws IllegalArgumentException {
/* -------------------------------------------------------------------------------------------------*/

       RosiValue value = getSensorValue( sensorName ) ;

       if( ! ( value instanceof StringValue ) )
          throw new
          IllegalArgumentException("Not a 'string' sensor : "+sensorValue ) ;

       ((StringValue)value).setValue( sensorValue ) ;

   }
   public void setSensor( String sensorName , float sensorValue ) throws IllegalArgumentException {
/* -----------------------------------------*/

       RosiValue value = getSensorValue( sensorName ) ;

       if( value instanceof FloatValue ){

           ((FloatValue)value).setValue( sensorValue ) ;

       }else{

          throw new
          IllegalArgumentException("Not a 'float' sensor : "+sensorValue ) ;

       }

   }
   public void setSensor( String sensorName , int sensorValue ) throws IllegalArgumentException {
/* -----------------------------------------*/

       RosiValue value = getSensorValue( sensorName ) ;

       if( value instanceof NumberValue ){

           ((NumberValue)value).setValue( sensorValue ) ;

       }else if( value instanceof FloatValue ){

           ((FloatValue)value).setValue( (float)sensorValue ) ;

       }else{

          throw new
          IllegalArgumentException("Not a 'float' sensor : "+sensorValue ) ;

       }

   }

   /**
     *
     */
   private RosiSensorDevice getSensor( String sensorName ) throws IllegalArgumentException {
/* -----------------------------------------------------------------------------------------*/

       RosiValue value = _globals.get( sensorName ) ;
       if( value == null )
          throw new
          IllegalArgumentException( "Sensor not found : "+sensorName ) ;

       if( ! (value instanceof RosiSensorDevice ) )
           throw new
           IllegalArgumentException( "Not a sensor : "+sensorName ) ;

       return (RosiSensorDevice)value ;

   }
   public boolean shouldTrigger( String sensorName ) throws IllegalArgumentException {
/* -----------------------------------------------------------------------------------*/

       return ((RosiSensorDevice)getSensor(sensorName)).isTrigger() ;

   }
/**
  *   Managing the Actors
  *   -------------------
  *
  */
   public void clearActors(){
/* --------------------------*/

      for( Map.Entry<String,RosiValue> entry : _globals.entrySet() ){

          RosiValue variable  = entry.getValue() ;

          if( variable instanceof RosiActorDevice ){
             ((RosiActorDevice)variable).clear() ;
          }
      }

   }
   public List<RosiActorDevice> getActors(){
/* -----------------------------------------*/

      List<RosiActorDevice> list = new ArrayList<RosiActorDevice>() ;

      for( Map.Entry<String,RosiValue> entry : _globals.entrySet() ){

          RosiValue value = entry.getValue() ;

          if(  value instanceof RosiActorDevice   ){

                 list.add( (RosiActorDevice)value ) ;

           }

      }
      return list ;

   }
   public List<RosiActorDevice> getChangedActors(){
/* -------------------------------------------------*/

      List<RosiActorDevice> list = new ArrayList<RosiActorDevice>() ;

      for( Map.Entry<String,RosiValue> entry : _globals.entrySet() ){

          RosiValue value = entry.getValue() ;

          if( (  value instanceof RosiActorDevice   )   &&
              ( ((RosiActorDevice)value).wasChanged() )    ){

                 list.add( (RosiActorDevice)value ) ;

           }

      }
      return list ;

   }
   public void clearMonoflops(){
/* --------------------------------*/

      for( Map.Entry<String,RosiValue> entry : _globals.entrySet() ){

          RosiValue variable  = entry.getValue() ;

          if( variable instanceof RosiMonoflopDevice ){
             ((RosiMonoflopDevice)variable).clear() ;
          }
      }
   }
/**
  *   Printouts 
  *   -------------------
  *
  */
   public String toShortString(){
/* --------------------------------*/

      String variableName = null ;
      String indexName    = null ;

      int maxLen = 0 ;
      for( Map.Entry<String,RosiValue> entry : _globals.entrySet() ){

          variableName = entry.getKey().toString();

          if( entry.getValue() instanceof RosiArray ){
             RosiArray array = (RosiArray)entry.getValue() ;
             Map<String,RosiConstant> map = array.getMap();          
             for( Map.Entry<String,RosiConstant> c : map.entrySet() ){
                indexName = c.getKey().toString() ;
                maxLen = Math.max( (variableName.length()+indexName.length()+2) , maxLen ) ;
             }
          }else{
             maxLen = Math.max( variableName.length() , maxLen ) ;
          }

      }
      maxLen += 2 ;

     // TreeMap<String,RosiValue> sorted = new TreeMap<String,RosiValue>(_globals) ;
      Map<String,RosiValue> sorted = this.sorted() ;

      StringBuffer sb = new StringBuffer() ;
      for( Map.Entry<String,RosiValue> entry : sorted.entrySet() ){
          
          if( entry.getValue() instanceof RosiArray ){
             RosiArray array = (RosiArray)entry.getValue() ;
             Map<String,RosiConstant> map = array.getMap();
             for( Map.Entry<String,RosiConstant> c : map.entrySet() ){

                variableName = entry.getKey().toString() ;
                indexName    = c.getKey().toString() ;

                sb.append(variableName).append("[").append(indexName).append("]") ; 
                for( int i = ( variableName.length() + indexName.length() ) ; i < maxLen ; i++ )
                   sb.append(".") ;
                sb.append( " [").append( c.getValue().getRosiType() ).
                                 append( c.getValue().getValueType() ).
                   append("] .. ");
                sb.append( c.getValue().getValueAsString() ) ;
                sb.append("\n");
             }
          }else{

             variableName = entry.getKey().toString() ;
             sb.append(variableName).append(" ") ;
             for( int i = variableName.length() ; i < maxLen ; i++ )
                sb.append(".") ;
             sb.append(" ");
             sb.append( " [").append( entry.getValue().getRosiType() ).
                              append( entry.getValue().getValueType() ).
                append("] .. ");
             sb.append( entry.getValue().getValueAsString() ) ;
/*
             sb.append( " ... ").append( entry.getValue().getClass().getName() ) ;
             if( entry.getValue() instanceof RosiDataDevice )
             sb.append( " ... ").append( ((RosiDataDevice)entry.getValue()).getValue().getClass().getName() ) ;
*/
             sb.append("\n");

          }

      }
      return sb.toString();
  }
  public static void main( String [] args ) throws Exception {
      Stack<String> s = new Stack<String>() ;
      s.push("fist") ;
      s.push("second") ;
      for( String x : s ){
         System.out.println("found : "+x );
      }

      System.out.println("x : "+s.firstElement());

  }
}

