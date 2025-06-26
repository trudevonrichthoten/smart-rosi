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
public class Rosi2Compiler {

   private Rule _rule = null ;
   private ReportingParseRunner<RosiData> _runner = null ;
   
   public Rosi2Compiler(){

     try{
       Rosi2 parser = Parboiled.createParser( Rosi2.class);

       _rule   = parser.Program() ;

       _runner = new ReportingParseRunner<RosiData>( _rule ) ;
     }catch(Exception ee ){
       ee.printStackTrace();
       if( ee instanceof RuntimeException ){
          Throwable eee = ((RuntimeException)ee).getCause();
          eee.printStackTrace();
       }
     }
         
   }  
   public RosiData compile( String string ) throws IllegalArgumentException {
/* --------------------------------------------------------------------------*/
   
       ParsingResult<RosiData> result = _runner.run( string );

       //System.out.println("!!!! Result : "+result.matched+" has errors : "+result.hasErrors());
       //ValueStack<RosiData> s = result.valueStack;
       //System.out.println("Value stack : "+s.size());
       //	     String parseTreePrintOut = ParseTreeUtils.printNodeTree(result);
       // 	     System.out.println(parseTreePrintOut);

       RosiData _baseTree = result.resultValue ;
       if( _baseTree == null )
         throw new
         IllegalArgumentException("Running the compilation step reported a problem.");

   //    System.out.println( _baseTree.formatString("") ) ; 

       return _baseTree ;
   }
   public static void main( String [] args ) throws Exception {
/* ------------------------------------------------------------*/
   
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
      
      RosiData program = compiler.compile( sb.toString() ) ;
      System.out.println("------------------------------------"); 
      System.out.println( program.toString() ) ;
      
      System.exit(0);

   }

}
