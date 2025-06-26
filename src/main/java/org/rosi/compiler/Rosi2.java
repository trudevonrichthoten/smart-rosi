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

/*
 * Expression <-  Term    ( "+ | -"   Term   )*
 * Term       <-  Factor  ( "* | /"   Factor ) *
 */
@BuildParseTree 
class Rosi2 extends BaseParser<RosiData> {
    
/*
 *    THE GRAMMER
 * ----------------------------------------------------------------
 */
    Rule Program(){
       return 

       Sequence(

          Spacing() ,

          push( new RosiData( "Program" ) ) ,

          DeviceDefinitionList() ,

          swap() &&   push( pop().addLinear( pop() ) ), 

          ZeroOrMore(

             Spacing() ,

             Function() , 
	      
             swap() &&   push( pop().addLinear( pop() ) ) 
          )

	) ;
    }
    Rule DeviceDefinitionList(){
      return
      Sequence(

          push( new RosiData( "DeviceDefinitionList" ) ) ,

          ZeroOrMore(

              Spacing() ,

              DeviceDefinition() ,

              swap() &&   push( pop().addLinear( pop() ) ) ,

              WhiteSpace() ,

              Terminal(";")

          )
      );
    }

    Rule Function(){
       return 
       Sequence(

          Variable() ,

          WhiteSpace() ,

          //Terminal("(") , 
          "(" ,

          WhiteSpace() ,

          FormalArgumentList() , 

	  swap() &&   push( new RosiData( "Function" , pop() , pop() ) ) ,

//          Terminal(")") ,
          ")" ,

          Section()  , 

	  swap() &&   push( pop().addLinear( pop() ) )  
       ) ;
    }
    Rule Section(){ 
      return 
      Sequence(

          Terminal("{") ,

          push( new RosiData( "Section" ) ) ,
          ZeroOrMore( 
              Spacing() ,
	  
              Command() , 
	      swap() &&   push( pop().addLinear( pop() ) ) ,

              WhiteSpace() , 

              Terminal(";")

	      	      
	  ),
          Terminal("}")  
      );
    }
    
