package org.rosi.compiler ;

import org.parboiled.* ;
import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.*;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.support.* ;
import org.parboiled.parserunners.* ;
import org.parboiled.trees.ImmutableBinaryTreeNode ;
import java.io.* ;
import java.util.* ;

import org.rosi.util.*;
import org.rosi.nodes.* ;
/**
  *  Builds the complete tree from the code. Name resolving is not done here.
  *  RosiProgram contains the main pointer to the tree plus pointers to 
  *  a list of timePatches and devices.
  *
  */
public class Rosi2Loader {

   private RosiProgram _program      = null ;
   private RosiData    _compiledTree = null ;
   
   public Rosi2Loader( RosiData compiledTree ){
/* -------------------------------------------*/
      _compiledTree = compiledTree ;
      _program      = new RosiProgram() ;
   }  
   public RosiProgram load( ) throws IllegalArgumentException {
/* ------------------------------------------------------------*/

       RosiValue resultRosiValue = convertRosiDataToRosiValue( _compiledTree ) ;
       if( !(  resultRosiValue instanceof RosiProgram ) )
         throw new
         IllegalArgumentException("Post compilation didn't return a 'RosiProgram'");

       return (RosiProgram)resultRosiValue ;
   }
   private RosiTimerDevice processTimer( RosiData cursor ) throws IllegalArgumentException {
/* -----------------------------------------------------------------------------------------*/

      String    deviceName = cursor.left().getValue() ;
      RosiValue delayValue = convertRosiDataToRosiValue( cursor.right() ) ;
      long      delay      = ((NumberValue)delayValue).getInt() ;

      RosiTimerDevice timer = new RosiTimerDevice(deviceName,delay) ;

      List<RosiData> list = cursor.getList();  

      timer.setRepeat( list.get(0).getValue().equals("repeat") ) ;
 
      String functionName = "main" ;

      if( list.size() > 1 ) functionName = convertRosiDataToRosiValue( list.get(1) ).getValueAsString() ;          

      timer.setFunctionName( functionName ) ;

      return timer ;
   
   }
   private RosiRandomDevice processRandom( RosiData cursor ) throws IllegalArgumentException {
/* -------------------------------------------------------------------------------------------*/

      String deviceName = cursor.left().getValue() ;

      RosiRandomDevice random = new RosiRandomDevice(deviceName) ;

      String helpString = "define random <name> <minTrueInterval> <maxTrueInterval> [<minFalse> <maxFalse>]" ; 

      List<RosiData> list = cursor.getList() ;

      if( ( list.size() != 2 ) && ( list.size() != 4 ) )
        throw new 
        IllegalArgumentException(helpString);

      RosiValue min = convertRosiDataToRosiValue( list.get(0));
      RosiValue max = convertRosiDataToRosiValue( list.get(1));
  
      random.setTrueInterval( (int)min.getValueAsNumber() , (int)max.getValueAsNumber() ) ;

      if( list.size() == 4 ){

           min = convertRosiDataToRosiValue(list.get(2));
           max = convertRosiDataToRosiValue(list.get(3));
  
          random.setFalseInterval( (int)min.getValueAsNumber() , (int)max.getValueAsNumber() ) ;

       }

       return random ;
   }
   private RosiMonoflopDevice processMonoflop( RosiData cursor ) throws IllegalArgumentException {
/* ---------------------------------------------------------------------------------------------*/

      String helpString = "define monoflop <name> <targetVariable> <delay/seconds> [<value>]" ;
		   
      String deviceName = cursor.left().getValue() ;
      String targetName = cursor.right().getValue(); 

      List<RosiData> list = cursor.getList() ;
		   
      RosiData data = list.get(0) ;

      RosiValue delayValue = convertRosiDataToRosiValue( data ) ;
      long      delay      = ((NumberValue)delayValue).getInt() ;
		   
      RosiMonoflopDevice device = new RosiMonoflopDevice( deviceName , targetName , delay ) ;

      int args = list.size() ;

      if( args > 1 ){

          RosiValue value = convertRosiDataToRosiValue( list.get(1) ) ;
          if( value instanceof VariableValue ){

              String option       = value.getValueAsString() ;
              if( option.equals("trigger") ){

                   if( args > 2 ){  
                       RosiValue triggerValue = convertRosiDataToRosiValue( list.get(2) ) ;
                       device.addTriggerValue( triggerValue ) ;
                   }else{
                      throw new
                      IllegalArgumentException("More arguments required for '"+option+" in definition of : "+deviceName);
                   }

              }else if( option.equals("trigger_if_changed") ){
                   device.setTriggerIfChanged(true);
              }else{
                 throw new
                 IllegalArgumentException("Illegal keywork '"+option+" in definition of : "+deviceName);
              }

          }else{
              throw new
              IllegalArgumentException("Syntax error in definition of : "+deviceName);

          }
      }

      return device ;   
   }
   private RosiActorDevice processActor( RosiData cursor ) throws IllegalArgumentException {
/* ----------------------------------------------------------------------------------------*/

      String deviceName = cursor.left().getValue() ;
      RosiValue   value = convertRosiDataToRosiValue( cursor.right() ) ;

      RosiActorDevice device = new RosiActorDevice( deviceName , value ) ;

      return device ;
   }
   private RosiSensorDevice processSensor( RosiData cursor ) throws IllegalArgumentException {
/* -----------------------------------------------------------------------------------------*/

      RosiValue deviceVariable = convertRosiDataToRosiValue( cursor.left() ) ;
      RosiValue value          = convertRosiDataToRosiValue( cursor.right()) ;

      RosiSensorDevice device = new RosiSensorDevice( (VariableValue)deviceVariable , value ) ;

      List<RosiData> arguments = cursor.getList() ;

      int args = arguments.size() ;

      if( args > 0 ){
         //
         //   trigger | trigger_if_changed  [functionCall]
         //
                value        = convertRosiDataToRosiValue( arguments.get(0) ) ;
         String option       = value.getValueAsString() ; 

         if( value instanceof VariableValue    ){
             if( option.equals("trigger" ) ){
                 device.setTrigger(true);
             }else if( option.equals("trigger_if_changed") ){
                 device.setTriggerIfChanged(true);
             }else{
                throw new
                IllegalArgumentException("Illegal keyword in 'sensor' definition "+option);
             }
         }else{
             throw new
             IllegalArgumentException("Expected keyword in 'sensor' found "+value.getClass().getName());
         }

         String functionName = null ;

         if( args == 1 ){

            functionName = "main" ;

         }else if( args == 2 ){

            value = convertRosiDataToRosiValue( arguments.get(1) ) ;

            if( value instanceof RosiFunctionCall ){

                RosiFunctionCall functionCall = (RosiFunctionCall) value ;

                _program.addFunctionCall( functionCall ) ; 

                functionName = functionCall.getValueAsString() ;

             }else{
                 throw new
                 IllegalArgumentException("Last keyword not a function in 'sensor' definition");
             }
         }else if( arguments.size() > 2 ){
             throw new
             IllegalArgumentException("Too many keywords in 'sensor' definition");
         }

         device.addTriggerFunction( functionName );

       
      }
   
      return device ;
   }
   /**
     *   FullTimePatch processFullTime 
     *  -------------------------------
     */
   private FullTimePatch processFullTime( RosiData assigmentNode  ) throws IllegalArgumentException {
/* --------------------------------------------------------------------------------------------------*/

      RosiData timePatchSide = assigmentNode.left() ;
      RosiData dataSide      = assigmentNode.right() ;

      if( ! timePatchSide.getLabel().equals("TimePatchList" ) )

	 throw new
	 IllegalArgumentException(
	    "Expected RegularList, found "+timePatchSide.getLabel()+
	    " in FullTime left side");

      if( ! dataSide.getLabel().equals("DatePatchList" ) )
	 throw new
	 IllegalArgumentException(
	    "Expected RegularList, found "+timePatchSide.getLabel()+
	    " in FullTime left side");

      List<RosiData> list = timePatchSide.getList() ;

      TimePatch fullTimePatch = new TimePatch() ;

      for( RosiData timePatchCursor : list ){

	  RosiValue value = convertRosiDataToRosiValue( timePatchCursor ) ;

	  if( value instanceof TimePatch ){

	     TimePatch timePatch = (TimePatch) convertRosiDataToRosiValue(timePatchCursor ) ;

	     fullTimePatch.addPatch( timePatch ) ;

	  }else{

	     throw new
		IllegalArgumentException(
	           "Expected TimePatch, found "+value.getClass()+" in FullTime left side");
	  }

      }

      list = dataSide.getList() ;

      DatePatch fullDatePatch = new DatePatch() ;
      DayValue  days          = new DayValue() ;

      for( RosiData datePatchCursor : list ){

	  RosiValue value = convertRosiDataToRosiValue( datePatchCursor ) ;

	  if( value instanceof DatePatch ){

	     DatePatch datePatch = (DatePatch) value;

	     if( datePatch.isDateValue() )datePatch = new DatePatch( datePatch , datePatch ) ;

	     fullDatePatch.addPatch( datePatch ) ;

	  }else if( value instanceof DayValue ){

	     days.addDay( (DayValue) value ) ;

	  }else{

	     throw new
		IllegalArgumentException(
	           "Expected DatePatch, found "+value.getClass()+" in FullTime left side");
	  }

      }
      fullDatePatch.addDays( days ) ;

      return new FullTimePatch( fullTimePatch , fullDatePatch ) ;

   }
   private RosiSection processSection( RosiData section ) throws IllegalArgumentException {
/* ---------------------------------------------------------------------------------------*/

          RosiSection  fun = new RosiSection() ;
          String   nt      = section.getLabel() ;

          if( ! nt.equals( "Section" ) )
              throw new
              IllegalArgumentException("Right side of a Function needs to be a Section: Found : "+nt);

          for( RosiData element : section.list() ){

               RosiValue val = convertRosiDataToRosiValue( element ) ;

               if( val instanceof RosiAssigment ){
                  fun.add( (RosiAssigment) val ) ;
               }else if( val instanceof Rosi2Conditional ){
                  fun.add( (Rosi2Conditional) val ) ;
               }else if( val instanceof RosiReturnStatement ){
                  fun.add( (RosiReturnStatement) val ) ;
               }else if( val instanceof RosiSection ){
                  fun.add( (RosiSection) val ) ;
               }else if( val instanceof RosiFunctionCall ){
                  fun.add( (RosiFunctionCall) val ) ;
                  _program.addFunctionCall( (RosiFunctionCall) val  );
               }else{
                  throw new
                  IllegalArgumentException(" Unknown type in : "+val );
               }

          }
          return fun ;
   }
   /*
   **--------------------------------------------------------------------------------------
   **
    *   converts the Cmopiler tree to our executable tree.
    */
   private RosiValue convertRosiDataToRosiValue( RosiData data ) throws IllegalArgumentException {
   /* ----------------------------------------------------------------------------------------------*/

       String    dataType = data.getLabel() ;
       //RosiValue value    = null ;
       /*
       **
       */
       //System.out.println("Converting : "+data.getLabel() ) ;

       if( dataType.equals( "Program" ) ){
       /*-----------------------------------------*/
 
           for( RosiData element : data.list() ){

              String elementType = element.getLabel() ;

              if( elementType.equals("DeviceDefinitionList") ){
              /*-----------------------------------------*/
                  for( RosiData def : element.list() ){
                      _program.addDevice( (RosiDevice)convertRosiDataToRosiValue( def) ) ;
                  }
              }else if( elementType.equals("Function") ){
              /*-----------------------------------------*/
                  _program.addFunction( (RosiFunction)convertRosiDataToRosiValue( element ) ) ;
              }else{
                throw new
                IllegalArgumentException("Illegal Item found in Program : "+elementType);
              }
           }
           return _program ;

       }else if( dataType.equals( "Return" ) ){
       /*-----------------------------------------*/

          RosiValue constOrExpr = convertRosiDataToRosiValue(data.list().get(0)) ;
/* 
          return new RosiReturnStatement( convertToExpressionValue( constOrExpr) ) ;
*/
          return new RosiReturnStatement(  constOrExpr ) ;

       }else if( dataType.equals( "FunctionCall" ) ){
       /*-----------------------------------------*/
  
          VariableValue    var  = (VariableValue)convertRosiDataToRosiValue( data.left() ) ;
          RosiVectorValue  args = (RosiVectorValue)convertRosiDataToRosiValue( data.right() )  ;
 
          RosiFunctionCall call = new RosiFunctionCall( var.getVariableName() );

          call.setArguments( args ) ;

          _program.addFunctionCall( call ) ;

          return call ;

       }else if( dataType.equals( "FullTimePatch" ) ){
       /*-----------------------------------------*/

           FullTimePatch patch =  processFullTime( data ) ;
           _program.addTimePatch( patch ) ;
           return patch ;

       }else if( dataType.equals( "Conditional" ) ){
       /*-----------------------------------------*/

           Rosi2Conditional cond = new Rosi2Conditional() ;
           RosiValue    expr     = null ;
           RosiSection  section  = null ;

           for( RosiData rosiData : data.list() ){

               RosiValue val = convertRosiDataToRosiValue( rosiData ) ;

               if( val instanceof RosiSection ){

                  if( ( section != null ) || ( expr == null ) )
                     throw new
                     IllegalArgumentException("Illegal Sequence (2) in decoding condition : "+val);

                  section = (RosiSection)val ;

                  cond.addConditionalSection( expr , section ) ;
 
                  section = null ;
                  expr    = null ;

               }else{ 

                  if( section != null )
                     throw new
                     IllegalArgumentException("Illegal Sequence in decoding condition : "+val);

                   expr = val ; 

               }
           } 
           return cond ;
           
       }else if( dataType.equals( "Assigment" ) ){
       /*-----------------------------------------*/

          RosiData variableNameNode = data.left() ;
          RosiData assigmentNode    = data.right() ;
         
             return new RosiAssigment(  
                 (VariableValue)convertRosiDataToRosiValue(variableNameNode) , 
                 convertRosiDataToRosiValue( assigmentNode )  ) ;
         
       }else if( dataType.equals( "FormalArgumentList" ) ){
       /*-----------------------------------------*/

          List<RosiData> list = data.getList() ;
          RosiVectorValue vector = new RosiVectorValue("FormalArgumentList") ;
          for( RosiData d : list ){
             RosiValue v = convertRosiDataToRosiValue( d ) ;
             if( ! ( v instanceof VariableValue ) )
                throw new
                IllegalArgumentException("FormalArgumentList must only contain variable");

             vector.add( v ) ;
          }
          return vector ;
       }else if( dataType.equals( "ArgumentList" ) ){
       /*-----------------------------------------*/

          List<RosiData> list = data.getList() ;
          RosiVectorValue vector = new RosiVectorValue("ArgumentList") ;
          for( RosiData d : list ){
             RosiValue v = convertRosiDataToRosiValue( d ) ;
             vector.add( v ) ;
          }
          return vector ;
       }else if( dataType.equals( "Function" ) ){
       /*-----------------------------------------*/

          RosiValue  varValue = convertRosiDataToRosiValue( data.left() ) ;
          String varValueName = varValue.getValueAsString() ;

          RosiFunction fun    = new RosiFunction( varValueName ) ; 

          fun.setArgumentList( (RosiVectorValue)convertRosiDataToRosiValue( data.right() ) ) ;

          fun.setSection( processSection( data.getList().get(0) ) ) ;
 
          return fun ;

       }else if( dataType.equals( "Section" ) ){
       /*-----------------------------------------*/

          return processSection( data ) ;

       }else if( dataType.equals( "SensorDeviceDefinition" ) ){
       /*-----------------------------------------*/
 
           return processSensor( data ) ;

       }else if( dataType.equals( "ActorDeviceDefinition" ) ){
       /*-----------------------------------------*/
 
           return processActor( data ) ;

       }else if( dataType.equals( "MonoflopDeviceDefinition" ) ){
       /*-----------------------------------------*/
 
           return processMonoflop( data ) ;

       }else if( dataType.equals( "TimerDeviceDefinition" ) ){
       /*-----------------------------------------*/
 
           return processTimer( data ) ;

       }else if( dataType.equals( "RandomDeviceDefinition" ) ){
       /*-----------------------------------------*/
 
           return processRandom( data ) ;

       }else if( dataType.equals( "StringLiteral" ) ){
       /*-----------------------------------------*/

	   return new StringValue( data.getValue() ) ; 

       }else if( dataType.equals( "Variable" ) ){
       /*-----------------------------------------*/

	   String    variableName = data.getValue() ;
	   VariableValue variable = new VariableValue( variableName ) ;

           if( data.list().size() > 0 )
                variable.setIndexExpression( convertRosiDataToRosiValue(data.list().get(0)) ) ;

           return variable;

       }else if( dataType.equals( "Number" ) ){
       /*-----------------------------------------*/

	   return new NumberValue( data.getValue() ) ;

       }else if( dataType.equals( "Boolean" ) ){
       /*-----------------------------------------*/

	   return new BooleanValue( data.getValue().equals("true") ) ;

       }else if( dataType.equals( "Float" ) ){
       /*-----------------------------------------*/

	   return new FloatValue( data.getValue() ) ;

       }else if( dataType.equals( "TimeValue" ) ){
       /*-----------------------------------------*/

           return new TimeValue( data.getValue() ) ;

       }else if( dataType.equals( "DateValue" ) ){
       /*-----------------------------------------*/

           return new DatePatch( data.getValue() ) ;

       }else if( dataType.equals( "&"  ) | dataType.equals( "|"  )  |
        	 dataType.equals( "==" ) | dataType.equals( ">=" )  |
        	 dataType.equals( "!=" ) | dataType.equals( "<=" )  |
        	 dataType.equals( "+"  ) | dataType.equals( "-"  )  |
        	 dataType.equals( "*"  ) | dataType.equals( "/"  )  |
        	 dataType.equals( "<"  ) | dataType.equals( ">"  )      ){
       /*----------------------------------------------------------------*/

           return new ExpressionValue( 
	                   dataType , 
	                   convertRosiDataToRosiValue( data.left() ) , 
		           convertRosiDataToRosiValue( data.right() ) 
		   ) ;

       }else if( dataType.equals( "OP!"  ) ){
       /*----------------------------------------------------------------*/

           return new ExpressionValue( 
	                   dataType , 
	                   convertRosiDataToRosiValue( data.left() ) ) ; 

       }else if( dataType.equals( "TimePatch" ) ){
       /*-----------------------------------------*/

           return new TimePatch( 
	              (TimeValue)convertRosiDataToRosiValue( data.left() ) , 
		      (TimeValue)convertRosiDataToRosiValue( data.right() ) 
		        	) ;

       }else if( dataType.equals( "DatePatch" ) ){
       /*----------------------------------------------------------------*/

           return new DatePatch( 
	              (DatePatch)convertRosiDataToRosiValue( data.left() ) , 
		      (DatePatch)convertRosiDataToRosiValue( data.right() ) 
		        	) ;

       }else if( dataType.equals( "DayOfWeek" ) ){
       /*----------------------------------------------------------------*/

           return new DayValue( data.getValue() ) ;

       }else{

	   throw new 
	   IllegalArgumentException("Can't convert : "+dataType ) ;

       }

   }
/*
   private RosiValue convertToExpressionValue( RosiValue value ) throws IllegalArgumentException{
      return value ;
   }
*/
 
