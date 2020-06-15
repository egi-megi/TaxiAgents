package pl.edu.pw.elka.taxiAgents;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.StaleProxyException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import pl.edu.pw.elka.taxiAgents.messages.CallCenterToClient;
import pl.edu.pw.elka.taxiAgents.messages.CallTaxi;

import java.io.IOException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DifferentEarningsDuringWorkingDay extends TestCommonInit {
    @Override
    protected Object[][] getTaxisData() {
        return new Object[][]{
                {new Position(860, 760), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 200, 5, 20, 600},
                {new Position(860, 760), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 150, 5, 20, 600},
                };
    }


    @Test
    public void quryingCallCenterForClientEarningsTest() throws StaleProxyException, IOException, InterruptedException, UnreadableException {

        //Check that if drivers have different earning, system choose driver who has less
        CallTaxi cct = new CallTaxi(new Position(1000, 1000), new Position(1010, 1010), false, false, false, 1, "normal");
        ACLMessage message = acClient.getO2AInterface(ITestClient.class).runMessage("CallCenter", cct);
        CallCenterToClient cctc = (CallCenterToClient) message.getContentObject();
        Assertions.assertEquals("1", cctc.getTaxiName().split("@")[0], "Taxi name should be 1 (name are from 0 not form 1) because it earns less money.");
    }


}