    Rule Command(){
       return

           FirstOf(
               
               Section() , 
               Conditional() ,
               Assigment() ,
               FunctionCall()  ,
               ReturnStatement() 
/*
               Sequence( Assigment() , WhiteSpace() , Terminal(";") ) ,
               Sequence( FunctionCall() , WhiteSpace() , Terminal(";") ) ,
               Sequence( ReturnStatement() ,WhiteSpace() , Terminal(";") )  
*/
		       
          ) ;
    }
    Rule ReturnStatement(){
       return 

        Sequence(
             Terminal("return"), 
             push( new RosiData("Return") ) ,
             Optional(
                LogicalExpression(), 
                swap() && push( pop().addLinear( pop() ) )  
             )
        ) ;

    }
/*
 *    THE COMMANDS
 * ----------------------------------------------------------------
 */
    Rule ValueDefinition(){
        return Sequence(
    
           Variable() ,
	   WhiteSpace(),
	   Terminal(":=" ) ,
           WhiteSpace() , 
	   Listelement() ,
	   
	   swap() && push( new  RosiData("ValueDefinition" , pop() , pop() ) ) 
	
	);
	
    }
    Rule Conditional(){
        return Sequence(
    
           LogicalExpression() ,

           push( new RosiData( "Conditional" ) ) && push( pop().addLinear( pop() ) ), 

	   WhiteSpace(),

	   Terminal(":" ) ,

           Section() ,

           swap() && push( pop().addLinear( pop() ) ) ,

          // swap() && push( new RosiData( "Conditional" , pop() , null ) ) &&  push( pop().addLinear( pop() ) )  ,

           ZeroOrMore(

                LogicalExpression() ,

                swap() && push( pop().addLinear( pop() ) )  ,
/*
                push( pop().addLinear( new RosiData( "Boolean" , "false" ) ) ),
*/ 
	        WhiteSpace(),

	        Terminal(":" ) ,

                Section()  ,

                swap() && push( pop().addLinear( pop() ) )
   
           ) ,

           Optional(

               WhiteSpace() ,
/*
               Terminal( "else" )  ,
               push( pop().addLinear( new RosiData( "Boolean" , "false" ) ) ),
*/
               push( pop().addLinear( new RosiData( "Boolean" , "true" ) ) ),
               Section() ,

               swap() && push( pop().addLinear( pop() ) )
         

           )

	);
	
    }
    Rule Assigment(){
    
       return Sequence(
       
          Variable(), 
	   
	  WhiteSpace(), 
	  
	  Terminal("=") , 
	 
          FirstOf(

              LogicalExpression() ,
              FullTimePatch() 

          ),

          swap() && push( new RosiData( "Assigment" , pop() , pop() ) )

      );
    }
    Rule TimePatchList(){
        return 
          Sequence( 
              TimePatch() ,
              push( new RosiData( "TimePatchList" , pop() ) ) ,  
              ZeroOrMore(
                WhiteSpace() ,
                Terminal(",") ,
                TimePatch() ,
                swap() && push( pop().addLinear( pop() ) )
              )
          ) ;
    }
    Rule DatePatchList(){
        return 
          Sequence( 
              FirstOf(
                 DatePatch() ,
                 DayOfWeek() 
              ),
              push( new RosiData( "DatePatchList" , pop() ) ) ,  
              ZeroOrMore(
                WhiteSpace() ,
                Terminal(",") ,
                FirstOf(
                   DatePatch() ,
                   DayOfWeek() 
                ),
                swap() && push( pop().addLinear( pop() ) )
              )
          ) ;
    }
    Rule FullTimePatch(){
      return 
       Sequence(
           TimePatchList() ,
           WhiteSpace() ,
           Terminal("ON") ,
           DatePatchList() ,
           swap() && push( new RosiData( "FullTimePatch" , pop() , pop() ) ) ,
           WhiteSpace() 
       );
    }
    Rule RegularList(){
    
       return Sequence(
       
 	  LogicalExpression() ,
	  
	  push( new RosiData( "RegularList" , pop() ) ) ,
	  
	  ZeroOrMore( 
	     
	     Sequence(  
	                WhiteSpace() , 
	                ',' , 
			WhiteSpace() ,
			
	                LogicalExpression() ,
			
			swap() && push( pop().addLinear( pop() ) )
	     ) 
	  
	  ) 
       ) ;
    }
    Rule DeviceDefinition(){

       return Sequence(
       
          Terminal("define") , 
	  	  
	  FirstOf( 
              SensorDevice() ,
              ActorDevice() ,
              MonoflopDevice() ,
              EventDevice() , 
              RandomDevice() , 
              TimerDevice()
          )
	  
       ) ;
    }
	 
    Rule RandomDevice(){
       return Sequence(
          /*
           * define random <devicename> <onMin> <onMax> <offMin> <offMax>
           */ 
          Terminal("random") ,

          Variable()  ,

	  push( new RosiData( "RandomDeviceDefinition" , pop() , new RosiData( "StringLiteral" , "dummy" ) ) ) ,

	  WhiteSpace(), Number() , swap() && push( pop().addLinear( pop() ) ) ,
	  WhiteSpace(), Number() , swap() && push( pop().addLinear( pop() ) ) ,
	  WhiteSpace(), Number() , swap() && push( pop().addLinear( pop() ) ) ,
	  WhiteSpace(), Number() , swap() && push( pop().addLinear( pop() ) )

       ) ;
    }
	  
