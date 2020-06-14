package pl.edu.pw.elka.taxiAgents.messages;

import pl.edu.pw.elka.taxiAgents.Position;

import java.io.Serializable;

public class ClientStatus implements Serializable{
    public Position position;
    public boolean isTaxiAssigned;
    public long timeToPickup;

    public ClientStatus(Position position, boolean isTaxiAssigned, long timeToPickup) {
        this.position = position;
        this.isTaxiAssigned = isTaxiAssigned;
        this.timeToPickup = timeToPickup;
    }
}
