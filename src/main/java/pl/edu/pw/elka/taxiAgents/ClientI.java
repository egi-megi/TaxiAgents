package pl.edu.pw.elka.taxiAgents;

import java.io.IOException;

public interface ClientI {
    String doQuery(String fromLongitude,
                   String fromLatitude,
                   String toLongitude,
                   String toLatitude,
                   boolean ifBabySeat,
                    boolean ifHomePet,
                    int numberOFPassengers,
                    String kindOfClient) throws IOException;
}
