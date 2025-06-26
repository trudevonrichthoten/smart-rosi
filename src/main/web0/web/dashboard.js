   
   var __xcounter = 0 ;
   
   var value_map = {} ;
   var messuredID = 0;
   var desiredID  = 0;
   var counterID = 0;

   var map = [
     { name : 'livingroom.messured' , id : 0 , key : 'livingroom.heater.temperature.messured' , value : "50.0" },
     { name : 'livingroom.desired' , id : 0 , key : 'livingroom.heater.temperature.desired' , value : "50.0" },
     { name : 'bathroom.messured' , id : 0 , key : 'bathroom.heater.temperature.messured' , value : "50.0" },
     { name : 'bathroom.desired' , id : 0 , key : 'bathroom.heater.temperature.desired' , value : "50.0" },
     { name : 'office.messured' , id : 0 , key : 'office.heater.temperature.messured' , value : "50.0" },
     { name : 'office.desired' , id : 0 , key : 'office.heater.temperature.desired' , value : "50.0" },
   ];

    function fix_float(in_string){
        console.log(in_string);
        var fs = in_string.split('.');
        var ac = fs[1];
        if( ac.length == 0 ){
           ac = ac + "00"; 
        }else if( ac.length == 1 ){
           ac = ac + "0"; 
        }
        return fs[0]+"."+ac.substring(0,2);
    }

    /*---------------------------------------------*/
    function initVars() {
    /*---------------------------------------------*/

        outMessage = document.getElementById('outMessage');
        outMessage.innerHTML = "Hallo Truedelchen (c)";

        for( map_entry of map ){

           console.log("Searching : "+map_entry);
           map_entry.id = document.getElementById(map_entry.name) ;
           map_entry.id.innerHTML = fix_float(""+map_entry.value);
        }

        clickUpdate();

     }
    /*---------------------------------------------*/
     function clickUpdate() {
    /*---------------------------------------------*/
         
         sendRequest();
         
         setTimeout( clickUpdate , 2000 ) ;

     }
    /*---------------------------------------------*/
     function sendRequest() {
    /*---------------------------------------------*/
        queryArray( ) ;
     };
    /*---------------------------------------------*/
    function replyQueryArray(){
    /*---------------------------------------------*/
       //console.log("State : "+xhttp.readyState+"; Status : "+xhttp.status);
       if( ( xhttp.readyState != 4 ) || ( xhttp.status != 200 ) )return false;

       var x = interpretReplyQueryArray();

       if( ! x )return false ;

       renderReply(x) ;

     }
    /*---------------------------------------------*/
    function renderReply( x ){
    /*---------------------------------------------*/
        value_map = x ;
        //console.log("Map: "+JSON.stringify(value_map));
        for( map_entry of map ){
           map_entry.id.innerHTML = fix_float(value_map[map_entry.key] );
        }
        counterID.innerHTML = ""+__xcounter;
        __xcounter++;
        
    };
    /*---------------------------------------------*/
    function reportError(em){
    /*---------------------------------------------*/
       // errMessage.innerHTML += ("/"+ em);
       alert(em);
    };
    /*---------------------------------------------*/
    function report(em){
    /*---------------------------------------------*/
       // outMessage.innerHTML = em;
    };
     
