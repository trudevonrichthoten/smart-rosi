package org.rosi.nodes ;


public class RosiTriggerDevice extends RosiDevice {

    public RosiTriggerDevice( String deviceName  ){
        super( "trigger" , deviceName ) ;
    }
    public String toString(){
        return "Trigger "+getDeviceName() ;
    }
}
