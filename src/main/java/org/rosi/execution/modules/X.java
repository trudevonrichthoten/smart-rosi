import java.io.*;

public class X {

   public static void main( String [] args ) throws Exception {

     String flagDirectoryName = args[0] ;
     File _file = new File( flagDirectoryName ) ;

      if( ( ! _file.isDirectory()               ) ||
          ( ! _file.canWrite()  )    )
         throw new
         IllegalArgumentException( "Not a dir. or can't write to : "+_file ) ;

   }
}