    Rule TimerDevice(){
       return Sequence(
          /*
           * define timer <timername> <time perioud/seconds> [once|repeat] [funcCall()];
           */ 
          
          Terminal("timer") ,

          Variable()  ,

	  WhiteSpace(),
	  
          Number()  ,

	  swap() && push( new RosiData( "TimerDeviceDefinition" , pop() , pop() ) ) ,

	  WhiteSpace(),

          FirstOf(
              Terminal("once") ,
              Terminal("repeat") 
          ) ,

          push( pop().addLinear( new RosiData( "StringLiteral" , matchOrDefault("default") )  ) ) ,

	  Optional( 
	 
               FunctionCall()  ,
               swap() && push( pop().addLinear( pop() ) )
          ) 
	  
       ) ;
    }
    Rule EventDevice(){
       return Sequence(
          /*
           * define event <sensorName> <observedSensorName> [<triggerValue>]
           */ 
          
          Terminal("event") ,

          Variable()  ,

	  WhiteSpace(),
	  
          Variable()  ,

	  swap() && push( new RosiData( "MonoflopDeviceDefinition" , pop() , pop() ) ) ,

          push( pop().addLinear( new RosiData( "Number" , "0") ) ) ,

	  WhiteSpace(),
	  
	  Optional( 
    
             Variable() ,
             swap() && push( pop().addLinear( pop() ) ) ,

	     Optional( 

	       WhiteSpace(),

               LogicalTerm()  ,

               swap() && push( pop().addLinear( pop() ) )
             )
          ) 
	  
       ) ;
    }
    Rule MonoflopDevice(){
       return Sequence(
          /*
           * define sensor <sensorName> <defaultValue> [trigger|funcCall()] ;
           */ 
          
          Terminal("monoflop") ,

          Variable()  ,

	  WhiteSpace(),
	  
          Variable()  ,

	  swap() && push( new RosiData( "MonoflopDeviceDefinition" , pop() , pop() ) ) ,

	  WhiteSpace(),

	  Number() ,

          swap() && push( pop().addLinear( pop() ) ) ,

	  WhiteSpace(),
	  
	  Optional( 
    
             Variable() ,
             swap() && push( pop().addLinear( pop() ) ) ,

	     Optional( 

	       WhiteSpace(),

               LogicalTerm()  ,

               swap() && push( pop().addLinear( pop() ) )
             )
          ) 
	  
       ) ;
    }
    Rule SensorDevice(){
       return Sequence(
          /*
           * define sensor <sensorName> <defaultValue> [trigger|funcCall()] ;
           */ 
          
          Terminal("sensor") ,

          Variable()  ,

	  WhiteSpace(),
	  
	  LogicalTerm() ,

	  swap() && push( new RosiData( "SensorDeviceDefinition" , pop() , pop() ) ) ,
	  
	  WhiteSpace() ,

	  Optional( 
             Variable() , 
	     swap() && push( pop().addLinear( pop() ) ) ,
             Optional(
	        WhiteSpace(),
                FunctionCall() ,
	        swap() && push( pop().addLinear( pop() ) )
             )
          )
	 /* 
	  Optional( 
	 
             FirstOf( 
	         FunctionCall() ,
	         Variable() 
             ) ,
	     swap() && push( pop().addLinear( pop() ) )
	      
	  )
          */ 
	  
       ) ;
    }
    Rule ActorDevice(){
       return Sequence(
          /*
           * define actor <sensorName> <defaultValue> ;
           */ 
          Terminal("actor") ,

          Variable()  ,

	  WhiteSpace(),
	  
	  LogicalTerm() ,

	  swap() && push( new RosiData( "ActorDeviceDefinition" , pop() , pop() ) ) 
	  
       ) ;
    }
/*
 *    THE COMMANDS
 * ----------------------------------------------------------------
 */
    Rule Listelement(){
       return FirstOf(
           Variable() ,
	   Number() ,
	   Float() ,
	   TimePatch() ,
	   LogicalExpression()
       );
    }
/*
 * ----------------------------------------------------------------
 */
    Rule LogicalExpression(){

        return Sequence(

	    WhiteSpace() , 

            LogicalTerm(),   

	    ZeroOrMore( 

                 WhiteSpace() , 
 
                 LogicalRight() 

            )
        ) ;
    }
    Rule LogicalRight(){
        Var<String> op = new Var<String>() ;
        return 
           Sequence(
	        FirstOf(
		   Terminal("*" ) , 
		   Terminal("/" ) , 
		   Terminal("+" ) , 
		   Terminal("-" ) , 
		   Terminal("|" ) , 
		   Terminal(">=") , 
		   Terminal("&" ) , 
		   Terminal("==") ,
		   Terminal("!=") ,
		   Terminal("<=") ,
		   Terminal("<") ,
		   Terminal(">")
		) ,

                push( new RosiData( match() ) ) ,
              //  op.set( matchOrDefault("x") ) ,
		WhiteSpace() ,
		LogicalTerm()  ,
                swap() && push( new RosiData( pop().getLabel() , pop() , pop(), true ) ) 
                //swap() && push( new RosiData( op.get(), pop() , pop() ) ) 
           );
    }
    Rule LogicalTerm(){
       return 
           FirstOf(
             Sequence( 
                Terminal("!") ,
                EndLogicalTerm() ,
                push( new RosiData( "OP!" , pop() , null ) ) 
             ) ,
             EndLogicalTerm()
           );
    }
    Rule EndLogicalTerm(){
       return 
	  
	  FirstOf(
              FunctionCall() ,
	      Number() ,
	      Float() ,
              BooleanValue() ,
	      StringLiteral() ,
              Variable() ,
              Sequence( 
                Terminal("(" ) ,
                WhiteSpace(),
                LogicalExpression() , 
                WhiteSpace(),
                Terminal(")")
              )
	 	  
	  );
    }
/*
 * ----------------------------------------------------------------
 */
    Rule FormalArgumentList() {
      return 
        Sequence( 
           push( new RosiData( "FormalArgumentList" ) ) ,
           Optional(
              Variable() ,   
              swap() && push( pop().addLinear( pop() ) ) ,
              WhiteSpace() ,
              ZeroOrMore(
                 Terminal(",") ,
                 WhiteSpace() ,
                 Variable() ,
                 swap() && push( pop().addLinear( pop() ) ) ,
                 WhiteSpace() 
              )
           )
        ) ;
    }
    Rule ArgumentList() {
      return 
        Sequence( 
           push( new RosiData( "ArgumentList" ) ) ,
           Optional(
              LogicalExpression() ,   
              swap() && push( pop().addLinear( pop() ) ) ,
              WhiteSpace() ,
              ZeroOrMore(
                 Terminal(",") ,
                 WhiteSpace() ,
                 LogicalExpression() ,
                 swap() && push( pop().addLinear( pop() ) ) ,
                 WhiteSpace() 
              )
           )
        ) ;
    }
    Rule FunctionCall(){
       return 
         Sequence(
            Variable() ,
            WhiteSpace() ,
            Terminal("(") ,
            WhiteSpace() ,
            ArgumentList() ,
            swap() && push( new RosiData("FunctionCall" , pop() , pop() ) ) ,
            Terminal(")") 
         ) ;
    }
    Rule BooleanValue(){
       return 
        Sequence(
          FirstOf( Terminal("true"), Terminal("false") ) ,
          push( new RosiData( "Boolean" , matchOrDefault("default")  ) ) 
        ) ;
    } 
    Rule DeviceName(){
    
      return 
        Sequence(
	  FirstOf(
            Terminal("sensor" ) ,
	    Terminal("actor" ),
	    Terminal("trigger" ),
	    Terminal("random" ),
	    Terminal("monoflop" ) 
	  ) ,
	  push( new RosiData( "Device" , matchOrDefault("default")  ) )
	
       );
    }
    Rule Statement() {
        return Sequence(
            Variable(),   "=" , Expression()  , ";" 
        );
    }
    
