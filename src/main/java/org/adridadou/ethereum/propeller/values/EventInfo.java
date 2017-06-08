package org.adridadou.ethereum.propeller.values;

import java.util.List;

/**
 * Created by davidroon on 03.04.17.
 * This code is released under Apache 2 license
 */
public class EventInfo {
    private final EthData eventSignature;
    private final EthData eventArguments;
    private final List<EthData> indexedArguments;

    public EventInfo(EthData eventSignature, EthData eventArguments, List<EthData> indexedArguments) {
        this.eventSignature = eventSignature;
        this.eventArguments = eventArguments;
        this.indexedArguments = indexedArguments;
    }

    public List<EthData> getIndexedArguments() {
        return indexedArguments;
    }

    public EthData getEventSignature() {
        return eventSignature;
    }

    public EthData getEventArguments() {
        return eventArguments;
    }

    @Override
    public String toString() {
        return "EventInfo{" +
                "eventSignature=" + eventSignature +
                ", eventArguments=" + eventArguments +
                ", indexedArguments=" + indexedArguments +
                '}';
    }
}
