package org.rosi.execution.modules ;

import java.util.* ;
import java.io.* ;
import java.util.concurrent.* ;

import org.rosi.util.*;
import org.rosi.execution.*;
import org.rosi.compiler.*;
import org.rosi.nodes.*;

public class ExecutionConsumerModule extends RosiModule {

   private Rosi2Compiler        _compiler  = new Rosi2Compiler() ;
   private Rosi2ExecutionEngine _execution = null ;
   private ProgramRegister      _register  = null ;
   private ProgramCode          _code      = null ;
   private ModuleLoader         _moduleLoader = null ;
   private PrintStream          _rosiOutput   = System.out ;
   private PrintStream          _rosiError    = System.err ;

   private long                 _suspendTime = 10000L;
   private boolean              _resetActors = false ;

   public ExecutionConsumerModule(
             String moduleName , 
             ModuleContext context ) throws Exception {

       super(moduleName,context);

       log("Initiating.");
       /*
        * The main program module.
        * .......................
        */
       String programFileName = getContext("program");
       if( programFileName == null )
          throw new
          RosiRuntimeException("Error : 'program' key not set in context",getName());

       log("Progam Name : "+programFileName);
        /*
         * I rosi input and ouput.
         * .......................
         */
       String rosiOutputString = _context.get("rosiOutput" );
       if( rosiOutputString != null ){
          File f = new File( rosiOutputString ) ;
          _rosiOutput = new PrintStream( new FileOutputStream( f , true ) ) ;
       }
       rosiOutputString = _context.get("rosiError" );
       if( rosiOutputString != null ){
          File f = new File( rosiOutputString ) ;
          _rosiError = new PrintStream( new FileOutputStream( f , true ) ) ;
       }
       /*
        * Define the window, within we don't trigger, even if the 
        * Sensor is defined 'trigger'. (currently ignored)
        * .......................
        */
       String catchTime = _context.get("catchTime") ;
       if( catchTime  != null ){
          log("New Catch Time : "+catchTime );
          try{
              _suspendTime = Integer.parseInt(catchTime) * 1000L ;
          }catch(Exception ee ){
             errorLog("Couldn't convert catchTime to 'integer'"+catchTime ) ;
          }
        }
        log("Using catch time : "+_suspendTime+" ms");
        /*
         * Initial actor reset. 
         * .......................
         */
       String resetActorString = _context.get("resetActors");
       _resetActors = ! ( ( resetActorString == null ) || resetActorString.equals("false") ) ;

        File programFile = new File( programFileName ) ;
        if( ! programFile.exists() )
          throw new
          RosiRuntimeException("Error : Program file not found : "+programFileName,getName() );

       try{

          prepareCompilation( loadRosiProgram( programFile ) ) ;          

       }catch(RosiRuntimeException rre ){
          System.out.println("Error in preparation : "+rre.getMessage()); 
          throw rre;
       }catch(Exception eee ){
          errorLog("Couldn't initiate ExecutionEngine : "+eee); 
          if( isDebugMode() )eee.printStackTrace() ;
          throw eee ;
       }       
        /*
         * The module directory. 
         * .......................
         */
       String moduleDirString = _context.get("moduleDirectory" );
       if( moduleDirString != null ){
           File moduleDir = new File( moduleDirString ) ;
           _moduleLoader = new ModuleLoader( moduleDir , 10000L ) ;
           log("Loading module.d for the first time");
           _moduleLoader.loadOnce() ;
           log("Finished loading of 'module.d'");
       }else{
           log("Module Loader directory (module.d) not specified,");
       }
   } 
   private void processModuleFile( File file ) throws Exception {

       RosiData compiledTree  = _compiler.compile( loadRosiProgram(file) ) ;

       Rosi2Loader loader     = new Rosi2Loader(compiledTree) ;

       RosiProgram program    = loader.load() ;
       if( program.functions().size() == 0 )
         throw new
         RosiRuntimeException("No function found in file : "+file.getName() ) ;

       debug( "-------------- "+file.getName()+" ------------------------" ) ;
       debug( program.toString() ) ;
       debug( "----------------------------------------------------------" ) ;

       synchronized( _execution ){
           _execution.addModule( program ) ;
       }

   }
   private class ModuleLoader extends Thread {
      private File _dir       = null ;
      private long _sleepTime = 10000L; 
      private ModuleLoader( File moduleDirectory , long sleepTime ){
        _dir = moduleDirectory;
        _sleepTime = sleepTime ;
      }
      public void loadOnce(){
          checkAndLoadModuleDirectory( _dir ) ;
      }
      public void run(){
        log("ModuleLoader started");
        try{
            while( ! Thread.interrupted() ){
               checkAndLoadModuleDirectory( _dir ) ;
               Thread.sleep(_sleepTime);
            }
        }catch(InterruptedException ie ){
          errorLog("checkAndLoadModuleDirectory was interrupted");
        }
        log("ModuleLoader finished");
 
      }
   }
   private Map<String,Long> _moduleFileMap = new HashMap<String,Long>() ;
   private void checkAndLoadModuleDirectory( File moduleDirectory ) {
 
       if( moduleDirectory.exists() && moduleDirectory.isDirectory() ){

           File [] files = moduleDirectory.listFiles() ;
           for( int i= 0 ; i < files.length ; i++ ){
              File f = files[i] ;
              String fileName = f.getName()  ;
              if( ( !  f.exists() ) || ( ! fileName.endsWith(".rosi") ) )continue ;
              Long lastAccess = _moduleFileMap.get( fileName ) ;
              if( ( lastAccess == null ) || ( lastAccess.longValue() != f.lastModified() ) ){
    
                   try{
                      log("Loading : "+fileName);
                      processModuleFile( f ) ;
                   }catch(Exception ee ){
                      errorLog("Problem in processing module : "+fileName);
                      if( isDebugMode() )ee.printStackTrace();
                   } 
                   _moduleFileMap.put( fileName , Long.valueOf( f.lastModified() ) ) ; 
              }
           }
       } 

   }
   private String loadRosiProgram( File file ) 
     throws RosiRuntimeException, IOException{


       BufferedReader reader = new BufferedReader( new FileReader( file ) ) ;
       StringBuffer   sb     = new StringBuffer() ;
       String         input  = null ;
       try{
          while( ( input = reader.readLine() ) != null ){
           sb.append( input ).append("\n");
          }
       }catch(IOException ee ){
          throw ee ;
       }finally{
          try{ reader.close() ; }catch(IOException eeee ){}
       }

       return sb.toString();
   }
   private void prepareCompilation( String compilerInput ) 
       throws RosiRuntimeException, Exception{

       long baseTime = System.currentTimeMillis() ;

       RosiData compiledTree  = _compiler.compile( compilerInput ) ;

       debug( compiledTree.toString() );
       log("Compiler took : "+(System.currentTimeMillis() - baseTime )+" millis" ) ;
       baseTime = System.currentTimeMillis() ;

       Rosi2Loader loader     = new Rosi2Loader(compiledTree) ;
       RosiProgram program    = loader.load() ;

       program.checkFunctions() ;

       log("Loader took : "+(System.currentTimeMillis() - baseTime )+" millis" ) ;
       baseTime = System.currentTimeMillis() ;

       log( program.toString() ) ;

       baseTime = System.currentTimeMillis() ;

       _execution = new Rosi2ExecutionEngine( program ) ;
       _execution.setPrintStreams( getRosiPrintStream() , getRosiPrintStream() /*  _rosiOutput , _rosiError */);
       _register  = _execution.getRegisters() ;
       _code      = _execution.getCode() ;

       log("Creating execution engine took : "+(System.currentTimeMillis() - baseTime )+" millis" ) ;
   
       return ;
   }
   private void prepareExecutionEngine() throws Exception{

       long baseTime = System.currentTimeMillis() ;
       // done later ; _execution.prepareExecution();
       log("Preparing execution engine took : "+(System.currentTimeMillis() - baseTime )+" millis" ) ;

       if( isDebugMode() )dumpRegister();

       baseTime = System.currentTimeMillis() ;

       if( _resetActors ){
           log("Running Actors");
           runAndClearActors() ;
       } 

       synchronized( _execution ){ _execution.executeFirst() ; }

       log("Running execution first time took : "+(System.currentTimeMillis() - baseTime )+" millis" ) ;
       if( isDebugMode() )dumpRegister();
       baseTime = System.currentTimeMillis() ;

       log("Running Actors again");
       runAndClearActors() ;

   }
   public void run(){

       log("Starting with debug : "+isDebugMode());

       try{

          log("Suspending start by 4 seconds.") ;

          Thread.sleep(4000);

          log("Starting now.") ;

          prepareExecutionEngine();

          log("prepareExecutionEngine finished");

       }catch(Exception ieee ){

          errorLog( ieee.getMessage() ) ;
          errorLog( "Aborted");
          return ;

       }
       if( _moduleLoader != null )_moduleLoader.start();
       try{

          int  triggerCount = 0 ;
          long timeStamp    = 0L ;
          List<String> callMain = new ArrayList<String>();
          callMain.add("main");

          while(true){

             try{

                RosiCommand command  = take() ;

                List<String> triggerCommands = processCommand( command ) ;
                if( triggerCommands.size() > 0 )triggerCount++ ;

                if( triggerCount == 1 ) {
                   timeStamp = System.currentTimeMillis() ;
                   triggerCount ++ ;
                }

                long timeDiff = System.currentTimeMillis() - timeStamp ;

                if( ( triggerCount > 0 ) && ( mightBlock() || ( timeDiff > _suspendTime ) ) ){ 
 
                    debug("Executing 'engine' after TC="+triggerCount+";MB="+mightBlock()+";t="+timeDiff);

                    execute( callMain ) ;

                    triggerCount = 0 ;

                }
             }catch(RosiRuntimeException rre ){
                errorLog("Error: "+rre.getMessage());
             }catch(Exception eee ){
                if( eee instanceof InterruptedException )throw (InterruptedException)eee ;
                errorLog(eee.getMessage() ) ;
             }
          }
      }catch(InterruptedException ieee ){
          errorLog("Interrupted: "+ieee.getMessage() ) ;
      }
   }
   private List<String> processCommand( RosiCommand command ) throws Exception {
 
       List<String> array = new ArrayList<String>() ;
       if( command instanceof RosiTimerCommand ){

           debug("Timer  '"+command.getSource()+"' -> '"+getName()+"' cmd="+command ) ;

           RosiTimerCommand timeCommand = (RosiTimerCommand)command ;

           _code.setTime( timeCommand.getCalendar() ) ;

           array.add("main");

       }else if( command instanceof RosiSetterCommand ){

           debug("Setter '"+command.getSource()+"' -> '"+getName()+"' cmd="+command ) ;

           RosiSetterCommand setter = (RosiSetterCommand)command ;

           return _register.setSensorValue( setter.getKey() , setter.getValue() ) ;

       }else{

           log("Unkown '"+command.getSource()+"' -> '"+getName()+"' cmd="+command ) ;
                
       }
       return array;

   }
   private void execute( List<String> functionList ) throws Exception {

       synchronized( _execution ){_execution.execute( functionList ) ;}

       if( isDebugMode() )dumpRegister();

       runAndClearActors() ;
   }
   private void execute() throws Exception {

       synchronized( _execution ){ _execution.execute() ; }

       if( isDebugMode() )dumpRegister();

       runAndClearActors() ;
   }
   private void runAndClearActors(){

       runActors() ;

       _register.clearActors() ;
       _register.clearMonoflops() ;
   }
   private void executeTimerCommand( RosiTimerCommand command )throws Exception {

       _code.setTime( command.getCalendar() ) ;

       execute() ;

   }
   private void executeSetterCommand( RosiSetterCommand command )throws Exception {

        _register.setSensorValue( command.getKey() , command.getValue() ) ;

        boolean shouldTrigger = _register.shouldTrigger( command.getKey() ) ;

        if( shouldTrigger )execute();

   }
   private void runActors(){

     if( isDebugMode() ){

        List<RosiActorDevice> actors2 = _register.getActors() ;

        log("-- Current Status of Actors --");

        for( RosiActorDevice actor : actors2 ){
           debug("  "+actor.getDeviceName()+" : "+actor.getValue()+" "+actor.wasChanged());
        }
        log("------------------------------");

      }

      List<RosiActorDevice> actors = _register.getChangedActors() ;

      for( RosiActorDevice actor : actors ){
         RosiCommand command = new RosiSetterCommand( actor.getDeviceName() , actor.getValue().getValueAsString() ) ;
         try{
            put(command); 
         }catch(Exception ie){
            errorLog("Warning : Error while sending command : "+ie.getMessage());
         }
      }
   }
   private void dumpRegister(){
 
      debug( "-- Register Dump --------------\n" + _register.toShortString() ); 
      debug("--------------------------------");
            
   } 
   public void setCommandProcessor( RosiCommandProcessor commandProcessor ){ }
}