   public static void main( String [] args ) throws Exception {
   
      if( args.length == 0 ){
            System.err.println("Usage : ... <RosiLanguageFile>");
            System.exit(4);
       }
       
       String filename = args[0] ;
       
       File file = new File( filename ) ;
       
       if( ! file.exists() ){
            System.err.println("File not found : "+filename);
            System.exit(4);
       }

       System.out.println("Processing Rosi File : "+filename);

       StringBuffer   sb     = new StringBuffer() ;
       String         input  = null ;

       BufferedReader reader = new BufferedReader( new FileReader( file ) ) ;

       while( ( input = reader.readLine() ) != null ){
         sb.append( input ).append("\n");
       }

      Rosi2Compiler compiler = new Rosi2Compiler() ;
              
      RosiData compiledTree = compiler.compile( sb.toString() ) ;

      Rosi2Loader loader  = new Rosi2Loader(compiledTree) ;
      
      RosiProgram program = loader.load() ;
      System.out.println("------------------------------------"); 
      System.out.println( program.toString() ) ;
      System.out.println("------------------------------------"); 
      System.out.println(" Trying to resolve functions");
      try{
         if( ! program.checkFunctions() )
         System.out.println("Warning : Some functions seem to be duplicated");
      }catch(RosiRuntimeException rre ){
         System.err.println("Error : "+rre.getMessage());
      }      

      
      System.exit(0);

   }

}
