package pl.edu.pw.elka.taxiAgents;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class TestCommonInit {

    protected AgentController acClient;
    protected Runtime rt;
    protected String[] taxisNames = new String[0];


    @BeforeAll
    public void setup() throws StaleProxyException {
        // Get a hold on JADE runtime
        rt = Runtime.instance();
        // Create a default profile
        Profile p = new ProfileImpl();
        // Create a new non-main container, connecting to the default
        rt.createMainContainer(new ProfileImpl(true));
        ContainerController ccAgent = rt.createAgentContainer(p);
        // Create a new agent

        AgentController ac = ccAgent.createNewAgent("CallCenter", "pl.edu.pw.elka.taxiAgents.CallCenter", new Object[0]);
        ac.start();

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ccAgent.createNewAgent("display", "pl.edu.pw.elka.taxiAgents.TestDisplay", new Object[0]).start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        initTaxis(ccAgent);

        acClient = ccAgent.createNewAgent("client-" + System.currentTimeMillis(), "pl.edu.pw.elka.taxiAgents.TestClient", new Object[0]);
        acClient.start();
    }


    protected Object[][] getTaxisData() {
        return new Object[][]{
                {new Position(860, 860), new Position(33, 33), true, true, "van", 8, true, true, "free", 4.5, 200, 5, 5 * 1000, 600},
                {new Position(860, 860), new Position(33, 33), true, true, "van", 8, true, true, "free", 4.5, 200, 10, 25 * 1000, 600},};

    }

    void initTaxis(ContainerController ccAgent) throws StaleProxyException {
        Object[][] taxisData = getTaxisData();
        taxisNames = new String[taxisData.length];
        for (int i = 0; i < taxisData.length; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            taxisNames[i] = " " + i;
            AgentController dummy = ccAgent.createNewAgent(taxisNames[i], "pl.edu.pw.elka.taxiAgents.TaxiAgent", taxisData[i]);
            // Fire up the agent
            dummy.start();
        }
    }


    @AfterAll
    public void shutdown() {
        rt.shutDown();
    }

}
