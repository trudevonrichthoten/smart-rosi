package org.rosi.compiler ;

import java.util.ArrayList ;
import java.util.List;
import org.parboiled.trees.ImmutableBinaryTreeNode ;

    
public  class RosiData extends ImmutableBinaryTreeNode<RosiData> {

    private String title = null ;
    private String value = null ;
    private List<RosiData>  vector = null ;

    public RosiData( String title , RosiData left , RosiData right ){
      super(left,right);
      this.title = title.trim() ;
    }
    public RosiData( String title , RosiData left , RosiData right , boolean swap ){
      super(right,left);
      this.title = title.trim() ;
    }
    public RosiData( String title ){
      super(null,null);
      this.title = title.trim() ;
    }
    public RosiData( String title , String value ){
      super(null,null);
      this.title = title.trim() ;
      this.value = title.equals("StringLiteral" ) ? value : value.trim() ;
    }
    public RosiData( String title , RosiData value ){
      super(null,null);
      this.title = title.trim() ;
      this.vector = new ArrayList<RosiData>() ;
      this.vector.add(value);
    }
    public RosiData addLinear( RosiData value ){
       if( this.vector == null )this.vector = new ArrayList<RosiData>() ;
       this.vector.add(value);
       return this ;
    }
    public String toString(){
       return formatString() ;
    }
    public RosiData getLeft(){ return left() ; }
    public RosiData getRight(){ return right() ; }
    public List<RosiData> getList(){ return this.list() ; }
    public List<RosiData> list(){ 
        if( vector == null )return new ArrayList<RosiData>() ;
        return this.vector ;
    } 
    public String getLabel(){ return this.title ; }
    public String getValue(){ return this.value ; }
    public String formatString(){
       return formatString("");
    }

    public String formatString( String gap ){

       StringBuffer sb = new StringBuffer() ;

       sb.append( gap ).append(title) ;

       if( this.value != null ){
	  sb.append(" = ").append(value) ;
       }
       sb.append("\n") ;

       if( left() != null  )sb.append(left().formatString( gap + "  L: " ) );
       if( right() != null )sb.append(right().formatString( gap +"  R: " ) );


       if( this.vector != null ){
	  //sb.append( gap ).append( "  Vector:\n" );
	  for( RosiData cursor : this.vector ){
	     sb.append( cursor.formatString( gap + "  V: " ) );
	  }
       }


       return sb.toString() ;
    }

}
