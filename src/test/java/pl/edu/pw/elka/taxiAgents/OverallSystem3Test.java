package pl.edu.pw.elka.taxiAgents;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.StaleProxyException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import pl.edu.pw.elka.taxiAgents.messages.CallCenterToClient;
import pl.edu.pw.elka.taxiAgents.messages.CallCenterToTaxi;
import pl.edu.pw.elka.taxiAgents.messages.CallTaxi;
import pl.edu.pw.elka.taxiAgents.messages.TaxiToCallCenter;

import java.io.IOException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OverallSystem3Test extends TestCommonInit {
    @Override
    protected Object[][] getTaxisData() {
        return new Object[][]{
                {new Position(760, 760), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 150, 5, 20, 60},
                {new Position(980, 980), new Position(33, 33), false, false, "sedan", 3, true, true, "free", 5.5, 150, 5, 20, 60},
                {new Position(990, 990), new Position(33, 33), true, true, "van", 8, true, true, "working", 5.5, 150, 5, 20, 8 * 60}};
    }



    @Test
    public void quryingCallCenterForClientTest() throws StaleProxyException, IOException, InterruptedException, UnreadableException {

      // Check if was choosen taxi which can take a large luggage
        CallTaxi cct4 = new CallTaxi(new Position(1000, 1000), new Position(1050, 1050), false, false, true, 1, "normal");
        ACLMessage message4 = acClient.getO2AInterface(ITestClient.class).runMessage("CallCenter", cct4);
        CallCenterToClient cctc4 = (CallCenterToClient) message4.getContentObject();
        Assertions.assertEquals("0", cctc4.getTaxiName().split("@")[0], "Taxi name should 0 (name are from 0 not form 1) because taxi 1 can take large luggage.");


    }


}
