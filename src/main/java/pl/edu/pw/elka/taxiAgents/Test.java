package pl.edu.pw.elka.taxiAgents;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.io.IOException;

public class Test {

    public static void main(String[] args) throws StaleProxyException, IOException {
        // Get a hold on JADE runtime
        Runtime rt = Runtime.instance();
        // Create a default profile
        Profile p = new ProfileImpl();
        // Create a new non-main container, connecting to the default
        // main container (i.e. on this host, port 1099)
        ContainerController ccAgent = rt.createAgentContainer(p);
        // Create a new agent, a DummyAgent
        // and pass it a reference to an Object

        Object[][] taxisData = new Object[][]{
                {new Position(500, 500), new Position(33, 33), true, true, "combi", 4, true, true, "free", 5.5, 150, 20, 40, 60},
                {new Position(950, 950), new Position(33, 33), true, true, "combi", 8, true, true, "free", 5.5, 150, 20, 40, 160},
                {new Position(32, 44), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40, 8 * 60},
                {new Position(42, 44), new Position(33, 33), true, true, "vip", 3, true, true, "free", 5.5, 150, 20, 40, 4 * 60},
                {new Position(52, 44), new Position(33, 33), true, true, "sedan", 3, true, true, "free", 5.5, 150, 20, 40, 8 * 60},
                {new Position(62, 44), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40, 5 * 60},
                {new Position(72, 44), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40, 8 * 60},
                {new Position(82, 44), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40, 8 * 60},
                {new Position(92, 44), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40, 8 * 60},
                {new Position(2, 44), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 150, 20, 40, 8 * 60}};
        //Object reference = new Object();
        // Object aargs[] = new Object[1];
        //aargs[0]=reference;
        for (int i = 0; i < 2; i++) {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            AgentController dummy = ccAgent.createNewAgent("taxi-" + System.currentTimeMillis(), "pl.edu.pw.elka.taxiAgents.TaxiAgent", taxisData[i]);
            // Fire up the agent
            dummy.start();
        }

        ContainerController ccClient = rt.createAgentContainer(p);
        // Create a new agent, a DummyAgent
        // and pass it a reference to an Object
        //Object reference = new Object();
        //Object aargs[] = new Object[1];
        //aargs[0]=reference;
        AgentController acClient = ccClient.createNewAgent("client-"+System.currentTimeMillis(), "pl.edu.pw.elka.taxiAgents.Client", args);
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
            acClient.getO2AInterface(ClientI.class).doQuery(new Position(1000, 1000), new Position(1100,1100), false, false,false, 1, "normal");
        }
    }
}
