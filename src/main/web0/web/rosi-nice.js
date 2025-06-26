    
    __chartsLivingoomTemp1 = null ;
    __dataLivingroomTemp1  = null ;
    __optionsLivingroomTemp1 = null ;
    
    __chartsLivingoomBrightness   = null ;
    __dataLivingroomBrightness    = null ;
    __optionsLivingroomBrightness = null ;
    
    
    
      __chartsLivingoom = [] ;
      __chartsBathroom  = [] ;
      __chartsHallway   = [] ;
      __chartsOffice    = [] ;

      __displayReady = false;

      if( google != undefined  ){
         google.charts.load('current', {'packages':['gauge']});
         google.charts.setOnLoadCallback(initVars);
      }

      
    class ImageClass {
        constructor( imageID ){
            this.image =  document.getElementById( imageID );
            this.a = "Ampelmann-red.png" ;
            this.b = "Ampelmann-green.png" ;
            this.image.src = this.a ;
            
        }
        setStill( still ){
           if( still == true )this.image.src = this.b ;
           else this.image.src = this.a ;
        }
    }
    class ChartClass {
        constructor( target , vector , opt ){
           this.chart = new google.visualization.Gauge(document.getElementById(target));
           var d = [ ['Label' , 'Value' ] ] ;
           for( var i = 0 ; i < vector.length ; i++ ) 
              d.push( vector[i] ) ;
           this.data = google.visualization.arrayToDataTable( d ) ;
           this.options = Object.assign({}, opt); /* {
               width: 300, height: 300,
               min: 0   , max : 255 ,
               yellowFrom: 155 , yellowTo: 255,
               minorTicks: 16
           } */ ;
           this.chart.draw( this.data , this.options ) ;
        }
        setDevider( no ){
           this.options.yellowFrom = no ;
        }
        setValue( nd , mode ){
           if( mode ){
              this.data.setValue(mode, 1, nd );
           }else{
              this.data.setValue(0, 1, nd );
           }
           this.chart.draw( this.data , this.options ) ;
        }
    }
    function onTimeout(){
        if( __displayReady == false ){
               document.getElementById("text").innerHTML = "Couldn't load libraries" ;
        }
    }
    function bodyLoaded(){
         document.getElementById("overlay").style.display = "block";   
         setTimeout(onTimeout,5000);
    }
    /*---------------------------------------------*/
    function initVars() {
    /*---------------------------------------------*/

        errMessage = outMessage = document.getElementById('outMessage');
        outMessage.innerHTML = "Hallo Trude (c)";

        __chartsLivingoom.temperature = [] ; 
        
        var dd = [ [ 'light' , 200 ] ] ;

        var lightOpts = {
               width: 700, 
               height: 300,
               min: 0   , 
               max : 255 ,
               yellowFrom: 155 , 
               yellowTo: 255,
               majorTicks: [ "0" , "64" , "128" , "192" , "256" ] ,           
               minorTicks: 4, 
               forceIFrame: true ,
           } ;

        __chartsLivingoom.brightness = new ChartClass( 'livingroom.brightness' , dd , lightOpts ) ;
        __chartsBathroom.brightness  = new ChartClass( 'bathroom.brightness'   , dd , lightOpts ) ;
        __chartsOffice.brightness    = new ChartClass( 'office.brightness'   , dd , lightOpts ) ;
        __chartsHallway.brightness   = new ChartClass( 'hallway.brightness'   , dd , lightOpts ) ;
        
        __chartsLivingoom.movement = new ImageClass( 'livingroom.motion' ) ;
        __chartsBathroom.movement  = new ImageClass( 'bathroom.motion' ) ;
        __chartsOffice.movement    = new ImageClass( 'office.motion' ) ;
        __chartsHallway.movement   = new ImageClass( 'hallway.motion' ) ;
        
        var opts = {
               width:  700, 
               height: 300,
               min:    15 ,
               max:    35 ,
        } ;
        opts.yellowFrom  = ( opts.max - opts.min ) / 2 + opts.min ;
        opts.yellowTo    = opts.max ;
        opts.minorTicks  = 5 ;
        opts.majorTicks  = [ "15" , "20" , "25" , "30" , "35" ] ,           
        
        dd = [ [ 'T1' , 20 ],[ 'T2' , 22 ] ] ;
        
        __chartsLivingoom.temperature = new ChartClass( 'livingroom.temperature' , dd , opts ) ;

        dd = [ [ 'T1' , 20 ]  ] ;
        
        __chartsBathroom.temperature = new ChartClass( 'bathroom.temperature' , dd , opts ) ;
        
        /* clickUpdate(); */
         document.getElementById("overlay").style.display = "none";
         displayReady = true ;
    }
    /*---------------------------------------------*/
    function reportError(em){
    /*---------------------------------------------*/
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
        queryArray( ) ;
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
    function renderReply( h ){
    /*---------------------------------------------*/
          
          var temp1    = parseFloat(h["livingroom.heater.temperature.messured"]);
          var desired  = parseFloat(h["livingroom.heater.temperature.desired"]);
          var temp2    = parseFloat(h["livingroom.light.01.temperature.messured"]);

          __chartsLivingoom.temperature.setDevider( desired );
          __chartsLivingoom.temperature.setValue( temp1 , 0);    
          __chartsLivingoom.temperature.setValue( temp2 , 1);    

          temp1    = parseFloat(h["bathroom.heater.temperature.messured"]);
          desired  = parseFloat(h["bathroom.heater.temperature.desired"]);
          __chartsBathroom.temperature.setDevider( desired );
          __chartsBathroom.temperature.setValue( temp1);    

          
          var bright = parseFloat(h["livingroom.brightness"]);
          __chartsLivingoom.brightness.setDevider( 128 );
          __chartsLivingoom.brightness.setValue( bright );
          bright = parseFloat(h["bathroom.motion.brightness"]);
          __chartsBathroom.brightness.setDevider( 128 );
          __chartsBathroom.brightness.setValue( bright );
          bright = parseFloat(h["hallway.brightness"]);
          __chartsHallway.brightness.setDevider( 128 );
          __chartsHallway.brightness.setValue( bright );
          bright = parseFloat(h["office.motion.brightness"]);
          __chartsOffice.brightness.setDevider( 128 );
          __chartsOffice.brightness.setValue( bright );
         /* 
          __chartsLivingoom.movement.setStill( parseFloat(h["livingroom.move"]) > 0.5 ) ;
          __chartsBathroom.movement.setStill( parseFloat(h["bathroom.move"]) > 0.5 ) ;
          __chartsOffice.movement.setStill( parseFloat(h["office.move"]) > 0.5 ) ;
          __chartsHallway.movement.setStill( parseFloat(h["hallway.move"]) > 0.5 ) ;
*/
          __chartsLivingoom.movement.setStill( h["livingroom.move"] == "on" ) ;
          __chartsBathroom.movement.setStill( h["bathroom.move"]    == "on" ) ;
          __chartsOffice.movement.setStill( h["office.move"]        == "on") ;
          __chartsHallway.movement.setStill( h["hallway.move"]      == "on" ) ;


    };
