package pl.edu.pw.elka.taxiAgents;

import jade.core.*;
import jade.core.Runtime;
import jade.core.behaviours.*;

import jade.wrapper.AgentController;

import jade.lang.acl.*;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import pl.edu.pw.elka.taxiAgents.messages.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.*;

public class TaxiAgent extends Agent {

    final String KIND_OF_CARS_VAN = "van";
    final String KIND_OF_CARS_COMBI = "combi";
    final String KIND_OF_CARS_SEDAN = "sedan";
    final String KIND_OF_CARS_VIP = "vip";

    final String DRIVER_STATUS_FREE = "free";
    final String DRIVER_STATUS_NEAR_END = "nearEnd";
    final String DRIVER_STATUS_GOES_HOME = "goesHome";
    final String DRIVER_STATUS_WORKING = "working";
    final String DRIVER_STATUS_UNAVAILABLE = "unavailable";
    final String DRIVER_STATUS_BREAK = "break";
    final String DRIVER_STATUS_VEHICLE_BREAKDOWN = "vehicleBreakdown";

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

    double maximumSpeed = 0;
    double distanceToClient;
    double distanceWithClient;
    double timeToPickUpClient;
    double priceForAllDistance;


    boolean isMoving = false; ///< specifies if taxi is moving
    List<Position> route = new ArrayList<>(); ///< remaining route to destination

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

    boolean notWorkingDriver() {
        return driverStatus.equalsIgnoreCase(DRIVER_STATUS_BREAK) || driverStatus.equalsIgnoreCase(DRIVER_STATUS_UNAVAILABLE) || driverStatus.equalsIgnoreCase(DRIVER_STATUS_VEHICLE_BREAKDOWN);
    }

    double computeDistance(CallCenterToTaxi cct) {
        distanceToClient = (Math.abs(positionTaxiNow.longitude - cct.getTo().getLongitude()) + Math.abs(positionTaxiNow.latitude - cct.getTo().getLatitude())) * 0.01;
        return distanceToClient;
    }

    /*double computeDistance(Position pFrom, Position pTo) {
        return (Math.abs(pFrom.longitude - pTo.longitude) + Math.abs(pFrom.latitude - pTo.latitude)) * 0.001;
    }*/

    double computeTime(CallCenterToTaxi cct) {
        double avarageDriverSpeed;
        if (ifExperiencedDriver) {
            avarageDriverSpeed = 40.0;
        } else {
            avarageDriverSpeed = 30.0;
        }
        timeToPickUpClient = (distanceToClient / avarageDriverSpeed) + timeToEndOrder;
        return timeToPickUpClient;
    }


    double computePrice(CallCenterToTaxi cct) {
        double pricePerKilometer;
        if (cct.getKindOfClient().equalsIgnoreCase("vip")) {
            pricePerKilometer = 2.3;
        } else if (cct.getKindOfClient().equalsIgnoreCase("korpo")) {
            pricePerKilometer = 2.5;
        } else {
            pricePerKilometer = 3.0;
        }
        priceForAllDistance = distanceToClient * pricePerKilometer;
            return priceForAllDistance;
    }

