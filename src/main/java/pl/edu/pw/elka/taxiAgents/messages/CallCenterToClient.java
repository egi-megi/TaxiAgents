package pl.edu.pw.elka.taxiAgents.messages;

import java.io.Serializable;

public class CallCenterToClient implements Serializable {

    String taxiName;
    Double timeToPickUp;

    public CallCenterToClient(String taxiName, Double timeToPickUp) {
        this.taxiName = taxiName;
        this.timeToPickUp = timeToPickUp;
    }

    public String getTaxiName() {
        return taxiName;
    }

    public Double getTimeToPickUp() {
        return timeToPickUp;
    }
}
