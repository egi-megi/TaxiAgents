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
public class OverallSystemTest extends TestCommonInit {
    @Override
    protected Object[][] getTaxisData() {
        return new Object[][]{
                {new Position(760, 760), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 150, 5, 20, 60},
                {new Position(980, 980), new Position(33, 33), false, false, "sedan", 3, true, true, "free", 5.5, 150, 5, 20, 60},
                {new Position(990, 990), new Position(33, 33), true, true, "van", 8, true, true, "working", 5.5, 150, 5, 20, 8 * 60}};
    }


    @Test
    public void quryingTaxiForClientTest() throws StaleProxyException, IOException, InterruptedException, UnreadableException {

        //Check if Taxi response to query of CallCenter about order if Taxi meets all requirements
        CallCenterToTaxi cct = new CallCenterToTaxi(new Position(1000, 1000), new Position(1050, 1050), false, false, false, 1, "normal", "1");
        ACLMessage message = acClient.getO2AInterface(ITestClient.class).runMessage(taxisNames[0], cct);
        Assertions.assertTrue(message.getContentObject() instanceof TaxiToCallCenter);

        TaxiToCallCenter tcc = (TaxiToCallCenter) message.getContentObject();
        Assertions.assertEquals("1", tcc.getQueryID(), "Query id should 1 because we set it previously ");


    }


    @Test
    public void quryingCallCenterForClientTest() throws StaleProxyException, IOException, InterruptedException, UnreadableException {

        //Check that if client send query then get the answer
        CallTaxi cct = new CallTaxi(new Position(1000, 1000), new Position(1010, 1010), false, false, false, 1, "normal");
        ACLMessage message = acClient.getO2AInterface(ITestClient.class).runMessage("CallCenter", cct);
        Assertions.assertTrue(message.getContentObject() instanceof CallCenterToClient);
/*
        // Check if was choosen taxi which is closer to the client
        CallCenterToClient cctc = (CallCenterToClient) message.getContentObject();
        Assertions.assertEquals("1", cctc.getTaxiName().split("@")[0], "Taxi name should be 1 (name are from 0 not form 1) because it has smaller time and is free to pick up the client ");

        // Check if was choosen taxi with baby seat
        CallTaxi cct2 = new CallTaxi(new Position(1000, 1000), new Position(1050, 1050), true, false, false, 1, "normal");
        ACLMessage message2 = acClient.getO2AInterface(ITestClient.class).runMessage("CallCenter", cct2);
        CallCenterToClient cctc2 = (CallCenterToClient) message2.getContentObject();
        Assertions.assertEquals("0", cctc2.getTaxiName().split("@")[0], "Taxi name should 0 (name are from 0 not form 1) because taxi 1 has baby seat (and is free to pick up the client but has longer time too pick up client).");

        // Check if was choosen taxi which can take a home pet
        CallTaxi cct3 = new CallTaxi(new Position(1000, 1000), new Position(1050, 1050), false, true, false, 1, "normal");
        ACLMessage message3 = acClient.getO2AInterface(ITestClient.class).runMessage("CallCenter", cct3);
        CallCenterToClient cctc3 = (CallCenterToClient) message3.getContentObject();
        Assertions.assertEquals("0", cctc3.getTaxiName().split("@")[0], "Taxi name should 0 (name are from 0 not form 1) because taxi 1 has baby seat (and is free to pick up the client but has longer time too pick up client).");

        // Check if was choosen taxi which can take a large luggage
        CallTaxi cct4 = new CallTaxi(new Position(1000, 1000), new Position(1050, 1050), false, false, true, 1, "normal");
        ACLMessage message4 = acClient.getO2AInterface(ITestClient.class).runMessage("CallCenter", cct4);
        CallCenterToClient cctc4 = (CallCenterToClient) message4.getContentObject();
        Assertions.assertEquals("0", cctc4.getTaxiName().split("@")[0], "Taxi name should 0 (name are from 0 not form 1) because taxi 1 can take large luggage.");

        // Check if was choosen taxi which can take 5 passengers
        CallTaxi cct5 = new CallTaxi(new Position(1000, 1000), new Position(1050, 1050), false, false, false, 5, "normal");
        ACLMessage message5 = acClient.getO2AInterface(ITestClient.class).runMessage("CallCenter", cct5);
        CallCenterToClient cctc5 = (CallCenterToClient) message5.getContentObject();
        Assertions.assertEquals("0", cctc5.getTaxiName().split("@")[0], "Taxi name should 0 (name are from 0 not form 1) because taxi 1 can take 5 passengers.");
        Assertions.assertEquals("0", cctc5.getTaxiName().split("@")[0], "Taxi name should 0 (name are from 0 not form 1) because taxi 1 can take 5 passengers.");
*/
    }


}
