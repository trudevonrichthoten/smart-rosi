
        xhttp = new XMLHttpRequest();
        __counter = 0 ;
    /*---------------------------------------------*/
    function setVariable( name , value ){
    /*---------------------------------------------*/
        __counter ++ ;
        if( value == '*' )value = 'XX-'+__counter;
        var message = 
          " { \"list\" : ["+
            "   { \"method\" : \"set\" , \"name\" : \""+name+"\" , \"value\" : \""+value+"\"  }"+
          "]}" ;
       xhttp.onreadystatechange = replySetVariable ;
       xhttp.open("POST", "/memory/json", true);
       report("Sending : "+message);
       xhttp.send(message);
    }; 
    /*---------------------------------------------*/
    function replySetVariable(){
    /*---------------------------------------------*/
       if (xhttp.readyState == 4 && xhttp.status == 200) {
          report("Received string : "+xhttp.responseText);
          var jsob = JSON.parse(xhttp.responseText);
          if( jsob.result == undefined ){
             reportError("Not a proper reply from Rosi, no 'result'");
          }else if( jsob.result != 0 ){
             reportError("Error in reply ("+jsob.result+") : "+jsob.errorMessage);
          }else{
             reportError("");
             clickUpdate();
          }
       }
    }
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
    function interpretReplyQueryArray(){
    /*---------------------------------------------*/
        /*
         * Check reply consistency.
         */
        report("replyQueryArray received string : "+xhttp.responseText);
        var jsob = JSON.parse(xhttp.responseText);
        if( jsob.result == undefined ){
           reportError("Not a proper reply from Rosi, no 'result'");
           return false ;
        }else if( jsob.result != 0 ){
           reportError("Error in reply ("+jsob.result+") : "+jsob.errorMessage);
           return false ;
        }
        if( jsob.elist == undefined ){
           reportError("Syntax error in reply: Can't find 'elist'.");
           return false ;
        }
        /*
         * Copy reply into 'h'.
         */
        var elist = jsob.elist ;
        var eLoopLength = elist.length ;
        if( eLoopLength < 1 ){
           reportError("Syntax error in reply: Result it not a list or empty.");
           return false ;
        }
        var h = {} ;
        for( m = 0 ; m < eLoopLength ; m++ ){
           var e = elist[m]  ;
           if( e.list == undefined ){
              reportError("Syntax error in reply: Missing 'list' in elist entry.");
              return false ;
           }
           var elength = e.list.length ;
           for( l = 0 ; l < elength ; l ++ ){
              attr = e.list[l];
              h[attr.name] = attr.value;
              report(attr);
           } 
         }
         return h ;
    };
