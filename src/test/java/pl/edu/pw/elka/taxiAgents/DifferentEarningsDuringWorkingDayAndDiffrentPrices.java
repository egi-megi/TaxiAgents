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
public class DifferentEarningsDuringWorkingDayAndDiffrentPrices extends TestCommonInit {
    @Override
    protected Object[][] getTaxisData() {
        return new Object[][]{
                {new Position(860, 760), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 200, 5, 20, 600},
                {new Position(860, 760), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 150, 5, 20, 600},
                {new Position(860, 760), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 150, 5, 20, 600},
        };
    }


/*
    @Test
    public void quryingCallCenterForClientEarningsTest() throws StaleProxyException, IOException, InterruptedException, UnreadableException {

        //Check that if drivers have different earning, system choose driver who has less
        CallTaxi cct = new CallTaxi(new Position(1000, 1000), new Position(1010, 1010), false, false, false, 1, "normal");
        ACLMessage message = acClient.getO2AInterface(ITestClient.class).runMessage("CallCenter", cct);
        CallCenterToClient cctc = (CallCenterToClient) message.getContentObject();
        Assertions.assertEquals("1", cctc.getTaxiName().split("@")[0], "Taxi name should be 1 (name are from 0 not form 1) because it earns less money.");

    }*/

    @Test
    public void quryingCallCenterForNormalClientPricesTest() throws StaleProxyException, IOException, InterruptedException, UnreadableException {

        //Check price for normal client (price per kilometr: 3.0)
        CallTaxi cct = new CallTaxi(new Position(1000, 1000), new Position(1500, 1010), false, false, false, 1, "normal");
        ACLMessage message = acClient.getO2AInterface(ITestClient.class).runMessage("CallCenter", cct);
        CallCenterToClient cctc = (CallCenterToClient) message.getContentObject();
        Assertions.assertEquals("15.3", "" + cctc.getPrice(), "Taxi name should be 1 (name are from 0 not form 1) because it earns less money.");
    }

    @Test
    public void quryingCallCenterForCorpoClientPricesTest() throws StaleProxyException, IOException, InterruptedException, UnreadableException {

        //Check price for korpo client (price per kilometr: 2.5)
        CallTaxi cct1 = new CallTaxi(new Position(1000, 1000), new Position(1500, 1010), false, false, false, 1, "korpo");
        ACLMessage message1 = acClient.getO2AInterface(ITestClient.class).runMessage("CallCenter", cct1);
        CallCenterToClient cctc1 = (CallCenterToClient) message1.getContentObject();
        Assertions.assertEquals("12.75", "" + cctc1.getPrice(), "Taxi name should be 1 (name are from 0 not form 1) because it earns less money.");
    }

    @Test
    public void quryingCallCenterForVIPClientDifferentPricesTest() throws StaleProxyException, IOException, InterruptedException, UnreadableException {

        //Check price for vip client (price per kilometr: 2.3)
        CallTaxi cct2 = new CallTaxi(new Position(1000, 1000), new Position(1500, 1010), false, false, false, 1, "vip");
        ACLMessage message2 = acClient.getO2AInterface(ITestClient.class).runMessage("CallCenter", cct2);
        CallCenterToClient cctc2 = (CallCenterToClient) message2.getContentObject();
        Assertions.assertEquals("11.73", "" + cctc2.getPrice(), "Taxi name should be 1 (name are from 0 not form 1) because it earns less money.");
    }


}
