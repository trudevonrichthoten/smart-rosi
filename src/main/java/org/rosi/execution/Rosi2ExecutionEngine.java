package org.rosi.execution ;

import org.rosi.nodes.* ;
import org.rosi.util.*;
import org.rosi.compiler.* ;

import java.io.* ;
import java.util.* ;

import java.util.Date ;
import java.util.Calendar ;
import java.text.SimpleDateFormat ;
import java.text.ParseException ;




public class Rosi2ExecutionEngine {

   private Stack<FunctionContext>
                            __functionContextStack = new Stack<FunctionContext>() ;
   private RosiProgram      __masterProgram  = null ;  
   private RosiFunction     __masterFunction = null; 
   private ProgramRegister  __globalRegisters= new ProgramRegister() ;
   private InternalRegister __registers      = new InternalRegister() ;
   private ProgramCode      __code           = new ProgramCode();

   private class InternalRegister {
       public InternalRegister(){
          __functionContextStack.push(
                 new FunctionContext("$",new HashMap<String,RosiValue>())
                                     );
       }
       public RosiConstant get( VariableValue name , boolean expectResolvable ) 
          throws RosiRuntimeException {

          RosiConstant result =  __functionContextStack.lastElement().get( name ) ; 

          if( ( ! expectResolvable ) || ( result != null ) )return result ;

          FunctionContext context = __functionContextStack.lastElement(); 

          throw new
          RosiRuntimeException(
               "Variable <"+name.getVariableName()+"> not found.",
               context.getFunctionName()
                              );

       }
       public void put( VariableValue name , RosiConstant value )
         throws RosiRuntimeException{
          __functionContextStack.lastElement().put( name , value ) ;
       }

       public RosiValue get( String name )
          throws RosiRuntimeException {
          return __functionContextStack.lastElement().get( name ) ; 
       }
       public void put( String name , RosiConstant value )
          throws RosiRuntimeException {
          __functionContextStack.lastElement().put( new VariableValue(name) , value ) ;
       }
       public void push( FunctionContext context ){
          __functionContextStack.push( context ) ;
       }
       public void pop(){
          __functionContextStack.pop() ;
       }
   }
   private String getContextName(){
      return    __functionContextStack.lastElement().getFunctionName();
   }
   private class FunctionContext {

      private Stack<Map<String,RosiValue>> sectionStack = new Stack<Map<String,RosiValue>>() ;
      private String name = null ;