    Rule Expression() {
        return Sequence(
            Term(),   ZeroOrMore( AnyOf("+-") , Term()  )
        );
    }
    Rule Term() {
        return Sequence(
            Factor(),  ZeroOrMore(  AnyOf("*/"), Factor()  )
        );
    }
    
    Rule Factor() {
        return FirstOf(
	
	    TimePatch() ,
	    Brackets() ,
	    Variable() ,  
            Number()
	    
        );
    }
    Rule Brackets() {
        return Sequence(
	    '('  , Expression() , ')'
	) ;
    }
/*
 *     The Terminal Values
 */
    Rule StringLiteral() {
        return Sequence(
                '"',
                ZeroOrMore(
                    Sequence(TestNot(AnyOf("\r\n\"\\")), ANY)
                ).suppressNode() ,
		push( new RosiData( "StringLiteral" , matchOrDefault("default")  ) ) ,
                '"'
        );
    }
    Rule TimePatch() {
        return 
	  Sequence(
	    Sequence(
                "[" , TimeValue() , "-" , TimeValue() , "]" 
            ) ,
	    swap() && push( new RosiData( "TimePatch" ,  pop() , pop() ) )
          );
    }
    Rule TimeValue() {
       return Sequence(
         Sequence(
	    CharRange('0' , '9' ) ,
	    CharRange('0' , '9' ) ,
	    ":" ,
	    CharRange('0' , '9' ) ,
	    CharRange('0' , '9' ) 
	   
          ).suppressNode() ,
	 push( new RosiData( "TimeValue" , matchOrDefault("default")  ) )
      ) ;
    }
     Rule DatePatch(){
       return Sequence(
       
              "[" ,

	      DateValue() ,

	      Optional(
        	 "-" , 
		 
		 DateValue() , 
		 
		 swap() && push( new RosiData("DatePatch" , pop() , pop() ) ) 
	       ),

	      "]"
	   
       ) ;
    }
    Rule DateValue(){
      return Sequence(
      
          Sequence(
      
 	      OneOrMore( CharRange('0' , '9' )  ).suppressNode() ,
	 
	      "/" ,
	      
	      OneOrMore( CharRange('0' , '9' )  ).suppressNode() ,
	      
	      Optional(
	      
	         "/" ,
	         OneOrMore( CharRange('0' , '9' )  )
		 
	      ).suppressNode() 
	   ) ,
	   push( new RosiData( "DateValue" , matchOrDefault("default")  ) )
       ) ;
    }
    Rule DayOfWeek(){
      return Sequence(
           FirstOf(
              Terminal("Monday" ),
              Terminal("Wednesday" ),
              Terminal("Tuesday" ),
              Terminal("Thursday" ),
              Terminal("Friday" ),
              Terminal("Saturday" ),
              Terminal("Sunday" )
           ),
           push( new RosiData( "DayOfWeek" , matchOrDefault("default")  ) )
      ) ;
    }
    Rule Variable() {
        return Sequence(
           Sequence(
	    FirstOf( CharRange('a', 'z') , CharRange('A', 'Z') ).suppressNode() ,
	    ZeroOrMore( 
	       FirstOf( 
	          CharRange('a', 'z') , 
	          CharRange('A', 'Z') , 
	          CharRange('0' , '9' ) ,
		  AnyOf( "._" ) 
	       ) 
	     ).suppressNode() 
           ),
	     push( new RosiData( "Variable" , matchOrDefault("default")  ) ) ,
             Optional(
                Terminal("[") ,
                LogicalExpression() ,
                Terminal("]") ,
                swap() &&  push( pop().addLinear( pop() ) ) 
             ) 
	 ) ;
    }
    Rule Number() {
       return  
         Sequence(  
	   Sequence( 
	   
              OneOrMore(CharRange('0', '9') ),
	      TestNot( '.' )	     
	      
	   )  ,
	   
	   push( new RosiData( "Number" , matchOrDefault("default")  ) )
	   
	 ) ;
   }
   
