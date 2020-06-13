package pl.edu.pw.elka.taxiAgents;

import jade.core.*;
import jade.core.Runtime;
import jade.core.behaviours.*;

import jade.wrapper.AgentController;

import jade.lang.acl.*;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import pl.edu.pw.elka.taxiAgents.messages.CallCenterToTaxi;
import pl.edu.pw.elka.taxiAgents.messages.CallCenterConfirmTaxi;
import pl.edu.pw.elka.taxiAgents.messages.TaxiRegister;
import pl.edu.pw.elka.taxiAgents.messages.TaxiToCallCenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TaxiAgent extends Agent {

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

    boolean isMoving = false; ///< specifies if taxi is moving
    List<Position> route = new ArrayList<>(); ///< remaining route to destination


        void setupFromArgs(Object[] taxisData) {
            positionTaxiNow = (Position)(taxisData[0]);
            positionTaxiHome =(Position)(taxisData[1]);
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
        protected void setup()
        {
            setupFromArgs(getArguments());

            addBehaviour(new CyclicBehaviour(this)
            {

                public void action()
                {
                    ACLMessage msg = receive();
                    if (msg!=null) {
                        try {
                            Object o = msg.getContentObject();
                            if (o instanceof CallCenterToTaxi) {
                                CallCenterToTaxi cct=(CallCenterToTaxi) o;
                                int fromLongitudeCustomer = cct.getFrom().getLongitude();


                                double time = Math.abs(positionTaxiNow.longitude - fromLongitudeCustomer)* positionTaxiNow.latitude;
                                TaxiToCallCenter response;
                                if(time>10) {
                                    response=TaxiToCallCenter.reject(cct.getIdQuery());
                                } else {
                                    response=TaxiToCallCenter.accepts(positionTaxiNow.longitude,time,cct.getIdQuery());
                                }
                                System.out.println(" - " +
                                        myAgent.getLocalName() + " received: " +
                                        fromLongitudeCustomer+" time "+time + "accepts " +response.isIfAccepts() );

                                ACLMessage reply = msg.createReply();
                                reply.setPerformative(ACLMessage.INFORM);
                                reply.setContentObject(response);
                                send(reply);
                            }

                            if (o instanceof CallCenterConfirmTaxi)
                                {
                                    System.out.println("Taxi has got confirmation of drive");
                                }
                        } catch(UnreadableException | IOException e){
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
                    {new Position(22, -44), new Position(-33, -33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40},
                    {new Position(12, 44), new Position(-33, -33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40},
                    {new Position(32, 44), new Position(-33, -33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40},
                    {new Position(42, -44), new Position(-33, -33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40},
                    {new Position(52, 44), new Position(-33, -33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40},
                    {new Position(62, 44), new Position(-33, -33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40},
                    {new Position(72, -44), new Position(-33, -33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40},
                    {new Position(82, 44), new Position(-33, -33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40},
                    {new Position(92, -44), new Position(-33, -33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40},
                    {new Position(2, 44), new Position(-33, -33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40}};
            //Object reference = new Object();
           // Object aargs[] = new Object[1];
            //aargs[0]=reference;
            for (int i = 0; i < 10; i++) {
                try{
                    Thread.sleep(2);
                } catch (Exception e) {}
                AgentController dummy = cc.createNewAgent("taxi-" + System.currentTimeMillis(), "pl.edu.pw.elka.taxiAgents.TaxiAgent", taxisData[i]);


                // Fire up the agent
                dummy.start();
            }
        }

    class MovementBehaviour extends CyclicBehaviour
    {
        long delay; ///< specifies time between invocations of action method (in miliseconds)
        long previousActionTime = 0; ///< specifies time of last invocation of action; 0 if taxi has been not moving
        double speedInPointsPerMs = maximumSpeed; ///< speed but in points on the map per millisecond

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
                }
                else {
                    positionTaxiNow = route.get(0);
                    route.remove(0);
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

        double distanceBetweenTwoPoints(Position point1, Position point2) {
            return Math.sqrt(Math.pow(point1.longitude - point2.longitude, 2) + Math.pow(point1.latitude - point2.latitude, 2));
        }

        void destinationReached() { //TODO kogo informować?
            isMoving = false;
            previousActionTime = 0;
        }
    }
}

