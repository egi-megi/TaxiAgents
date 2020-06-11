package pl.edu.pw.elka.taxiAgents.messages;

import java.io.Serializable;

public class CallCenterConfirmTaxi implements Serializable{
    String from;
    String to;
    String idQuery;

    public CallCenterConfirmTaxi(String from, String to, String idQuery) {
        this.from = from;
        this.to = to;
        this.idQuery = idQuery;
    }
}
