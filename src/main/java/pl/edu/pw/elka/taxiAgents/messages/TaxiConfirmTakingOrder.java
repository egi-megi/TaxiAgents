package pl.edu.pw.elka.taxiAgents.messages;

import pl.edu.pw.elka.taxiAgents.Position;

import java.io.Serializable;

public class TaxiConfirmTakingOrder implements Serializable {
    String idQuery;
    boolean ifTake;

    public TaxiConfirmTakingOrder(String idQuery, boolean ifTake) {
        this.idQuery = idQuery;
        this.ifTake = ifTake;
    }

    public static TaxiConfirmTakingOrder accepts(String driverStatus, String idQuery){
        if (driverStatus.equalsIgnoreCase("free") || driverStatus.equalsIgnoreCase("nearEnd") || driverStatus.equalsIgnoreCase("goesHome")) {
            return new TaxiConfirmTakingOrder(idQuery, true);
        } else {
            return  new TaxiConfirmTakingOrder(idQuery, false);
        }

    }


    public String getIdQuery() {
        return idQuery;
    }

    public boolean isIfTake() {
        return ifTake;
    }
}
