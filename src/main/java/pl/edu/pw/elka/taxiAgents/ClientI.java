package pl.edu.pw.elka.taxiAgents;

import java.io.IOException;

public interface ClientI {
    String doQuery(Position from,
                   Position to,
                   boolean ifBabySeat,
                   boolean ifHomePet,
                   boolean ifLargeLuggage,
                   int numberOFPassengers,
                   String kindOfClient) throws IOException, InterruptedException;
}
