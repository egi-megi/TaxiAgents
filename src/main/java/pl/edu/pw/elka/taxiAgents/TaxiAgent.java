package pl.edu.pw.elka.taxiAgents;

import jade.core.Agent;
import jade.core.behaviours.*;

import jade.core.AID;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import jade.lang.acl.*;
import pl.edu.pw.elka.taxiAgents.messages.CallCenterToTaxi;
import pl.edu.pw.elka.taxiAgents.messages.TaxiRegister;
import pl.edu.pw.elka.taxiAgents.messages.TaxiToCallCenter;

import java.io.IOException;

public class TaxiAgent extends Agent {

    String name = "taxi1" ;
    AID taxiAgent = new AID( name, AID.ISLOCALNAME );

    int longitudeTaxi = 45;

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


                                double time = Math.abs(longitudeTaxi - longitudeCustomer) ;
                                TaxiToCallCenter response;
                                if(time>10) {
                                    response=TaxiToCallCenter.reject(cct.getIdQuery());
                                } else {
                                    response=TaxiToCallCenter.accepts(longitudeTaxi,time,cct.getIdQuery());
                                }
                                System.out.println(" - " +
                                        myAgent.getLocalName() + " received: " +
                                        longitudeCustomer+" time "+time + "accepts " +response.isIfAccepts() );

                                ACLMessage reply = msg.createReply();
                                reply.setPerformative(ACLMessage.INFORM);
                                reply.setContentObject(response);
                                send(reply);
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
    }