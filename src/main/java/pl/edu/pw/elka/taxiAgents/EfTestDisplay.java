package pl.edu.pw.elka.taxiAgents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import pl.edu.pw.elka.taxiAgents.messages.TaxiStatus;

public class EfTestDisplay extends Agent {


    protected void setup() {

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {
                ACLMessage msgI;
                while ((msgI=receive()) != null) {
                    try {
                        Object msg = msgI.getContentObject();
                        if (msg instanceof TaxiStatus) {
                            TaxiStatus status = (TaxiStatus) msg;
                        }
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                }
                block();
            }
        });
    }

}
