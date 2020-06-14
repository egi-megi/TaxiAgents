package pl.edu.pw.elka.taxiAgents.messages;

import pl.edu.pw.elka.taxiAgents.Position;

import java.io.Serializable;
import java.util.List;

public class TaxiStatus implements Serializable {
    public String status;
    public Position position;
    public List<Position> route;
    public boolean isWithClient;

    public TaxiStatus(String status, Position position, List<Position> route, boolean isWithClient) {
        this.status = status;
        this.position = position;
        this.route = route;
        this.isWithClient = isWithClient;
    }
}
