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

    String name = "taxi1" ;
    AID taxiAgent = new AID( name, AID.ISLOCALNAME );

    int longitudeTaxiNow = 0;
    int latitudeTaxiNow = 0;
    double speed = 0;
    int longitudeTaxiHome = 0;
    int latitudeTaxiHome = 0;
    boolean ifBabySeat;
    boolean ifHomePet;
    String kindOFCar;
    int numberOFPassengers;
    double taxiFare;
    boolean ifExperiencedDriver;
    String driverStatus;
    double workingTimeInThisDay;
    double todayEarnings;
    int timeFromLasClient;




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


                                double time = Math.abs(longitudeTaxiNow - longitudeCustomer) ;
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
            Object reference = new Object();
            Object aargs[] = new Object[1];
            aargs[0]=reference;
            AgentController dummy = cc.createNewAgent("taxi-"+System.currentTimeMillis(), "pl.edu.pw.elka.taxiAgents.TaxiAgent", args);
            // Fire up the agent
            dummy.start();
        }
    }