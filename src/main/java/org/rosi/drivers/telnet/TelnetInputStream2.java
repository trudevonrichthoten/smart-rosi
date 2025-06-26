package org.rosi.drivers.telnet ;

import java.net.* ;
import java.io.* ;
import java.util.* ;

public class TelnetInputStream2 extends InputStream {

   private TelnetStreamEngine _core =  null ;
   
   public TelnetInputStream2( TelnetStreamEngine core ){
      _core = core ;
   }
   public int read() throws IOException {  
      int rc = _core.read() ;
      return rc ; 
   }
   //
   // we have to overwrite the following two
   // read methods. Otherwise the call to the
   // superclass method will block until all
   // requestes byte will have arrived.
   //
   public int read( byte [] b )throws IOException { 
      return this.read( b , 0 , b.length ) ; 
   }
   public int read( byte [] b , int off , int i ) throws IOException {
       int rc ;
       if( i <= 0 )return i ;
       if( (  rc = this.read() ) < 0 )return -1 ;
       b[off] = (byte)rc ;
       return 1 ;
   }
   public void close() throws IOException {
       _core.close() ;
   }
}
