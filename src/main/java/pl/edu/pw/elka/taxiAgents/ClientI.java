package pl.edu.pw.elka.taxiAgents;

import java.io.IOException;

public interface ClientI {
    String doQuery(String query) throws IOException;
}
