package pl.edu.pw.elka.taxiAgents.messages;

import pl.edu.pw.elka.taxiAgents.Position;

import java.io.Serializable;

public class CallCenterConfirmTaxi implements Serializable{
    Position from;
    Position to;
    String kindOfClient;
    String idQuery;

    public CallCenterConfirmTaxi(Position from, Position to, String kindOfClient, String idQuery) {
        this.from = from;
        this.to = to;
        this.kindOfClient = kindOfClient;
        this.idQuery = idQuery;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public String getKindOfClient() {
        return kindOfClient;
    }

    public String getIdQuery() {
        return idQuery;
    }
}
