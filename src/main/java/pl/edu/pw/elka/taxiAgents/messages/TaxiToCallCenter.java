package pl.edu.pw.elka.taxiAgents.messages;

import java.io.Serializable;

public class TaxiToCallCenter implements Serializable {
    boolean ifAccepts;
    int taxiPlace;
    double timeToPickUp;
    String queryID;

    private TaxiToCallCenter(boolean ifAccepts, int taxiPlace, double timeToPickUp, String queryID) {
        this.ifAccepts = ifAccepts;
        this.taxiPlace = taxiPlace;
        this.timeToPickUp = timeToPickUp;
        this.queryID = queryID;
    }

    public static TaxiToCallCenter accepts(int taxiPlace, double timeToPickUp, String queryID){
        return new TaxiToCallCenter(true,taxiPlace,timeToPickUp,queryID);
    }
    public static TaxiToCallCenter reject(String queryID){
        return  new TaxiToCallCenter(false,-1,-1,queryID);
    }

    public boolean isIfAccepts() {
        return ifAccepts;
    }

    public int getTaxiPlace() {
        return taxiPlace;
    }

    public double getTimeToPickUp() {
        return timeToPickUp;

    }

    public String getQueryID() {
        return queryID;
    }
}
