package pl.edu.pw.elka.taxiAgents;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;

//import jade.domain.AMSService;
//import jade.domain.FIPAAgentManagement.*;

import jade.lang.acl.*;

public class Customer extends Agent
{
    String name = "Customer" ;
    AID customer = new AID( name, AID.ISLOCALNAME );
    protected void setup()
    {
        // First set-up answering behaviour

        addBehaviour(new CyclicBehaviour(this)
        {
            public void action() {
                // Send messages to "a1" and "a2"

                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent( "Ping" );
                msg.addReceiver( new AID(  "taxi1", AID.ISLOCALNAME) );

                send(msg);
                ACLMessage msgR= receive();
                if (msgR!=null)
                    System.out.println( "== Answer" + " <- "
                            +  msgR.getContent() + " from "
                            +  msgR.getSender().getName() );
                block();
            }
        });



    }
}