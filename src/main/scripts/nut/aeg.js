var c = new Mongo() ;
var db = c.getDB("rosi");
var count = 0 ;
var root  = 0 ;
var array = {} ;
var go    = 0 ;
function y(x){

   if( ( go == 0 ) && ( x["aeg/ups/status"] == "net" ) )return ;
   go = 1 ;

   if( count == 0 ){ root = x["timestamp"].getTime() ; }
   var t = ( x["timestamp"].getTime() - root )/1000 ;

   print( Math.round(t/60), x["aeg/ups/status"],
          x["aeg/battery/voltage"],
          x["aeg/battery/charge"],
          x["aeg/ups/temperature"] ) ;
   count++;
}
db.logger201702.find( 
   { timestamp : 
         { $gte : ISODate("2017-04-09T19:00:00") , $lt : ISODate("2017-04-09T23:59:00" ) }
   }
).forEach(

    function(x){ y(x) }

)
print(" length : "+Object.keys(array));
