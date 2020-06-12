package pl.edu.pw.elka.taxiAgents.messages;

import java.io.Serializable;

public class CallCenterToTaxi implements Serializable {
    String fromLongitude;
    String fromLatitude;
    String toLongitude;
    String toLatitude;
    boolean ifBabySeat;
    boolean ifHomePet;
    int numberOFPassengers;
    String kindOfClient;
    String idQuery;

    public CallCenterToTaxi(String from, String to, String idQuery) {
        this.from = from;
        this.to = to;
        this.idQuery = idQuery;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getIdQuery() {
        return idQuery;
    }
}
