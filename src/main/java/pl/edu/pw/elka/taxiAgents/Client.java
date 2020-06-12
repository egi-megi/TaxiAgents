package pl.edu.pw.elka.taxiAgents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.ContainerController;
import jade.core.Runtime;
import jade.wrapper.*;
import pl.edu.pw.elka.taxiAgents.messages.CallCenterToClient;
import pl.edu.pw.elka.taxiAgents.messages.CallTaxi;
import pl.edu.pw.elka.taxiAgents.messages.TaxiRegister;
import pl.edu.pw.elka.taxiAgents.messages.TaxiToCallCenter;

import java.io.IOException;
import java.util.LinkedList;

public class Client extends Agent implements ClientI {

    public Client() {
        registerO2AInterface(ClientI.class,this);
    }



    protected void setup()
    {


        // First set-up answering behaviour

        addBehaviour(new CyclicBehaviour(this)
        {
            public void action() {
                // Send messages to "a1" and "a2"

                ACLMessage msgI = receive();
                if (msgI != null) {
                    try {
                        Object mesg = msgI.getContentObject();
                        if (mesg instanceof CallCenterToClient) {
                            CallCenterToClient ct = (CallCenterToClient) mesg;
                            System.out.println("Przyjeżdża po mnie taksówka: " + ct.getTaxiName() + " za " + ct.getTimeToPickUp() + " min.");
                        }
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                    /**if (msgI!=null) {

                     System.out.println("== Answer to Client" + " <- "
                     + msgI.getContent() + " from "
                     + msgI.getSender().getName());
                     }
                     block();*/
                }
            }
        });



    }


    @Override
    public String doQuery(String fromLongitude,
                          String fromLatitude,
                          String toLongitude,
                          String toLatitude,
                          boolean ifBabySeat,
                          boolean ifHomePet,
                          int numberOFPassengers,
                          String kindOfClient) throws IOException {

        ACLMessage msg= new ACLMessage(ACLMessage.INFORM);

        msg.addReceiver(new AID("callCenter", AID.ISLOCALNAME));

        CallTaxi ct=new CallTaxi(fromLongitude, fromLatitude, toLongitude, toLatitude, ifBabySeat, ifHomePet, numberOFPassengers, kindOfClient);
        msg.setContentObject(ct);
        send(msg);
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
        Object reference = new Object();
        Object aargs[] = new Object[1];
        aargs[0]=reference;
        AgentController dummy = cc.createNewAgent("client-"+System.currentTimeMillis(), "pl.edu.pw.elka.taxiAgents.Client", args);
        // Fire up the agent
        dummy.start();
        for (int i =0 ; i<100; i=i+10) {
            Object o=new Object();
            synchronized (o) {
                try {
                    o.wait(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            dummy.getO2AInterface(ClientI.class).doQuery("777", "22222", "44", "55",true, false, 4, "normal");
        }
    }

}
