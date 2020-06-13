package pl.edu.pw.elka.taxiAgents;

import jade.core.*;
import jade.core.Runtime;
import jade.core.behaviours.*;

import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import jade.lang.acl.*;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import pl.edu.pw.elka.taxiAgents.messages.CallCenterToTaxi;
import pl.edu.pw.elka.taxiAgents.messages.CallCenterConfirmTaxi;
import pl.edu.pw.elka.taxiAgents.messages.TaxiRegister;
import pl.edu.pw.elka.taxiAgents.messages.TaxiToCallCenter;

import java.io.IOException;

import static java.lang.Thread.*;

public class TaxiAgent extends Agent {

    final String KIND_OF_CARS_VAN = "van";
    final String KIND_OF_CARS_COMBI = "combi";
    final String KIND_OF_CARS_SEDAN = "sedan";
    final String KIND_OF_CARS_VIP = "vip";

    final String DRIVER_STATUS_FREE = "free";
    final String DRIVER_STATUS_UNAVAILABLE = "unavailable";
    final String DRIVER_STATUS_BREAK = "break";
    final String DRIVER_STATUS_WORKING = "working";
    final String DRIVER_STATUS_VEHICLE_BREAKDOWN = "vehicleBreakdown";
    final String DRIVER_STATUS_GOES_HOME = "goesHome";

    Position positionTaxiNow;
    Position positionTaxiHome;
    boolean ifBabySeat;
    boolean ifHomePet;
    String kindOFCar;
    //enum kindOFCar {sedan, combi, van, vip};
    int numberOFPassengers;
    boolean ifStandByForSpecialTask;
    boolean ifExperiencedDriver;
    String driverStatus;
    double workingTimeInThisDay;
    double todayEarnings;
    int timeFromLastClient;
    double timeToEndOrder = 0;
    double speed = 0;


    void setupFromArgs(Object[] taxisData) {
        positionTaxiNow = (Position) (taxisData[0]);
        positionTaxiHome = (Position) (taxisData[1]);
        ifBabySeat = (boolean) taxisData[2];
        ifHomePet = (boolean) taxisData[3];
        kindOFCar = (String) taxisData[4];
        numberOFPassengers = (int) taxisData[5];
        ifStandByForSpecialTask = (boolean) taxisData[6];
        ifExperiencedDriver = (boolean) taxisData[7];
        driverStatus = (String) taxisData[8];
        workingTimeInThisDay = (double) taxisData[9];
        todayEarnings = (int) taxisData[10];
        timeFromLastClient = (int) taxisData[11];
        timeToEndOrder = (int) taxisData[12];
    }

    boolean isMissingBabySeat(CallCenterToTaxi cct) {
        return cct.isIfBabySeat() && ifBabySeat == false;
    }

    boolean canNotTakePet(CallCenterToTaxi cct) {
        return (cct.isIfHomePet() && ifHomePet == false);
    }

    boolean canNotTakeLuggage(CallCenterToTaxi cct) {
        return cct.isIfLargeLuggage() &&
                (!
                        (kindOFCar.equalsIgnoreCase(KIND_OF_CARS_VAN)
                                || (kindOFCar.equalsIgnoreCase(KIND_OF_CARS_COMBI))
                        ));
    }

    boolean tooMuchPassengers(CallCenterToTaxi cct) {
        return cct.getNumberOFPassengers() > numberOFPassengers;
    }

    protected void setup() {
        setupFromArgs(getArguments());

        addBehaviour(new CyclicBehaviour(this) {

            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    try {
                        Object o = msg.getContentObject();
                        if (o instanceof CallCenterToTaxi) {
                            CallCenterToTaxi cct = (CallCenterToTaxi) o;
                            int fromLongitudeCustomer = cct.getFrom().getLongitude();
                            TaxiToCallCenter response;

                            if (isMissingBabySeat(cct) ||
                                    canNotTakePet(cct) ||
                                    tooMuchPassengers(cct) ||
                                    canNotTakeLuggage(cct)
                            ) {
                                response = TaxiToCallCenter.reject(cct.getIdQuery());
                            } else {
                                double time = Math.abs(positionTaxiNow.longitude - fromLongitudeCustomer) * positionTaxiNow.latitude;
                                response = TaxiToCallCenter.accepts(positionTaxiNow.longitude, time, cct.getIdQuery());
                                System.out.println(" - " +
                                        myAgent.getLocalName() + " received: " +
                                        fromLongitudeCustomer + " time " + time + "accepts " + response.isIfAccepts());
                            }

                            ACLMessage reply = msg.createReply();
                            reply.setPerformative(ACLMessage.INFORM);
                            reply.setContentObject(response);
                            send(reply);
                        }

                        if (o instanceof CallCenterConfirmTaxi) {
                            System.out.println("Taxi has got confirmation of drive");
                        }
                    } catch (UnreadableException | IOException e) {
                        e.printStackTrace();
                    }


                }
                block();
            }
        });
        ACLMessage register = new ACLMessage(ACLMessage.INFORM);
        register.addReceiver(new AID("callCenter", AID.ISLOCALNAME));
        try {
            register.setContentObject(new TaxiRegister());
            send(register);
        } catch (IOException e) {
            e.printStackTrace();
        }
//coinnego
    }

    public static void main(String[] args) throws StaleProxyException, IOException {
        // Get a hold on JADE runtime
        Runtime rt = Runtime.instance();
        // Create a default profile
        Profile p = new ProfileImpl();
        // Create a new non-main container, connecting to the default
        // main container (i.e. on this host, port 1099)
        ContainerController cc = rt.createAgentContainer(p);
        // Create a new agent, a DummyAgent
        // and pass it a reference to an Object

        Object[][] taxisData = new Object[][]{
                {new Position(22, -44), new Position(-33, -33), true, true, "combi", 4, true, true, "free", 5.5, 150, 20, 40},
                {new Position(12, 44), new Position(-33, -33), true, true, "combi", 8, true, true, "free", 5.5, 150, 20, 40},
                {new Position(32, 44), new Position(-33, -33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40},
                {new Position(42, -44), new Position(-33, -33), true, true, "vip", 3, true, true, "free", 5.5, 150, 20, 40},
                {new Position(52, 44), new Position(-33, -33), true, true, "sedan", 3, true, true, "free", 5.5, 150, 20, 40},
                {new Position(62, 44), new Position(-33, -33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40},
                {new Position(72, -44), new Position(-33, -33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40},
                {new Position(82, 44), new Position(-33, -33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40},
                {new Position(92, -44), new Position(-33, -33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40},
                {new Position(2, 44), new Position(-33, -33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40}};
        //Object reference = new Object();
        // Object aargs[] = new Object[1];
        //aargs[0]=reference;
        for (int i = 0; i < 1; i++) {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            AgentController dummy = cc.createNewAgent("taxi-" + System.currentTimeMillis(), "pl.edu.pw.elka.taxiAgents.TaxiAgent", taxisData[i]);
            // Fire up the agent
            dummy.start();
        }
    }
}