package org.adridadou.ethereum.propeller.values;

/**
 * Created by davidroon on 03.04.17.
 * This code is released under Apache 2 license
 */
public class EventInfo {
    private final EthData eventSignature;
    private final EthData eventArguments;


    public EventInfo(EthData eventSignature, EthData eventArguments) {
        this.eventSignature = eventSignature;
        this.eventArguments = eventArguments;
    }

    public EthData getEventSignature() {
        return eventSignature;
    }

    public EthData getEventArguments() {
        return eventArguments;
    }
}
