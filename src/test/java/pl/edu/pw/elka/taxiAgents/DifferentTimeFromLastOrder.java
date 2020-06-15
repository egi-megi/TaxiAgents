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
public class DifferentTimeFromLastOrder extends TestCommonInit {

    protected Object[][] getTaxisData() {
        return new Object[][]{
                {new Position(860, 860), new Position(33, 33), true, true, "van", 8, true, true, "free", 4.5, 200, 5, 5 * 1000, 600},
                {new Position(860, 860), new Position(33, 33), true, true, "van", 8, true, true, "free", 4.5, 200, 50, 5 * 1000, 600},};

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
