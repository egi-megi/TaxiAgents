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
public class TimeToGoHome extends TestCommonInit {

    @Override
    protected Object[][] getTaxisData() {
        return new Object[][]{
                {new Position(850, 850), new Position(1100, 1100), true, true, "van", 8, true, true, "goesHome", 5.7, 200, 5, 0, 6},
                {new Position(950, 950), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.7, 150, 5, 0, 80}};

    }

    @Test
    public void quryingCallCenterForClientGoHomeTest() throws StaleProxyException, IOException, InterruptedException, UnreadableException {

        //Check that if drivers have different time to go home, system choose driver who has status "goesHome"
        CallTaxi cct = new CallTaxi(new Position(1000, 1000), new Position(1010, 1010), false, false, false, 1, "normal");
        ACLMessage message = acClient.getO2AInterface(ITestClient.class).runMessage("CallCenter", cct);
        CallCenterToClient cctc = (CallCenterToClient) message.getContentObject();
        Assertions.assertEquals("0", cctc.getTaxiName().split("@")[0], "Taxi name should be 0 (name are from 0 not form 1) because he goes home.");

    }


}