    protected void setup() {
        setupFromArgs(getArguments());

            //TODO temp
            maximumSpeed = 5;
            route.add(new Position(2, 80));
            route.add(new Position(25, 80));
            route.add(new Position(25, 60));
            route.add(new Position(14, 60));
            route.add(new Position(-10, -10));
            route.add(new Position(20, 20));
            isMoving = true;
            long movingDelay = 1000;

            addBehaviour(new MovementBehaviour(this, movingDelay));

            addBehaviour(new CyclicBehaviour(this)
            {

            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    try {
                        Object o = msg.getContentObject();
                        if (o instanceof CallCenterToTaxi) {
                            CallCenterToTaxi cct = (CallCenterToTaxi) o;
                            int fromLongitudeCustomer = cct.getFrom().getLongitude();
                            //TaxiToCallCenter response;

                            if (isMissingBabySeat(cct) ||
                                    canNotTakePet(cct) ||
                                    tooMuchPassengers(cct) ||
                                    canNotTakeLuggage(cct) ||
                                    notWorkingDriver()
                            ) {
                                //response = TaxiToCallCenter.reject(cct.getIdQuery());
                            } else {
                                computeDistance(cct);
                                if (distanceToClient > 200 && ifStandByForSpecialTask == false) {
                                    //response = TaxiToCallCenter.reject(cct.getIdQuery());
                                } else {
                                    computeTime(cct);
                                    computePrice(cct);
                                    TaxiToCallCenter response;
                                    response = TaxiToCallCenter.accepts(positionTaxiHome, kindOFCar, workingTimeInThisDay, todayEarnings, timeFromLastClient, distanceToClient, timeToPickUpClient, priceForAllDistance, cct.getIdQuery());
                                    System.out.println(" - " +
                                            myAgent.getLocalName() + " odbierze klienta za: " + timeToPickUpClient + " min.");
                                    ACLMessage reply = msg.createReply();
                                    reply.setPerformative(ACLMessage.INFORM);
                                    reply.setContentObject(response);
                                    send(reply);
                                }
                            }


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
            ACLMessage register=new ACLMessage(ACLMessage.INFORM);
            register.addReceiver(new AID("callCenter", AID.ISLOCALNAME));
            try {
                register.setContentObject(new TaxiRegister());
                send(register);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                    {new Position(22, -44), new Position(-33, -33), true, true, "sedan", 4, true, true, "free", 5.5, 150, 20, 40},
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
                AgentController dummy = cc.createNewAgent("taxi-" + System.currentTimeMillis(), "pl.edu.pw.elka.taxiAgents.TaxiAgent", taxisData[i]);


                // Fire up the agent
                dummy.start();
            }
        }

    /** \class MovementBehaviour
     * \brief Class that is responsible for taxi movement. Taxi moves along given route. Route contains of points which
     * taxi is expected to reach. Taxi travels directly to the next point (along a line connecting current position and
     * the next point. When last point is reached, variable isMoving is set to false.
     * During the travel, points are being consequently removed from route.
     */
    class MovementBehaviour extends CyclicBehaviour
    {
        long delay; ///< specifies time between invocations of action method (in milliseconds)
        long previousActionTime = 0; ///< specifies time of last invocation of action; 0 if taxi has been not moving
        double speedInPointsPerMs = maximumSpeed / 1000; ///< speed but in points on the map per millisecond

        public MovementBehaviour(Agent a, long delay) {
            super(a);
            this.delay = delay;
        }

        public void action() {
            if(!isMoving || route.isEmpty()) { // if not moving or nowhere to go - do nothing
                previousActionTime = 0;
                block(delay); //TODO może da się tu zrobić oczekiwanie na wiadomość
                return;
            }

            long currentTime = System.currentTimeMillis();
            if(previousActionTime != 0) { // if previous time == 0 there is nothing to calculate
                long millisecondsPassed = currentTime - previousActionTime;
                double quantumOfTraveledDistance = speedInPointsPerMs * millisecondsPassed;
                double distanceToNextPoint = distanceBetweenTwoPoints(route.get(0), positionTaxiNow);
                if(quantumOfTraveledDistance < distanceToNextPoint) { //if next point is not reached (it implies that distanceToNextPoint > 0)
                    positionTaxiNow.latitude = (int)(positionTaxiNow.latitude + quantumOfTraveledDistance / distanceToNextPoint * (route.get(0).latitude - positionTaxiNow.latitude));
                    positionTaxiNow.longitude = (int)(positionTaxiNow.longitude + quantumOfTraveledDistance / distanceToNextPoint * (route.get(0).longitude - positionTaxiNow.longitude));
                    System.out.println(" - " + myAgent.getLocalName() + " moving: " + positionTaxiNow.longitude + " " + positionTaxiNow.latitude);

                }
                else {
                    positionTaxiNow = route.get(0);
                    route.remove(0);
                    System.out.println(" - " + myAgent.getLocalName() + " point reached: " + positionTaxiNow.longitude + " " + positionTaxiNow.latitude);
                    if (route.isEmpty()) {
                        destinationReached();
                        previousActionTime = 0;
                        block(delay);
                        return;
                    }
                }
            }
            previousActionTime = currentTime;
            block(delay);
        }

        /**
         * Method calculates distance between two points
         * @param point1 First point
         * @param point2 Second point
         * @return distance between points.
         */
        double distanceBetweenTwoPoints(Position point1, Position point2) {
            return Math.sqrt(Math.pow(point1.longitude - point2.longitude, 2) + Math.pow(point1.latitude - point2.latitude, 2));
        }

        /**
         * Method invoked when destination is reached. It stops movement, resets previousActionTime, and informs others about the event
         */
        void destinationReached() { //TODO kogo informować?
            isMoving = false;
            previousActionTime = 0;
            System.out.println(" - " + myAgent.getLocalName() + " destination reached: " + positionTaxiNow.longitude + " " + positionTaxiNow.latitude);
        }
    }
}

