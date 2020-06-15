package pl.edu.pw.elka.taxiAgents;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.io.IOException;

public class Demonstration {
    public static void main(String[] args) throws StaleProxyException {
        // Get a hold on JADE runtime
        Runtime rt = Runtime.instance();
        // Create a default profile
        Profile p = new ProfileImpl();
        // Create a new non-main container, connecting to the default main container
        ContainerController cc = rt.createAgentContainer(p);
        // Create a new agent, a DummyAgent
        // and pass it a reference to an Object
        Object[][] taxisData = new Object[][]{
                {new Position(960, 960), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 0, 0, 0, 8 * 60},
                {new Position(980, 980), new Position(33, 33), true, true, "sedan", 3, true, true, "free", 5.5, 0, 0, 0, 8 * 60},
                {new Position(990, 990), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 0, 0, 0, 8 * 60},
                {new Position(42, 44), new Position(33, 33), true, true, "vip", 3, true, true, "free", 5.5, 0, 0, 0, 4 * 60},
                {new Position(52, 44), new Position(33, 33), true, true, "sedan", 3, true, true, "free", 5.5, 0, 0, 0, 8 * 60},
                {new Position(62, 44), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 0, 0, 0, 5 * 60},
                {new Position(72, 44), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 0, 0, 0, 8 * 60},
                {new Position(82, 44), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 0, 0, 0, 8 * 60},
                {new Position(92, 44), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 0, 0, 0, 8 * 60},
                {new Position(2, 44), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 0, 0, 0, 8 * 60}};
        //Object reference = new Object();
        // Object aargs[] = new Object[1];
        //aargs[0]=reference;
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            AgentController dummy = cc.createNewAgent("taxi-" + System.currentTimeMillis(), "pl.edu.pw.elka.taxiAgents.TaxiAgent", taxisData[i]);
            // Fire up the agent
            dummy.start();
        }
    }
}