      private FunctionContext( String name , Map<String,RosiValue> parameterMap ){

           this.name = name ;
           sectionStack.push( __globalRegisters.getGlobals() ) ; 
           if( ! name.equals("$") )sectionStack.push( new HashMap<String,RosiValue>(parameterMap) ) ;

      }
      public String getFunctionName(){
        return this.name ;
      }
      private RosiConstant get( String name )
        throws RosiRuntimeException
      {
      /* ------------------------------- */
         return get( new VariableValue(name) ) ; 
      }
      /**
        * Get the value of the variable. (Can return RosiConstant)
        */ 
      private RosiConstant get( VariableValue variable )
        throws RosiRuntimeException
      {
      /* ----------------------------------------------- */
         String    name  = variable.getVariableName() ;
         RosiValue value = null ;

         for( int i = this.sectionStack.size() - 1 ; i >= 0 ; i-- ){

            Map<String,RosiValue> map = sectionStack.get(i); 

            if( ( value = map.get(name) ) != null ){

              if( value instanceof RosiArray ){
                 if( ! variable.isArray() )
                    throw new
                    IllegalArgumentException("'array' <-> 'scalar' mismatch with : "+name); 

                 RosiArray    array    = (RosiArray)value;
                 RosiConstant resolved = calculateExpression( variable.getIndexExpression() ) ;

                 return array.get( resolved.getValueAsString() );

              }else{ 

                 if( variable.isArray() )
                    throw new
                    IllegalArgumentException("'array' <-> 'scalar' mismatch with : "+name); 

                 return (RosiConstant)value ;
              }
            }
         } 
         return null ; 
      }
      /**
        * Put the value of the variable.
        */ 
      private void put( VariableValue variable , RosiConstant value )
        throws RosiRuntimeException {
      /* ---------------------------------------------------------- */
         String    name   = variable.getVariableName() ;
         RosiValue result = null ;

         for( int i = this.sectionStack.size() - 1 ; i >= 0 ; i-- ){

            Map<String,RosiValue> map = sectionStack.get(i); 

            if( ( result = map.get(name) ) != null ){

               if( result instanceof RosiArray ){

                 if( ! variable.isArray() )
                    throw new
                    IllegalArgumentException("'array' <-> 'scalar' mismatch with : "+name); 

                 RosiArray    array    = (RosiArray)result;
                 RosiConstant resolved = calculateExpression( variable.getIndexExpression() ) ;
                 array.put(resolved.getValueAsString(),value);

               }else{

                 if( variable.isArray() )
                    throw new
                    IllegalArgumentException("'array' <-> 'scalar' mismatch with : "+name); 

                  map.put( name , value ) ;
               }
               return ;
            }
         } 
         if( variable.isArray() ){

            RosiArray     array   = new RosiArray() ;
            RosiConstant resolved = calculateExpression( variable.getIndexExpression() ) ;

            array.put(resolved.getValueAsString(),value);
            this.sectionStack.lastElement().put(name,array);

         }else{ 
            this.sectionStack.lastElement().put(name,value);
         }

      }
      private void put( String name , RosiConstant value )
        throws RosiRuntimeException {
          put( new VariableValue(name) , value ) ;
      }
      private void pushSection(){
         this.sectionStack.push( new HashMap<String,RosiValue>() );
      }
      private void popSection(){
         this.sectionStack.pop();
      }
   }
   private PrintStream _outStream = System.out ;
   private PrintStream _errStream = System.err ;
   private void setOutPrintStreams( PrintStream out ){
      _outStream = out == null ? System.out : out ;
   }
   private void setErrPrintStreams( PrintStream err ){
      _errStream = err  == null ? System.err : err ;

   }
   public void setPrintStreams( PrintStream out , PrintStream err ){
      _outStream = out ;
      _errStream = err ;
   }
   public Rosi2ExecutionEngine( ) throws Exception { }
   public Rosi2ExecutionEngine(  RosiProgram program  ) throws Exception {

       __masterProgram = program ;

       prepareExecution() ;
   }
   public void addModule( RosiProgram program ) throws Exception {

       if( __masterProgram == null ){

          __masterProgram = program ;

          prepareExecution() ;

       }else{
          collectItems( program ) ;
       }

       __code.setTime( new RosiCalendar() ) ;

   }
   public ProgramRegister getRegisters(){
       return __globalRegisters ; 
    }
   public ProgramCode getCode(){
       return __code ; 
    }
   private Rosi2ExecutionEngine prepareExecution() throws RosiRuntimeException {

       collectItems( __masterProgram ) ;

       __code.setTime( new RosiCalendar() ) ;

       __masterFunction = __code.getFunction( "main" , true ) ;

       RosiFunction initializer = __code.getFunction( "initialize" ,false ) ;

       if( initializer != null ){

          executeFunction( initializer,
                           new HashMap<String,RosiValue>(),
                           false ) ;

       }

       return this ;
   }
   public void executeFirst() throws Exception {

       if( __masterFunction == null )
          throw new
          IllegalArgumentException("'main' program not found");

      BooleanValue initValue = new BooleanValue(true) ; 

       __registers.put( "init" , initValue ) ;

      executeFunction( __masterFunction ,
                       new HashMap<String,RosiValue>(),
                       true ) ;       

      initValue.setValue(false);

   } 
   public void execute( List<String> functionList )
     throws RosiRuntimeException{

       for( String functionName : functionList ){

           RosiFunction func = __code.getFunction( functionName , true ) ;

           executeFunction(
                     func,
                     new HashMap<String,RosiValue>(),
                     true ) ;

       }
   }   
   public void execute()
      throws RosiRuntimeException {

       executeFunction( __masterFunction ,
                        new HashMap<String,RosiValue>(),
                        true );

   }
   private RosiConstant calculateExpression( RosiValue value )
      throws RosiRuntimeException {
   /* ------------------------------------------------------- */
      if( value instanceof RosiDevice ){
      /* ------------------------------- */

           RosiDevice device = (RosiDevice)value ;
           if( device instanceof RosiMonoflopDevice ){
              return new BooleanValue( ((RosiMonoflopDevice)device).getValueAsBoolean() ) ;
           }else if( device instanceof RosiRandomDevice ){
              return new BooleanValue( ((RosiRandomDevice)device).getValueAsBoolean() ) ;
           }else if( device instanceof RosiDataDevice ){
              return (RosiConstant)((RosiDataDevice)device).getValue() ;
           }else{
              throw new
              IllegalArgumentException("Can't convert : "+value.getClass().getName());
           }
      }else if( value instanceof RosiConstant ){
      /* -------------------------------------- */

          return (RosiConstant)value ;

      }else if( value instanceof VariableValue ){
      /* --------------------------------------- */

           VariableValue variable = (VariableValue)value ;
           return calculateExpression(__registers.get( variable , true  )) ; 

      }else if( value instanceof FullTimePatch ){
      /* --------------------------------------- */

           return new BooleanValue( ((FullTimePatch)value).getValueAsBoolean() ) ;

      }else if( value instanceof RosiFunctionCall ){
      /* --------------------------------------- */

           return executeFunctionCall( (RosiFunctionCall) value , true ) ;

      }else if( value instanceof ExpressionValue ){
      /* --------------------------------------- */

           return resolveExpression( (ExpressionValue) value ) ;

      }else{
           throw new
           IllegalArgumentException("Can't calculate : "+value.getClass().getName());
      }
   }
   private SimpleDateFormat _defaultTimeFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");

