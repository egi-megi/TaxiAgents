package pl.edu.pw.elka.taxiAgents;

import jade.core.*;
import jade.core.Runtime;
import jade.core.behaviours.*;

import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAException;
import jade.wrapper.AgentContainer;
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

    public final static String DRIVER_STATUS_FREE = "free";
    public final static String DRIVER_STATUS_NEAR_END = "nearEnd";
    public final static String DRIVER_STATUS_GOES_HOME = "goesHome";
    public final static String DRIVER_STATUS_WORKING = "working";
    public final static String DRIVER_STATUS_UNAVAILABLE = "unavailable";
    public final static String DRIVER_STATUS_BREAK = "break";
    public final static String DRIVER_STATUS_VEHICLE_BREAKDOWN = "vehicleBreakdown";

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
    int workingTime; // time in seconds

    long endWorkingTime; // time in milisec

    double maximumSpeed = 0; ///< points per second
    double distanceToClient;
    double distanceWithClient;
    double timeToPickUpClient;
    double priceForAllDistance;
    double timeWithClient;


    boolean isRidingWithClient = false; ///< specifies if client is in a taxi
    boolean isMoving = false; ///< specifies if taxi is moving
    Position clientDestination;
    Position clientStartPoint;
    List<Position> route = new ArrayList<>(); ///< remaining route to destination

    boolean isDisplayAgentPresent = false;

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
        workingTime = (int) taxisData[13];
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

    boolean canNotTakeOrderDriverStatus() {
        return driverStatus.equalsIgnoreCase(DRIVER_STATUS_BREAK) || driverStatus.equalsIgnoreCase(DRIVER_STATUS_UNAVAILABLE) || driverStatus.equalsIgnoreCase(DRIVER_STATUS_VEHICLE_BREAKDOWN) || driverStatus.equalsIgnoreCase(DRIVER_STATUS_WORKING);
    }

    /*double computeDistance(CallCenterToTaxi cct) {
        distanceToClient = (Math.abs(positionTaxiNow.longitude - cct.getTo().getLongitude()) + Math.abs(positionTaxiNow.latitude - cct.getTo().getLatitude())) * 0.01;
        return distanceToClient;
    }*/

    double computeDistance(Position pFrom, Position pTo) {
        return (Math.abs(pFrom.longitude - pTo.longitude) + Math.abs(pFrom.latitude - pTo.latitude));
    }

    boolean queryOnWayToHome(Position pTo) {
        if(computeDistance(positionTaxiNow, positionTaxiHome) > 2*computeDistance(pTo, positionTaxiHome)){
            return true;}
        else return false;
    }

    double computeTime(double distance) {
        double avarageDriverSpeed;
        if (ifExperiencedDriver) {
            avarageDriverSpeed = 40.0;
        } else {
            avarageDriverSpeed = 30.0;
        }
        return (distance / avarageDriverSpeed) + timeToEndOrder;
    }

    double computeTimeWithTimeToEndOrder(double distance) {
        return computeTime(distance) + timeToEndOrder;
    }


    double computePrice(double distance, String kindOfClient) {
        double pricePerKilometer;
        if (kindOfClient.equalsIgnoreCase("vip")) {
            pricePerKilometer = 2.3;
        } else if (kindOfClient.equalsIgnoreCase("korpo")) {
            pricePerKilometer = 2.5;
        } else {
            pricePerKilometer = 3.0;
        }
        priceForAllDistance = distance * pricePerKilometer;
            return priceForAllDistance;
    }

    long computeTimeToGoHome(long workingTime) {
        long currentTime = System.currentTimeMillis();
        endWorkingTime = currentTime + workingTime*1000;
        return endWorkingTime;
    }

    protected void setup() {
        setupFromArgs(getArguments());

        if (ifExperiencedDriver) {
            maximumSpeed = 40.0;
        } else {
            maximumSpeed = 30.0;
        }

        long movingDelay = 1000;
        long schedulerDelay = 500;
        long statusSenderDelay = 1000;
        long displayAgentCheckerDelay = 3000;

        addBehaviour(new MovementBehaviour(this, movingDelay));
        addBehaviour(new TaskScheduler(this, schedulerDelay));
        addBehaviour(new DisplayAgentPresenceChecker(this, displayAgentCheckerDelay));
        addBehaviour(new StatusSender(this, statusSenderDelay));

        addBehaviour(new CyclicBehaviour(this)
        {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    try {
                        Object o = msg.getContentObject();
                        if (o instanceof CallCenterToTaxi) {
                            CallCenterToTaxi cct = (CallCenterToTaxi) o;
                            if (isMissingBabySeat(cct) ||
                                    canNotTakePet(cct) ||
                                    tooMuchPassengers(cct) ||
                                    canNotTakeLuggage(cct) ||
                                    canNotTakeOrderDriverStatus()
                            ) {
                                //response = TaxiToCallCenter.reject(cct.getIdQuery());
                            } else {
                                distanceToClient = computeDistance(positionTaxiNow, cct.getFrom());
                                if (distanceToClient > 200 && ifStandByForSpecialTask == false) {
                                    //response = TaxiToCallCenter.reject(cct.getIdQuery());
                                } else if (driverStatus == DRIVER_STATUS_GOES_HOME && !queryOnWayToHome(cct.getTo())) {
                                    //response = TaxiToCallCenter.reject(cct.getIdQuery());
                                } else {
                                    long leftTimeToGoHome = (endWorkingTime - System.currentTimeMillis()) / 1000;
                                    if (leftTimeToGoHome < 30) {
                                        driverStatus.equalsIgnoreCase(DRIVER_STATUS_GOES_HOME);
                                    }
                                    if (leftTimeToGoHome <= 0) {
                                        driverStatus.equalsIgnoreCase(DRIVER_STATUS_UNAVAILABLE);
                                    }
                                    timeToPickUpClient = computeTimeWithTimeToEndOrder(distanceToClient);
                                    distanceWithClient = computeDistance(cct.getFrom(), cct.getTo());
                                    priceForAllDistance = computePrice(distanceWithClient, cct.getKindOfClient());
                                    TaxiToCallCenter response;
                                    response = TaxiToCallCenter.accepts(positionTaxiHome, kindOFCar, workingTimeInThisDay, todayEarnings, timeFromLastClient, driverStatus, distanceToClient, timeToPickUpClient, priceForAllDistance, cct.getIdQuery());
                                    ACLMessage reply = msg.createReply();
                                    reply.setPerformative(ACLMessage.INFORM);
                                    reply.setContentObject(response);
                                    send(reply);
                                }
                            }

                        }

                        if (o instanceof CallCenterConfirmTaxi) {
                            CallCenterConfirmTaxi ccct = (CallCenterConfirmTaxi) o;
                            TaxiConfirmTakingOrder response;
                            if (driverStatus.equalsIgnoreCase("free") || driverStatus.equalsIgnoreCase("nearEnd") || driverStatus.equalsIgnoreCase("goesHome")) {
                                response = new TaxiConfirmTakingOrder(ccct.getIdQuery(), true);
                            } else {
                                response = new TaxiConfirmTakingOrder(ccct.getIdQuery(), false);
                            }
                            ACLMessage reply = msg.createReply();
                            reply.setPerformative(ACLMessage.INFORM);
                            reply.setContentObject(response);
                            send(reply);
                            distanceToClient = computeDistance(positionTaxiNow, ccct.getFrom());
                            timeToPickUpClient = computeTimeWithTimeToEndOrder(distanceToClient);
                            distanceWithClient = computeDistance(ccct.getFrom(), ccct.getTo());
                            timeWithClient = computeTime(distanceWithClient);
                            priceForAllDistance = computePrice(distanceWithClient, ccct.getKindOfClient());
                            workingTimeInThisDay = workingTimeInThisDay + timeToPickUpClient + timeWithClient - timeToEndOrder;
                            todayEarnings = todayEarnings + priceForAllDistance;
                            clientStartPoint = ccct.getFrom();
                            clientDestination = ccct.getTo();
                            System.out.println("The taxi finally confirms that the client has been picked up for " + timeToPickUpClient + " sec.");
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
            endWorkingTime = computeTimeToGoHome(workingTime);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
     * Method that calculates time required to travel along given route.
     * @param startPoint First point of the route.
     * @param nextPoints Remaining part of the route.
     * @return Time in seconds, required to travel from start to end.
     */
    long calculateRouteTime(Position startPoint, List<Position> nextPoints) {
        if(maximumSpeed == 0) return Long.MAX_VALUE;
        long result = 0;
        Position currentPoint = startPoint;
        for(Position nextPoint: nextPoints) {
            double sectionLength = distanceBetweenTwoPoints(currentPoint, nextPoint);
            result = result + (long)(sectionLength / maximumSpeed);
            currentPoint = nextPoint;
        }
        return result;
    }

    /**
     * Overloaded method createRoad.
     * @param startPoint Start of the journey.
     * @param finishPoint Last point of the journey.
     * @return The list of points without start point.
     */
    List<Position> createRoute(Position startPoint, Position finishPoint) {
        return createRoute(startPoint, finishPoint, true);
    }

    /**
     * Method that creates route from start point to end point. Star point is not included in a route. If start point is
     * the same as end point, route consist of only one point (end point). Otherwise, it consist of two points: middle
     * point and finish point. Position of middle point depends on argument isLongitudeFirst. If it is true, middle
     * point's latitude is the same as start point, longitude same as finish point. If false - otherwise.
     * @param startPoint Start of the journey.
     * @param finishPoint Last point of the journey.
     * @param isLongitudeFirst Tells if travel starts along longitude or latitude.
     * @return The list of points without start point.
     */
    List<Position> createRoute(Position startPoint, Position finishPoint, boolean isLongitudeFirst) {
        List<Position> result = new ArrayList<>();
        if(startPoint == finishPoint) result.add(finishPoint);
        else {
            if(isLongitudeFirst) result.add(new Position(finishPoint.longitude, startPoint.latitude));
            else result.add(new Position(startPoint.longitude, finishPoint.latitude));
            result.add(finishPoint);
        }
        return result;
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
                {new Position(500, 500), new Position(33, 33), true, true, "combi", 4, true, true, "free", 5.5, 150, 20, 40, 60},
                {new Position(950, 950), new Position(33, 33), true, true, "combi", 8, true, true, "free", 5.5, 150, 20, 40, 160},
                {new Position(32, 44), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40, 8*60},
                {new Position(42, 44), new Position(33, 33), true, true, "vip", 3, true, true, "free", 5.5, 150, 20, 40, 4*60},
                {new Position(52, 44), new Position(33, 33), true, true, "sedan", 3, true, true, "free", 5.5, 150, 20, 40, 8*60},
                {new Position(62, 44), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40, 5*60},
                {new Position(72, 44), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40, 8*60},
                {new Position(82, 44), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40, 8*60},
                {new Position(92, 44), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40, 8*60},
                {new Position(2, 44), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40, 8*60}};
        //Object reference = new Object();
        // Object aargs[] = new Object[1];
        //aargs[0]=reference;
        for (int i = 0; i < 2; i++) {
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
            //System.out.println("Taxi has left " + ((endWorkingTime - System.currentTimeMillis())/1000) + " sec to the end of the working day.");
            if(previousActionTime != 0) { // if previous time == 0 there is nothing to calculate
                long millisecondsPassed = currentTime - previousActionTime;
                double quantumOfTraveledDistance = speedInPointsPerMs * millisecondsPassed;
                double distanceToNextPoint = distanceBetweenTwoPoints(route.get(0), positionTaxiNow);
                if(quantumOfTraveledDistance < distanceToNextPoint) { //if next point is not reached (it implies that distanceToNextPoint > 0)
                    positionTaxiNow.latitude = (int)(positionTaxiNow.latitude + quantumOfTraveledDistance / distanceToNextPoint * (route.get(0).latitude - positionTaxiNow.latitude));
                    positionTaxiNow.longitude = (int)(positionTaxiNow.longitude + quantumOfTraveledDistance / distanceToNextPoint * (route.get(0).longitude - positionTaxiNow.longitude));
                    System.out.println(" - " + myAgent.getLocalName() + " moving: " + positionTaxiNow.longitude + " " + positionTaxiNow.latitude);

                }
                else { // next point is reached in this move
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
         * Method invoked when destination is reached. It stops movement, resets previousActionTime, and informs others about the event
         */
        void destinationReached() { //TODO kogo informować?
            isMoving = false;
            previousActionTime = 0;
            System.out.println(" - " + myAgent.getLocalName() + " destination reached: " + positionTaxiNow.longitude + " " + positionTaxiNow.latitude);
        }
    }


    /**
     * Class that tells taxi to move and checks if it reached destination.
     */
    class TaskScheduler extends CyclicBehaviour {
        long delay; ///< specifies time between invocations of action method (in milliseconds)
        long timeOfLastJobEnd = System.currentTimeMillis();

        TaskScheduler(Agent a, long delay) {
            super(a);
            this.delay = delay;
        }

        public void action() {
            timeToEndOrder = 0;
            timeFromLastClient = 0;

            //things to do if driver is working
            if(driverStatus.equals(DRIVER_STATUS_WORKING)) {
                //If driver is working but not moving it means, he reached destination.
                if(!isMoving) {
                    //If he was riding with a client, he's now free and clears memory out of previous directions.
                    if(isRidingWithClient) {
                        driverStatus = DRIVER_STATUS_FREE;
                        isRidingWithClient = false;
                        clientStartPoint = null;
                        clientDestination = null;
                        timeOfLastJobEnd = System.currentTimeMillis();
                        System.out.println(" - " + myAgent.getLocalName() + " job done");
                    }
                    //Otherwise, it's time to pick up a client.
                    else {
                        route = createRoute(positionTaxiNow, clientDestination);
                        isRidingWithClient = true;
                        isMoving = true;
                        timeToEndOrder = calculateRouteTime(positionTaxiNow, route);
                        System.out.println(" - " + myAgent.getLocalName() + " picking up the client");
                    }
                }
                //Otherwise driver is moving and time to end must be calculated
                else {
                    timeToEndOrder = calculateRouteTime(positionTaxiNow, route);
                    //If driver is going for a client, ride with a client must be included.
                    if(!isRidingWithClient) timeToEndOrder = timeToEndOrder +
                            calculateRouteTime(clientStartPoint, createRoute(clientStartPoint, clientDestination));
                }
            }

            //Things to do when driver is free.
            else if(driverStatus.equals(DRIVER_STATUS_FREE)) {
                //If clientDestination and clientStartPoint is not null, it's time to go.
                if (clientDestination != null && clientStartPoint != null) {
                    driverStatus = DRIVER_STATUS_WORKING;
                    route = createRoute(positionTaxiNow, clientStartPoint);
                    isRidingWithClient = false;
                    isMoving = true;
                    System.out.println(" - " + myAgent.getLocalName() + " starting job - going for a client");
                } else {
                    timeFromLastClient = (int) (System.currentTimeMillis() - timeOfLastJobEnd) / 1000; //in seconds
//                    System.out.println(" - " + myAgent.getLocalName() + " time since last job: " + timeFromLastClient);
                }
            }

            block(delay);
        }
    }

    class StatusSender extends CyclicBehaviour {
        long delay; ///< specifies time between invocations of action method (in milliseconds)

        public StatusSender(Agent a, long delay) {
            super(a);
            this.delay = delay;
        }

        public void action() {
            if(isDisplayAgentPresent) {
                ACLMessage status = new ACLMessage(ACLMessage.INFORM);
                status.addReceiver(new AID("display", AID.ISLOCALNAME));
                TaxiStatus message = new TaxiStatus(driverStatus, positionTaxiNow, route, isRidingWithClient);
            try {
                status.setContentObject(message);
                send(status);
            } catch (IOException e) {
                e.printStackTrace();
            }
            }
            block(delay);
        }
    }

    class DisplayAgentPresenceChecker extends CyclicBehaviour {
        long delay; ///< specifies time between invocations of action method (in milliseconds)
        boolean toggleAction;
        AMSAgentDescription description;

        public DisplayAgentPresenceChecker(Agent a, long delay) {
            super(a);
            this.delay = delay;
            description = new AMSAgentDescription();
            description.setName(new AID("display", AID.ISLOCALNAME));
        }

        public void action() {
            toggleAction = !toggleAction;
            if(toggleAction) {
                AMSAgentDescription[] foundAgents = null;
                try {
                    foundAgents = AMSService.search(myAgent, description);
                } catch (FIPAException e) {
                    e.printStackTrace();
                }
                isDisplayAgentPresent = (foundAgents != null && foundAgents.length > 0);
            }
            block(delay);
        }
    }
}

