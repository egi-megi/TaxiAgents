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
import pl.edu.pw.elka.taxiAgents.messages.CallCenterToTaxi;
import pl.edu.pw.elka.taxiAgents.messages.CallTaxi;
import pl.edu.pw.elka.taxiAgents.messages.TaxiConfirmTakingOrder;
import pl.edu.pw.elka.taxiAgents.messages.TaxiToCallCenter;

import java.io.IOException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OverallSystemTest {
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
            taxisNames=new String[taxisData.length];
            for (int i = 0; i < 2; i++) {
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                taxisNames[i]="taxi-" + System.currentTimeMillis();
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
    public void quryingTaxiForClientTest() throws StaleProxyException, IOException, InterruptedException, UnreadableException {
           // CallTaxi ct = new CallTaxi(new Position(1000, 1000), new Position(1100, 1100), false, false, false, 1, "normal");

           // ACLMessage message = acClient.getO2AInterface(ITestClient.class).runMessage("callCenter", ct);

            CallCenterToTaxi cct=new CallCenterToTaxi(new Position(1000, 1000), new Position(1100, 1100), false, false, false, 1, "normal","1");
            ACLMessage message = acClient.getO2AInterface(ITestClient.class).runMessage(taxisNames[0], cct);
            Assertions.assertTrue(message.getContentObject() instanceof TaxiToCallCenter);

            TaxiToCallCenter tcc=(TaxiToCallCenter) message.getContentObject();
            Assertions.assertEquals("1",tcc.getQueryID(),"Query id should 1 becuse we set it previously ");


    }



    @Test
    public void quryingCallCenterForClientTest() throws StaleProxyException, IOException, InterruptedException, UnreadableException {

            // CallTaxi ct = new CallTaxi(new Position(1000, 1000), new Position(1100, 1100), false, false, false, 1, "normal");

            // ACLMessage message = acClient.getO2AInterface(ITestClient.class).runMessage("callCenter", ct);

            CallCenterToTaxi cct=new CallCenterToTaxi(new Position(1000, 1000), new Position(1100, 1100), false, false, false, 1, "normal","1");
            ACLMessage message = acClient.getO2AInterface(ITestClient.class).runMessage(taxisNames[0], cct);
            Assertions.assertTrue(message.getContentObject() instanceof TaxiToCallCenter);

            TaxiToCallCenter tcc=(TaxiToCallCenter) message.getContentObject();
            Assertions.assertEquals("1",tcc.getQueryID(),"Query id should 1 becuse we set it previously ");


    }
}