    Rule Float() {
       return  
        Sequence( 
         FirstOf( 
	     Sequence( "-" ,
                       OneOrMore(CharRange('0', '9')),
	               '.' ,
                       ZeroOrMore(CharRange('0', '9'))
		      ) ,
	     Sequence( 
                       OneOrMore(CharRange('0', '9')),
	               '.' ,
                       ZeroOrMore(CharRange('0', '9'))
		      ) 
		      
             
        ) , 
        push( new RosiData( "Float" , matchOrDefault("default")  ) )
       );
   }
/*
 *     The Terminal Values
 */
    @SuppressNode
     Rule WhiteSpace(){
        return ZeroOrMore( AnyOf( " \t\f\n" ) ) ;
    }
   //-------------------------------------------------------------------------
    //  JLS 3.6-7  Spacing
    //-------------------------------------------------------------------------

    @SuppressNode
    Rule Spacing() {
        return ZeroOrMore(FirstOf(

                // whitespace
                OneOrMore(AnyOf(" \t\r\n\f").label("Whitespace")),

                // traditional comment
                Sequence("/*", ZeroOrMore(TestNot("*/"), ANY), "*/")

        ));
    }


    //-------------------------------------------------------------------------
    //  helper methods
    //-------------------------------------------------------------------------

//    @Override
//    protected Rule fromCharLiteral(char c) {
        // turn of creation of parse tree nodes for single characters
//        return super.fromCharLiteral(c).suppressNode();
//    }

//    @SuppressNode
//    @DontLabel
    Rule Terminal(String string) {
        return Sequence(string, Spacing()).label('\'' + string + '\'');
    }

    @SuppressNode
    @DontLabel
    Rule Terminal(String string, Rule mustNotFollow) {
        return Sequence(string, TestNot(mustNotFollow), Spacing()).label('\'' + string + '\'');
    }


}
