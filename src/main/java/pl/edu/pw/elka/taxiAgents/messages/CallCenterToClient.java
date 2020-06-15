package pl.edu.pw.elka.taxiAgents.messages;

import java.io.Serializable;

public class CallCenterToClient implements Serializable {

    String taxiName;
    Double timeToPickUp;
    Double price;

    public CallCenterToClient(String taxiName, Double timeToPickUp, Double price) {
        this.taxiName = taxiName;
        this.timeToPickUp = timeToPickUp;
        this.price = price;
    }

    public String getTaxiName() {
        return taxiName;
    }

    public Double getTimeToPickUp() {
        return timeToPickUp;
    }

    public Double getPrice() { return price; }
}