package org.rosi.nodes ;

import java.util.List ;
import java.util.Set ;
import java.util.HashSet ;
import java.util.ArrayList ;
import org.rosi.util.RosiRuntimeException;

public class RosiProgram extends RosiValue {
/*
    private List<RosiValue>     _commands    = new ArrayList<RosiValue>() ;
*/
    private List<RosiFunction>  _functions   = new ArrayList<RosiFunction>() ;
    private List<RosiDevice>    _devices     = new ArrayList<RosiDevice>() ;
    private List<FullTimePatch> _timePatches = new ArrayList<FullTimePatch>() ;
    private Set<RosiFunctionCall> _functionCalls = new HashSet<RosiFunctionCall>() ;
/*
    public void add( RosiValue command ){
       _commands.add( command ) ;
    }
*/
    public void addFunction( RosiFunction command ){
       _functions.add( command ) ;
    }
    public void addDevice( RosiDevice command ){
       _devices.add( command ) ;
    }
    public void addTimePatch( FullTimePatch timePatch ){
       _timePatches.add( timePatch ) ;
    }
    public void addFunctionCall( RosiFunctionCall funcCall ){
       _functionCalls.add( funcCall ) ;
    }
/*
    public List<RosiValue>     commands(){ return _commands ; }
*/
    public List<RosiFunction>  functions(){ return _functions ; }
    public List<RosiDevice>    devices(){ return _devices ; }
    public List<FullTimePatch> timePatches(){ return _timePatches ; }
    
    public boolean checkFunctions() throws RosiRuntimeException{
     
        boolean result = true ;

        Set<String> funcSet = new HashSet<String>();

        StringBuffer sb = new StringBuffer();
        sb.append("Duplicate Entries : ");
        for( RosiFunction func : _functions ){
           String functionName = func.getFunctionName() ;
           if( funcSet.contains(functionName) ){
              sb.append(functionName).append(",");
              result = false ;
           }else{
              funcSet.add( functionName ) ;
           }
        }
        sb.append("; Unresolved: ");
        _functionCalls.add( new RosiFunctionCall( "main" ) ) ; 
        Set<String> missingFunctions = new HashSet<String>() ;
        for( RosiFunctionCall funcCall : _functionCalls ){
           String fname = funcCall.getFunctionName() ;
           if( ( ! funcSet.contains( fname ) ) && ( ! fname.startsWith("System.") ) ){
               missingFunctions.add(funcCall.getFunctionName() ) ;
           }
        }
        for( String funcName : missingFunctions ){
          sb.append(funcName).append(",");
        }
        if( ( ! result ) || ( missingFunctions.size() > 0  ))
          throw new
          RosiRuntimeException(sb.toString(),"Resolver"); 

        return result ;
    }
    public String formatString( String gap ){

	StringBuffer sb = new StringBuffer() ;

	sb.append( gap ).append( "Program" ).append("\n") ;
/*
	sb.append( gap ).append( "*Commands" ).append("\n") ;
        for( RosiValue node : _commands ){
	   sb.append( node.formatString( gap + "   " )  ) ;
        }
*/
	sb.append( gap ).append( "*Devices" ).append("\n") ;
        for( RosiValue node : _devices ){
	   sb.append( node.formatString( gap + "   " )  ) ;
        }
	sb.append( gap ).append( "*TimePatches" ).append("\n") ;
        for( RosiValue node : _timePatches ){
	   sb.append( node.formatString( gap + "   " )  ) ;
        }
	sb.append( gap ).append( "*Functions" ).append("\n") ;
        for( RosiValue node : _functions ){
	   sb.append( node.formatString( gap + "   " )  ) ;
        }
	sb.append( gap ).append( "*FunctionCalls" ).append("\n") ;
        for( RosiValue node : _functionCalls ){
	   sb.append( node.formatString( gap + "   " )  ) ;
        }

	return sb.toString() ;
   }
   public String toString(){
      return this.formatString("");
   }
}   
