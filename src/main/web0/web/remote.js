    __configuration = { };
    __configuration["bathroom.temperature.nice"]    = [ "bathroom.nice" , "Bathroom High" , 21.0 ] ;
    __configuration["bathroom.temperature.low"]     = [ "bathroom.low"  , "Bathroom Low"  , 16.0 ] ;
    __configuration["bathroom.temperature.drying"]  = [ "bathroom.drying"  , "Bathroom Drying"  , 22.0 ] ;
    __configuration["livingroom.temperature.nice"]  = [ "livingroom.nice" , "Livingroom High" , 25.0 ] ;
    __configuration["livingroom.temperature.low"]   = [ "livingroom.low"  , "Livingroom Low"  , 16.0 ] ;
    __configuration["office.temperature.nice"]      = [ "office.high"     , "Office High"  , 25.0 ] ;
    __configuration["office.temperature.low"]       = [ "office.low"      , "Office Low"   , 16.0 ] ;

    /*---------------------------------------------*/
    function initVars() {
    /*---------------------------------------------*/

        outMessage = document.getElementById('outMessage');
        errMessage = document.getElementById('errorMessage');
        outMessage.innerHTML = "Hallo Trude (c)";
        errMessage.innerHTML = "";

        desiredId = document.getElementById('livingroom.desired');
        currentId = document.getElementById('livingroom.current');
        desiredBathId = document.getElementById('bathroom.desired');
        currentBathId = document.getElementById('bathroom.current');
        desiredOfficeId = document.getElementById('office.desired');
        currentOfficeId = document.getElementById('office.current');
        currentHallId = document.getElementById('hallway.current');
        appartmentInId = document.getElementById('apartment.in');
        brightLivId  = document.getElementById('livingroom.brightness');
        brightBathId = document.getElementById('bathroom.brightness');
        brightHallId = document.getElementById('hallway.brightness');
        brightOfficeId = document.getElementById('office.brightness');
        moveLivId    = document.getElementById('livingroom.move');
        moveBathId   = document.getElementById('bathroom.move');
        moveHallId   = document.getElementById('hallway.move');
        moveOfficeId = document.getElementById('office.move');
        configurationId = document.getElementById('configurationId');
        buttontableId   = document.getElementById('buttontable');
        officeHighLowId   = document.getElementById('officehighlow');

        initConfigurationEntries() ;

        xhttp = new XMLHttpRequest();
 
        clickUpdate();
    }
    /*---------------------------------------------*/
    function reportError(em){
    /*---------------------------------------------*/
       //console.log(em);
       errMessage.innerHTML += ("/"+ em);
    };
    /*---------------------------------------------*/
    function report(em){
    /*---------------------------------------------*/
       outMessage.innerHTML = em;
    };
    /*---------------------------------------------*/
     function clickUpdate() {
    /*---------------------------------------------*/
        queryArray(  ) ;
     };
    /*---------------------------------------------*/
     function queryArray( queryList ) {
    /*---------------------------------------------*/
          
        var qs = "{ \"elist\" : [ { \"method\" : \"get\" , \"name\" : \".*\" } ] }" ;
/*
        var qs = "{ \"elist\" : [" ;
        for( i = 0 ; i < queryList.length ; i++ ){
           qs = qs + "   { \"method\" : \"get\" , \"name\" : \""+queryList[i]+"\" }," ;
        }
        qs = qs + "]}" ;
*/
        report(qs);
        xhttp.onreadystatechange =replyQueryArray ;
        xhttp.open("POST", "/memory/json", true);
        xhttp.send(qs);
     };
    /*---------------------------------------------*/
    function replyQueryArray(){
    /*---------------------------------------------*/

       if( ( xhttp.readyState != 4 ) || ( xhttp.status != 200 ) )return false;

       var h = interpretReplyQueryArray(); 

       if( ! h )return false ;
    
       renderReply(h) ;
     }
    /*---------------------------------------------*/
     function setSetValueDefault( value , defValue ){
    /*---------------------------------------------*/
          if( value == undefined ){
             return defValue ;
          }else{
             return value ;
          }
     }
    /*---------------------------------------------*/
     function setColorOnValue( value ){
    /*---------------------------------------------*/
          if( ( value == "on" ) || ( value == "1.0" ) ){
             return "green" ;
          }else{
             return "red" ;
          }
     }
    /*---------------------------------------------*/
    function renderReply(h){
    /*---------------------------------------------*/

       desiredId.innerHTML     = setSetValueDefault( h["livingroom.heater.temperature.desired"]   , "-" ); 
       currentId.innerHTML     = setSetValueDefault( h["livingroom.heater.temperature.messured"]  , "-" );             
       currentBathId.innerHTML = setSetValueDefault( h["bathroom.heater.temperature.messured"]    , "-" );             
       desiredBathId.innerHTML = setSetValueDefault( h["bathroom.heater.temperature.desired"]     , "-" );             
       currentOfficeId.innerHTML = setSetValueDefault( h["office.heater.temperature.messured"]    , "-" );             
       desiredOfficeId.innerHTML = setSetValueDefault( h["office.heater.temperature.desired"]     , "-" );             

          appartmentInId.style.color = setColorOnValue( h["apartment.in"] )
          moveLivId.style.color      = setColorOnValue( h["livingroom.move"] )
          moveBathId.style.color     = setColorOnValue( h["bathroom.move"] )
          moveHallId.style.color     = setColorOnValue( h["hallway.move"] )
          moveOfficeId.style.color   = setColorOnValue( h["office.move"] )

          brightHallId.innerHTML = setSetValueDefault( h["hallway.brightness"]    , "-" );
          brightLivId.innerHTML  = setSetValueDefault( h["livingroom.brightness"] , "-" );
          brightBathId.innerHTML = setSetValueDefault( h["bathroom.motion.brightness"]   , "-" );
          brightOfficeId.innerHTML = setSetValueDefault( h["office.motion.brightness"]   , "-" );

          var val = h["office.heater.temperature.desired"];
          if( Number(val) > 17.0 ) officeHighLowId.style.color = "red";
          else officeHighLowId.style.color = "blue";

          for( x in __configuration ){

              var y = __configuration[x];

              var id = document.getElementById( x ) ;
       
              if(  id == null ){
                reportError("Assertion: Missing on page : "+x);
                continue ; 
              }
              if( h[x] == undefined ){
                 id.innerHTML = __configuration[x][2] ;
              }else{
                 id.innerHTML = __configuration[x][2] = h[x] ;
              }
            
          }
       return true;

    };
    /*---------------------------------------------*/
    function buttonUpDownClick( event ){
    /*---------------------------------------------*/
        var h = event.target.helper ;
        var out = 0 ;
        var inNumber = Math.round( h.view.innerHTML ) ;
        if( isNaN(inNumber) )inNumber = 15.5
      
        if( event.target == h.buttonUp ){
           out =  Math.min( 30, inNumber + 1 )  ;
        }else{
           out =  Math.max( 15 , inNumber - 1  ) ;
        }
        setVariable( h.x , "" + out  ) ;
        report("Setting "+h.x+" : "+out);
    }
    /*---------------------------------------------*/
    function configInput( event ){
    /*---------------------------------------------*/
       outMessage.innerHTML = event.key + " " +event.target.value ;
       reportError( event.key ) ;
       if( ( event.key == "Enter") || (event.key == undefined ) ){
           var pat = /^[0-9]+[\.[0-9]*]{0,1}$/i ;
           outMessage.innerHTML = event.key + " " +pat.test( event.target.value ) ;
           if( ( ! pat.test( event.target.value ) ) || ( event.target.value > 50.0 ) ){
              alert("Must be a value between 0 and 50");           
              event.target.value = __configuration[event.target.id][2];
              return ;
           }else{
              setVariable( event.target.id , event.target.value ) ;
           }
       }
    }
    /*---------------------------------------------*/
    function initConfigurationEntries(){
    /*---------------------------------------------*/
        for( x in __configuration ){

            var y = __configuration[x];

            var row = document.createElement("TR");
            row.className = "buttontable" ;

            var td = document.createElement("TD");
            td.className = "buttontable" ;
            td.colSpan = 3 ;

            var div = document.createElement("DIV");
            div.className = "configlabel" ;
            div.innerHTML = y[1] ;
 
            td.appendChild( div ) ; 
            row.appendChild(td); 
            buttontableId.appendChild( row ) ;

            row = document.createElement("TR");
            row.className = "buttontable" ;

            td = document.createElement("TD");
            td.className = "buttontable" ;
            td.colSpan = 3 ;

            td.appendChild( createUpDownField( x , y )  ) ; 

            row.appendChild(td); 

            buttontableId.appendChild( row ) ;
     
            outMessage.innerHTML = y  ;

        }
    }
    /*---------------------------------------------*/
    function initConfigurationEntries2(){
    /*---------------------------------------------*/
        for( x in __configuration ){

            var y = __configuration[x];

            var row = document.createElement("TR");
            row.className = "buttontable" ;

            var td = document.createElement("TD");
            td.className = "buttontable" ;

            var div = document.createElement("DIV");
            div.className = "configlabel" ;
            div.innerHTML = y[1] ;
 
            td.appendChild( div ) ; 
            row.appendChild(td); 

            var td2 = document.createElement("TD");
            td2.className = "buttontable" ;
            td2.colSpan = 2 ;

            td2.appendChild( createUpDownField( x , y )  ) ; 

            row.appendChild(td2); 

            buttontableId.appendChild( row ) ;
     
            outMessage.innerHTML = y  ;

        }
    }
    /*---------------------------------------------*/
    function createUpDownField( x  ,   y   ){
    /*---------------------------------------------*/

         var helper = {} ;
         helper.x = x ;
         helper.y = y ;

         var tab = document.createElement("TABLE") ;
         tab.className   = "upDownField" ;
 
         var row = document.createElement("TR") ;
         row.className   = "upDownField" ;

            var data = document.createElement("TD") ;
            data.className   = "upDownField" ;

               var buttonUp   = document.createElement("BUTTON") ;
               buttonUp.innerHTML = "Down" ;
               buttonUp.className = "upDownField" ;
               buttonUp.onclick   = buttonUpDownClick
               buttonUp.helper    = helper ;
               helper.buttonDown  = buttonUp ;

            data.appendChild(buttonUp); 

         row.appendChild(data);

            data = document.createElement("TD") ;
            data.className   = "upDownField" ;

               var inp         = document.createElement("DIV");
               inp.innerHTML   = y[2] ;
               inp.className   = "upDownField" ;
               inp.helper      = helper ;
               inp.id          = x;
               helper.view     = inp ;

            data.appendChild(inp);

         row.appendChild(data);

            data = document.createElement("TD") ;
            data.className   = "upDownField" ;

               buttonUp   = document.createElement("BUTTON") ;
               buttonUp.innerHTML = "Up" ;
               buttonUp.className = "upDownField" ;
               buttonUp.onclick   = buttonUpDownClick
               buttonUp.helper    = helper ;
               helper.buttonUp    = buttonUp ;

            data.appendChild(buttonUp); 

         row.appendChild(data);

         tab.appendChild(row);

         return tab ;



    }
