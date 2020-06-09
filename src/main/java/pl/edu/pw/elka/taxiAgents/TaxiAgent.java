package pl.edu.pw.elka.taxiAgents;

import jade.core.Agent;
import jade.core.behaviours.*;

import jade.core.AID;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

import jade.lang.acl.*;

public class TaxiAgent extends Agent {

    String name = "taxi1" ;
    AID taxiAgent = new AID( name, AID.ISLOCALNAME );

        protected void setup()
        {
            addBehaviour(new CyclicBehaviour(this)
            {
                public void action()
                {
                    ACLMessage msg = receive();
                    if (msg!=null) {
                        System.out.println( " - " +
                                myAgent.getLocalName() + " received: " +
                                msg.getContent() );

                        ACLMessage reply = msg.createReply();
                        reply.setPerformative( ACLMessage.INFORM );
                        reply.setContent("Pong" );
                        send(reply);
                    }
                    block();
                }
            });
        }
    }