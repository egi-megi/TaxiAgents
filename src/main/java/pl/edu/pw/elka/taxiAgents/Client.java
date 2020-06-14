package pl.edu.pw.elka.taxiAgents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.ContainerController;
import jade.core.Runtime;
import jade.wrapper.*;
import pl.edu.pw.elka.taxiAgents.messages.*;

import java.io.IOException;
import java.util.LinkedList;

public class Client extends Agent implements ClientI {
    Position startPosition;

    public Client() {
        registerO2AInterface(ClientI.class,this);
    }

    protected void setup()
    {

        addBehaviour(new CyclicBehaviour(this)
        {
            public void action() {

                ACLMessage msgI = receive();
                if (msgI != null) {
                    try {
                        Object mesg = msgI.getContentObject();
                        if (mesg instanceof CallCenterToClient) {
                            CallCenterToClient ct = (CallCenterToClient) mesg;
                            if(isDisplayAgentPresent()) sendStatusToDisplay(startPosition, true, ct.getTimeToPickUp().longValue());
                            System.out.println("Taxi: " + ct.getTaxiName() + "pick me up for " + ct.getTimeToPickUp() + " sec.");
                        }
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                    /**if (msgI!=null) {

                     System.out.println("== Answer to Client" + " <- "
                     + msgI.getContent() + " from "
                     + msgI.getSender().getName());
                     }*/
                     block();
                }
            }
        });

    }

    void sendStatusToDisplay(Position clientPosition, boolean isTaxiAssigned, long timeToPickup) {
        ACLMessage status = new ACLMessage(ACLMessage.INFORM);
        status.addReceiver(new AID("display", AID.ISLOCALNAME));
        ClientStatus message = new ClientStatus(clientPosition, isTaxiAssigned, timeToPickup);
        try {
            status.setContentObject(message);
            send(status);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean isDisplayAgentPresent() {
        AMSAgentDescription description = new AMSAgentDescription();
        description.setName(new AID("display", AID.ISLOCALNAME));
        AMSAgentDescription[] foundAgents = null;
        try {
            foundAgents = AMSService.search(this, description);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        return (foundAgents != null && foundAgents.length > 0);
    }


    @Override
    public String doQuery(Position from,
                          Position to,
                          boolean ifBabySeat,
                          boolean ifHomePet,
                          boolean ifLargeLuggage,
                          int numberOFPassengers,
                          String kindOfClient) throws IOException {

        startPosition = from;

        ACLMessage msg= new ACLMessage(ACLMessage.INFORM);

        msg.addReceiver(new AID("callCenter", AID.ISLOCALNAME));

        CallTaxi ct=new CallTaxi(from, to, ifBabySeat, ifHomePet, ifLargeLuggage, numberOFPassengers, kindOfClient);
        msg.setContentObject(ct);
        send(msg);
        System.out.println("Asking about picking me up.");

        if(isDisplayAgentPresent()) sendStatusToDisplay(startPosition, false, Long.MAX_VALUE);
        return null;
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
        //Object reference = new Object();
        //Object aargs[] = new Object[1];
        //aargs[0]=reference;
        AgentController acClient = cc.createNewAgent("client-"+System.currentTimeMillis(), "pl.edu.pw.elka.taxiAgents.Client", args);
        // Fire up the agent
        acClient.start();
        for (int i =0 ; i<10; i=i+10) {
            Object o=new Object();
            synchronized (o) {
                try {
                    o.wait(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            acClient.getO2AInterface(ClientI.class).doQuery(new Position(1000, 1000), new Position(1050, 1050), false, false, false, 1, "normal");
        }
    }

}
