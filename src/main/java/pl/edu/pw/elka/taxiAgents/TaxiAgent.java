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

public class TaxiAgent extends Agent {

    static int longitudeTaxiNow = 0;
    static int latitudeTaxiNow = 0;
    static int longitudeTaxiHome = 0;
    static int latitudeTaxiHome = 0;
    static boolean ifBabySeat;
    static boolean ifHomePet;
    static String kindOFCar;
    //enum kindOFCar {sedan, combi, van, vip};
    static int numberOFPassengers;
    static boolean ifStandByForSpecialTask;
    static boolean ifExperiencedDriver;
    static String driverStatus;
    static double workingTimeInThisDay;
    static double todayEarnings;
    static int timeFromLastClient;
    static double speed = 0;



        protected void setup()
        {


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
                                int longitudeCustomer = Integer.parseInt(cct.getFrom());


                                double time = Math.abs(longitudeTaxiNow - longitudeCustomer)* latitudeTaxiNow ;
                                TaxiToCallCenter response;
                                if(time>10) {
                                    response=TaxiToCallCenter.reject(cct.getIdQuery());
                                } else {
                                    response=TaxiToCallCenter.accepts(longitudeTaxiNow,time,cct.getIdQuery());
                                }
                                System.out.println(" - " +
                                        myAgent.getLocalName() + " received: " +
                                        longitudeCustomer+" time "+time + "accepts " +response.isIfAccepts() );

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
//coinnego
        }


        // zrobic maina zrobic maina , tylko on nie musi wysylac zapytan a tylko wystartowac agenta taksowke
       // ( lub w petli kilka dla wygody)
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
                    {22, -33, 10, 10, true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40},
                    {22, 2, 10, 10, true, true, "sedan", 8, false, false, "free", 5.5, 150, 20, 40},
                    {22, 20, 10, 10, true, true, "combi", 8, true, false, "free", 5.5, 150, 20, 40},
                    {30, 30, 10, 10, true, true, "vip", 8, false, false, "free", 5.5, 150, 20, 40},
                    {22, 8, 10, 10, true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40},
                    {40, -33, 10, 10, true, true, "combi", 8, false, true, "free", 5.5, 150, 20, 40},
                    {22, 5, 10, 10, true, true, "sedan", 8, false, false, "free", 5.5, 150, 20, 40},
                    {60, -33, 10, 10, true, true, "sedan", 8, true, true, "free", 5.5, 150, 20, 40},
                    {22, 80, 10, 10, true, true, "combi", 8, false, false, "free", 5.5, 150, 20, 40},
                    {22, -33, 10, 10, true, true, "sedan", 8, true, true, "free", 5.5, 150, 20, 40}};
            Object reference = new Object();
            Object aargs[] = new Object[1];
            aargs[0]=reference;
            for (int i = 0; i < 10; i++) {
                AgentController dummy = cc.createNewAgent("taxi-" + System.currentTimeMillis(), "pl.edu.pw.elka.taxiAgents.TaxiAgent", taxisData);
                longitudeTaxiNow = (int) taxisData[i][0];
                latitudeTaxiNow = (int) taxisData[i][1];
                longitudeTaxiHome = (int) taxisData[i][2];
                latitudeTaxiHome = (int) taxisData[i][3];
                ifBabySeat = (boolean) taxisData[i][4];
                ifHomePet = (boolean) taxisData[i][5];
                kindOFCar = (String) taxisData[i][6];
                numberOFPassengers = (int) taxisData[i][7];
                ifStandByForSpecialTask = (boolean) taxisData[i][8];
                ifExperiencedDriver = (boolean) taxisData[i][9];
                driverStatus = (String) taxisData[i][10];
                workingTimeInThisDay = (double) taxisData[i][11];
                todayEarnings = (int) taxisData[i][12];
                timeFromLastClient = (int) taxisData[i][13];
                speed = (int) taxisData[i][14];

                // Fire up the agent
                dummy.start();
            }
        }
    }