   private RosiConstant buildTimeString( List<RosiConstant> args ){

      SimpleDateFormat sdf = _defaultTimeFormat ;

      try{
      
         if( ( args.size() > 0 ) && ( args.get(0) instanceof StringValue ) ){
            sdf = new SimpleDateFormat(args.get(0).getValueAsString());
         }
      }catch(Exception ee ){
         return new StringValue("{"+sdf.format(new Date())+"}"); 
      }
      return new StringValue(sdf.format(new Date()));
   }
   private RosiConstant callSystemFunction( RosiFunctionCall call )
      throws RosiRuntimeException {
   /* --------------------------------------------------------------- */
     
         List<RosiConstant> actualArguments = new ArrayList<RosiConstant>() ;
         for( RosiValue a : call.getArguments().list() ){
            actualArguments.add( calculateExpression( a ) ) ;      
         }

      String functionName = call.getName() ;  
      if( functionName.equals("System.out.println" ) ){
         StringBuffer sb = new StringBuffer() ;
         for( RosiValue a : actualArguments )sb.append(a.getValueAsString());
         _outStream.println(sb.toString());
      }else if( functionName.equals("System.err.println" ) ){
         StringBuffer sb = new StringBuffer() ;
         for( RosiValue a : actualArguments )sb.append(a.getValueAsString());
         _errStream.println(sb.toString());
      }else if( functionName.equals("System.time") ){
         return buildTimeString( actualArguments ) ;
      }else if( functionName.equals("System.now") ){
         return new NumberValue( System.currentTimeMillis() ) ;
      }else if( functionName.equals("System.random") ){
         return new FloatValue( (float)Math.random() ) ;
      }else{
         throw new
         RosiRuntimeException(
             "System function not found : "+call.getName(),
             __functionContextStack.lastElement().getFunctionName()
         );
      }
      return new BooleanValue(true);
   }
   private RosiConstant executeFunctionCall( RosiFunctionCall call , boolean doPush )
      throws RosiRuntimeException {
      /*
       * find the  function.
       */
      RosiFunction func = __code.getFunction( call.getName() , false );

      if( func == null )return callSystemFunction( call )  ;
      /*
       * calculate the actual arguments given to the function call.
       */
      List<RosiConstant> actualArguments = new ArrayList<RosiConstant>() ;
      for( RosiValue a : call.getArguments().list() ){
         actualArguments.add( calculateExpression( a ) ) ;      
      }
      /* 
       * Get the formal parameters.
       */
      RosiVectorValue formalArguments = func.getArguments() ;
      /*
       * Check if formal parameters match actual parameters. (Count only).
       */
      if(  formalArguments.size() != actualArguments.size() )
          throw new
          IllegalArgumentException("Formal and actual argument list doesn't match for "+func.getName());
       /*
        * Merge actual and formal arguments.
        */
       Map<String,RosiValue>  variableMap = new HashMap<String,RosiValue>() ;
       List<RosiValue> formalArgumentList = formalArguments.list() ;
       for( int i = 0 ; i < formalArgumentList.size() ; i++ ){

            variableMap.put( 
                  formalArgumentList.get(i).getValueAsString() ,
                  actualArguments.get(i)
                           ) ;

        }

        return executeFunction( func , variableMap , doPush ) ;

   }
   private RosiConstant executeFunction( RosiFunction func , Map<String,RosiValue> map , boolean doPush )
      throws RosiRuntimeException { 

      if( doPush )__registers.push( new FunctionContext( func.getName() , map ) ) ;
      
      RosiConstant result = doPush ? 
                            executeSection( func.getSection() ) :
                            executeSectionContent( func.getSection() ) ;
  
      if( doPush )__registers.pop() ;

      return result ;
   }
   private RosiConstant resolveExpression( ExpressionValue expr  )
      throws RosiRuntimeException {

      String op = expr.getOperation() ;
      if( op.equals("nop") ){

         return calculateExpression( expr.left() ) ;  

      }else if( op.equals("OP!") ){

         RosiConstant res =  calculateExpression( expr.left() ) ;  
    
         return new BooleanValue( ! res.getValueAsBoolean() ) ;  

      }else{
 
         RosiConstant left  = calculateExpression( expr.left() ) ; 
         RosiConstant right = calculateExpression( expr.right() ) ; 
        
         return evaluateExpression( op , left , right ) ;

      }
   }
   private RosiConstant evaluateExpression( String operation , 
                                         RosiConstant left ,
                                         RosiConstant right ) throws IllegalArgumentException {

        if( operation.equals("|") ){

           return new BooleanValue( left.getValueAsBoolean() || right.getValueAsBoolean() ) ;

        }else if( operation.equals("&") ){

           return new BooleanValue( left.getValueAsBoolean() && right.getValueAsBoolean() ) ;

        }else if( operation.equals("==") ){

           return new BooleanValue( left.compareTo( right ) == 0 ) ;

        }else if( operation.equals("!=") ){

           return new BooleanValue( left.compareTo( right ) != 0 ) ;

        }else if( operation.equals(">") ){

           return new BooleanValue( left.compareTo( right ) > 0 ) ;

        }else if( operation.equals("<") ){

           return new BooleanValue( left.compareTo( right ) < 0 ) ;

        }else if( operation.equals("<=") ){

           return new BooleanValue( left.compareTo( right ) <= 0 ) ;

        }else if( operation.equals(">=") ){

           return new BooleanValue( left.compareTo( right ) >= 0 ) ;

        }else if( operation.equals("/") ){

           return executeOperation( "/" , left , right ) ;

        }else if( operation.equals("*") ){

           return executeOperation( "*" , left , right ) ;

        }else if( operation.equals("-") ){

           return executeOperation( "-" , left , right ) ;

        }else if( operation.equals("+") ){

           return executeOperation( "+" , left , right ) ;

        }else{

           throw new
           IllegalArgumentException("Operation not support yet : "+operation ) ;

        }

   }
   private RosiConstant executeOperation( String op , RosiValue left , RosiValue right ){
      if( op.equals("+") ){
         if( left instanceof StringValue ){
            return new StringValue( left.getValueAsString() + right.getValueAsString()  ) ;
         }else if( ( left instanceof FloatValue ) || (right instanceof FloatValue ) ){
            return new FloatValue( left.getValueAsFloat() + right.getValueAsFloat() ) ;
         }else if( ( left instanceof NumberValue ) && ( left instanceof NumberValue ) ){
            return new NumberValue( left.getValueAsNumber() + right.getValueAsNumber() ) ;
         }else{
            throw new
            IllegalArgumentException("Operation "+op+" not supported for "+left);
         }
      }else if( op.equals("-") ){
         if( ( left instanceof FloatValue ) || (right instanceof FloatValue ) ){
            return new FloatValue( left.getValueAsFloat() - right.getValueAsFloat() ) ;
         }else if( ( left instanceof NumberValue ) && ( left instanceof NumberValue ) ){
            return new NumberValue( left.getValueAsNumber() - right.getValueAsNumber() ) ;
         }else{
            throw new
            IllegalArgumentException("Operation "+op+" not supported for "+left);
         }
      }else if( op.equals("*") ){
         if( ( left instanceof FloatValue ) || (right instanceof FloatValue ) ){
            return new FloatValue( left.getValueAsFloat() * right.getValueAsFloat() ) ;
         }else if( ( left instanceof NumberValue ) && ( left instanceof NumberValue ) ){
            return new NumberValue( left.getValueAsNumber() * right.getValueAsNumber() ) ;
         }else{
            throw new
            IllegalArgumentException("Operation "+op+" not supported for "+left);
         }
      }else if( op.equals("/") ){
         if( ( left instanceof FloatValue ) || (right instanceof FloatValue ) ){
            return new FloatValue( left.getValueAsFloat() / right.getValueAsFloat() ) ;
         }else if( ( left instanceof NumberValue ) && ( left instanceof NumberValue ) ){
            return new NumberValue( left.getValueAsNumber() / right.getValueAsNumber() ) ;
         }else{
            throw new
            IllegalArgumentException("Operation "+op+" not supported for "+left);
         }
      }else{
         throw new
         IllegalArgumentException("Operation "+op+" not supported.");

      }
   }
   private RosiConstant executeSection( RosiSection section )
      throws RosiRuntimeException {
 
      FunctionContext context = __functionContextStack.lastElement() ;

      context.pushSection() ;

      RosiConstant result =  executeSectionContent( section ) ;

      context.popSection() ;

      return result;
      
   }
   private RosiConstant executeSectionContent( RosiSection section )
      throws RosiRuntimeException
   {

       RosiConstant returnValue = new BooleanValue(true) ;

       for( RosiValue cursor : section.statements() ){

          if( cursor instanceof RosiAssigment ){

             RosiAssigment assignment = (RosiAssigment)cursor ;

             VariableValue variableValue = assignment.getVariableNameValue() ;

             String variableName = variableValue.getVariableName() ;

             RosiValue v = __registers.get( variableValue , false ) ; /* allow 'null' */
             //
             // v can be 'null', which is not a problem
             //
             if( v instanceof RosiDevice ){

                 if( variableValue.isArray() )
                    throw new
                    RosiRuntimeException("Name conflict 'array' <-> Device : "+variableName,getContextName());
                 
                 if( v instanceof RosiActorDevice ){
                    RosiActorDevice actor = (RosiActorDevice)v ;
                    actor.setValue( calculateExpression( assignment.getRightSide() ) ) ;
                 }else if( v instanceof RosiSensorDevice ){
                    throw new
                    RosiRuntimeException(
                        "Can't assign values to sensors : "+variableName,getContextName());
                 }else if( v instanceof RosiMonoflopDevice ){
                    throw new
                    RosiRuntimeException("Can't assign values to monoflop : "+variableName,getContextName());
                 }
             }else{  /* including 'null' */

                __registers.put( variableValue , 
                                 calculateExpression( assignment.getRightSide() )
                               ) ;
             }

          }else if( cursor instanceof RosiFunctionCall ){

             executeFunctionCall( (RosiFunctionCall) cursor , true );

          }else if( cursor instanceof RosiReturnStatement ){

             RosiReturnStatement returnStatement = (RosiReturnStatement) cursor ;

             returnValue = calculateExpression( returnStatement.getExpression() ) ;

          }else if( cursor instanceof RosiSection ){

             executeSection( (RosiSection)cursor );

          }else if( cursor instanceof Rosi2Conditional ){

             Rosi2Conditional cond = (Rosi2Conditional) cursor ;

             for( Map.Entry<RosiValue,RosiSection> e : cond.getList() ){

                 RosiValue expr = calculateExpression( e.getKey() ) ;
                 if( expr.getValueAsBoolean() ){
                     executeSection( e.getValue() ) ;
                     break ;
                 }
             }

          }
       }

       return returnValue ;
   }
   /**
     *  Copies the devies and the fullTimePatches to
     *  the register and code strucure.
     */
   private void collectItems( RosiProgram program )
      throws RosiRuntimeException {
      /*
       * collect the devices, and store in registers
       */
      for( RosiDevice device : program.devices() ){

         __registers.put( new VariableValue( device.getDeviceName() ) , device ) ;

         activateDevice( device ) ;

      }
      /*
       * collect fullTimePatches
       */
      for( RosiFunction function : program.functions() ){

         __code.addFunction( function ) ;

         recursiveSectionScan( function.getSection() ) ;
      }
   }
   private void recursiveSectionScan( RosiSection section ){

         for( RosiValue statement : section.statements() ){ 

             if( statement instanceof RosiAssigment ){

	         RosiAssigment assigment = (RosiAssigment)statement ;

	         RosiValue right = assigment.getAssigment() ;

                 if( right instanceof FullTimePatch ){

	            String variableName = assigment.getVariableName() ;

                    __code.addTimePatch( (FullTimePatch)right ) ;	    

                 }

	     }else if( statement instanceof RosiSection ){

                 recursiveSectionScan( (RosiSection)statement ) ;

	     }else if( statement instanceof Rosi2Conditional ){

                 Rosi2Conditional conditional = (Rosi2Conditional)statement ;

                 for( Map.Entry<RosiValue,RosiSection> e : conditional.getList() ){

                      recursiveSectionScan( e.getValue() ) ;

                 }
 /*
                 recursiveSectionScan( conditional.getIfTrue() ) ;

                 RosiSection s = conditional.getIfFalse() ;

                 if( s != null )recursiveSectionScan( s ) ;
*/

             }
         }
   }
   private void activateDevice( RosiDevice device )
      throws RosiRuntimeException {

       if( device instanceof RosiMonoflopDevice ){
	 
           RosiMonoflopDevice monoflop = (RosiMonoflopDevice) device ;
	     
           String targetName = monoflop.getTargetName();
	     
           RosiValue target = __registers.get( targetName ) ;
	     
           if( target == null )
	         throw new
		 IllegalArgumentException( "Observer : "+
		                           monoflop.getDeviceName()+
					   " can't find it's target : "+targetName);
	     
           if( ! (target instanceof RosiSensorDevice ) )
             throw new
             IllegalArgumentException( "Observer : "+
                             monoflop.getDeviceName()+
                             " : Not a sensor name "+targetName);
	     
             ((RosiSensorDevice)target).addObserver( monoflop ) ;
	     
        } 
	 
   }
   public void showVariables(){
      System.out.println( __globalRegisters.toString() ) ;
   }
   public static class MyFilterStream extends FilterOutputStream {

