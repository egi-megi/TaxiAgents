package pl.edu.pw.elka.taxiAgents;

        import jade.core.Profile;
        import jade.core.ProfileImpl;
        import jade.core.Runtime;
        import jade.lang.acl.ACLMessage;
        import jade.lang.acl.UnreadableException;
        import jade.wrapper.AgentController;
        import jade.wrapper.ContainerController;
        import jade.wrapper.StaleProxyException;

        import org.junit.jupiter.api.*;
        import pl.edu.pw.elka.taxiAgents.messages.*;

        import java.io.IOException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DifferentTimeFromLastOrder {
    AgentController acClient;
    Runtime rt;
    String[] taxisNames=new String[0];
    @BeforeAll
    public void setup() throws StaleProxyException {
        // Get a hold on JADE runtime
        rt = Runtime.instance();
        // Create a default profile


        Profile p = new ProfileImpl();
        // Create a new non-main container, connecting to the default
        // main container (i.e. on this host, port 1099)
        rt.createMainContainer(new ProfileImpl(true));
        ContainerController ccAgent = rt.createAgentContainer(p);
        // Create a new agent, a DummyAgent
        // and pass it a reference to an Object

        AgentController ac=ccAgent.createNewAgent("CallCenter","pl.edu.pw.elka.taxiAgents.CallCenter",new Object[0]);
        ac.start();


        Object[][] taxisData = new Object[][]{
                {new Position(760, 760), new Position(33, 33), true, true, "van", 8, true, true, "free", 4.5, 200, 5, 0, 600},
                {new Position(760, 760), new Position(33, 33), true, true, "van", 8, true, true, "free", 4.5, 200, 5, 27, 600},};

        taxisNames=new String[taxisData.length];
        for (int i = 0; i < 2; i++) {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            taxisNames[i]=" " + i;
            AgentController dummy = ccAgent.createNewAgent(taxisNames[i], "pl.edu.pw.elka.taxiAgents.TaxiAgent", taxisData[i]);
            // Fire up the agent
            dummy.start();
        }

        // ContainerController ccClient = rt.createAgentContainer(p);
        acClient = ccAgent.createNewAgent("client-" + System.currentTimeMillis(), "pl.edu.pw.elka.taxiAgents.TestClient", new Object[0]);
        acClient.start();
    }

    @AfterAll
    public void shutdown(){
        rt.shutDown();
    }



    @Test
    public void quryingCallCenterForClientLastOrderTest() throws StaleProxyException, IOException, InterruptedException, UnreadableException {

        //Check that if drivers have different time form last order, system choose driver who has bigger time
        CallTaxi cct = new CallTaxi(new Position(1000, 1000), new Position(1010, 1010), false, false, false, 1, "normal");
        ACLMessage message = acClient.getO2AInterface(ITestClient.class).runMessage("CallCenter", cct);
        CallCenterToClient cctc = (CallCenterToClient) message.getContentObject();
        Assertions.assertEquals("1", cctc.getTaxiName().split("@")[0], "Taxi name should be 1 (name are from 0 not form 1) because it hase longer break form last order.");

    }


}
