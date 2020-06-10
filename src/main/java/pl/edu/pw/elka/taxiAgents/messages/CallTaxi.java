package pl.edu.pw.elka.taxiAgents.messages;

import java.io.Serializable;

public class CallTaxi implements Serializable {
    String from;
    String to;

    public CallTaxi(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}
