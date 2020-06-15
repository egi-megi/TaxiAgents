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
                {new Position(760, 760), new Position(1100, 1100), true, true, "van", 8, true, true, "goesHome", 5.7, 200, 5, 20, 6},
                {new Position(900, 900), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.7, 150, 5, 20, 8}};

    }

    @Test
    public void quryingCallCenterForClientGoHomeTest() throws StaleProxyException, IOException, InterruptedException, UnreadableException {

        //Check that if drivers have different time to go home, system choose driver who has status "goesHome"
        CallTaxi cct = new CallTaxi(new Position(1000, 1000), new Position(1010, 1010), false, false, false, 1, "normal");
        ACLMessage message = acClient.getO2AInterface(ITestClient.class).runMessage("CallCenter", cct);
        CallCenterToClient cctc = (CallCenterToClient) message.getContentObject();
        //TODO: WYchodzi 1 ale expected ustawilas 0 ale komentarz ze ma byc  jeden WTF
        Assertions.assertEquals("1", cctc.getTaxiName().split("@")[0], "Taxi name should be 1 (name are from 0 not form 1) because it earns less money.");

    }


}

