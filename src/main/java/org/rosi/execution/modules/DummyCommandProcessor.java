package org.rosi.execution.modules ;

import java.util.* ;
import org.rosi.execution.*;
import org.rosi.util.*;

public class DummyCommandProcessor implements RosiCommandProcessor {
   private ModuleContext  _context = null ;
   public DummyCommandProcessor( ModuleContext context ){
      _context = context ;
   } 
   public RosiCommand process( String command ){
       if( command.equals("dummy") )return null ;
       return new RosiCommand( "PROCESSED: "+command);
   }
}
