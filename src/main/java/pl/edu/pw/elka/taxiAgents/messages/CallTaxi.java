package pl.edu.pw.elka.taxiAgents.messages;

import pl.edu.pw.elka.taxiAgents.Position;

import java.io.Serializable;

public class CallTaxi implements Serializable {
    Position from;
    Position to;
    boolean ifBabySeat;
    boolean ifHomePet;
    boolean ifLargeLuggage;
    int numberOFPassengers;
    String kindOfClient;

    public CallTaxi(Position from, Position to, boolean ifBabySeat, boolean ifHomePet, boolean ifLargeLuggage, int numberOFPassengers, String kindOfClient) {
        this.from = from;
        this.to = to;
        this.ifBabySeat = ifBabySeat;
        this.ifHomePet = ifHomePet;
        this.ifLargeLuggage = ifLargeLuggage;
        this.numberOFPassengers = numberOFPassengers;
        this.kindOfClient = kindOfClient;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public boolean isIfBabySeat() {
        return ifBabySeat;
    }

    public boolean isIfHomePet() {
        return ifHomePet;
    }

    public boolean isIfLargeLuggage() {
        return ifLargeLuggage;
    }

    public int getNumberOFPassengers() {
        return numberOFPassengers;
    }

    public String getKindOfClient() {
        return kindOfClient;
    }
}