      public MyFilterStream( OutputStream stream ){
          super(stream);
      }
      public void write( byte [] buffer ) throws IOException{
         System.out.println("!!! write(byte[]buffer) = "+buffer.length);
         super.write( buffer ) ;
      }
      public void write( byte [] buffer , int offset , int len  ) throws IOException {
         System.out.println("!!! write(byte[]buffer,"+offset+","+len+") = "+buffer.length);
         super.write( buffer , offset , len ) ;
      }
      public void write( int buffer ) throws IOException{
         System.out.println("!!! write(int)");
         super.write( buffer ) ;
      }
   }
   public static String loadRosiFile( File file ) throws IOException {

       StringBuffer   sb     = new StringBuffer() ;
       String         input  = null ;

       BufferedReader reader = new BufferedReader( new FileReader( file ) ) ;

       while( ( input = reader.readLine() ) != null ){
         sb.append( input ).append("\n");
       }
       return sb.toString();
   }
   /*
    *     The Main Test
    * -----------------------------------------------------------------------------
    *
    */
   public static void main( String [] args ) throws Exception {
   
      if( args.length < 1 ){
            System.err.println("Usage : ... <RosiLanguageFile> [<> [...<>]]");
            //System.err.println("Usage : ... <RosiLanguageFile> [yyyy/mm/dd-hh:mm[");
            System.exit(4);
       }
       
       String filename = args[0] ;
       
       //String timeString = args.length > 1 ? args[1] : null ;
       String timeString =  null ;

       System.out.println("Creating Compiler");
       Rosi2Compiler compiler = null ;
       try{
         compiler = new Rosi2Compiler() ;
       }catch(Exception ce ){
         System.out.println("Creating compiler failed : "+ce);
         ce.printStackTrace();
       }

      try{

         //RosiProgram program = null ;

         Rosi2ExecutionEngine execution = new Rosi2ExecutionEngine() ;

         ProgramRegister registers = execution.getRegisters() ;
         ProgramCode     code      = execution.getCode() ;

         for( int i = 0 ; i < args.length ; i++ ){

            System.out.println(" ---------  Processing Rosi File : "+args[i]);

            File file = new File( args[i] ) ;
       
            if( ! file.exists() ){
                System.err.println("File not found : "+file);
                System.exit(4);
            } 

            RosiData compiledTree  = compiler.compile( loadRosiFile(file) ) ;

            Rosi2Loader loader     = new Rosi2Loader(compiledTree) ;

            RosiProgram program    = loader.load() ;

//            System.out.println( program.toString() ) ;

            execution.addModule( program ) ;
   
            execution.execute() ;
        
         }
/*
         execution.setPrintStreams( new PrintStream(new MyFilterStream(System.out)) ,
                                    new PrintStream(new MyFilterStream(System.err)) );
*/
 

//	 System.out.println( code.toString() ) ;

	 try{

            //if( ! program.checkFunctions() );

	    if( timeString != null )code.setTime( new RosiCalendar( timeString ) ) ;

	    execution.execute() ;
	    
	 }catch(RosiRuntimeException rre ){
            System.err.println(rre.getMessage());
	 }catch(Exception ee ){

            System.out.println("Execution error : "+ee.getMessage());
            //ee.printStackTrace();
	    throw ee ;
	 }

         System.out.println("----------------------------");
         System.out.println( registers.toShortString());

      }catch(Exception  eea ){
         System.out.println("Error: "+eea.getMessage());
         eea.printStackTrace();
         System.exit(1);
      }
      
      System.exit(0);

   }
   
   
}
