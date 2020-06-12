package pl.edu.pw.elka.taxiAgents.messages;

import java.io.Serializable;

public class CallTaxi implements Serializable {
    String fromLongitude;
    String fromLatitude;
    String toLongitude;
    String toLatitude;
    boolean ifBabySeat;
    boolean ifHomePet;
    int numberOFPassengers;
    String kindOfClient;

    public CallTaxi(String fromLongitude, String fromLatitude, String toLongitude, String toLatitude, boolean ifBabySeat, boolean ifHomePet, int numberOFPassengers, String kindOfClient) {
        this.fromLongitude = fromLongitude;
        this.fromLatitude = fromLatitude;
        this.toLongitude = toLongitude;
        this.toLatitude = toLatitude;
        this.ifBabySeat = ifBabySeat;
        this.ifHomePet = ifHomePet;
        this.numberOFPassengers = numberOFPassengers;
        this.kindOfClient = kindOfClient;
    }

    public String getFromLongitude() {
        return fromLongitude;
    }

    public String getFromLatitude() {
        return fromLatitude;
    }

    public String getToLongitude() {
        return toLongitude;
    }

    public String getToLatitude() {
        return toLatitude;
    }

    public boolean isIfBabySeat() {
        return ifBabySeat;
    }

    public boolean isIfHomePet() {
        return ifHomePet;
    }

    public int getNumberOFPassengers() {
        return numberOFPassengers;
    }

    public String getKindOfClient() {
        return kindOfClient;
    }
}
