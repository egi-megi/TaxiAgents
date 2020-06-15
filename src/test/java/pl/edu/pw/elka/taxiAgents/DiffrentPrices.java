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
public class DiffrentPrices extends TestCommonInit {
    @Override
    protected Object[][] getTaxisData() {
        return new Object[][]{
                {new Position(860, 760), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 200, 5, 20, 600},
                {new Position(860, 760), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 150, 5, 20, 600},
                {new Position(860, 760), new Position(33, 33), true, true, "van", 8, true, true, "free", 5.5, 150, 5, 20, 600},
        };
    }


    @Test
    public void quryingCallCenterForNormalClientPricesTest() throws StaleProxyException, IOException, InterruptedException, UnreadableException {

        //Check price for normal client (price per kilometr: 3.0)
        CallTaxi cct = new CallTaxi(new Position(1000, 1000), new Position(5990, 1010), false, false, false, 1, "normal");
        ACLMessage message = acClient.getO2AInterface(ITestClient.class).runMessage("CallCenter", cct);
        CallCenterToClient cctc = (CallCenterToClient) message.getContentObject();
        Assertions.assertEquals("15.0", "" + cctc.getPrice(), "Excepted price (calculated by hand) for order is 15 PLN.");
    }

    @Test
    public void quryingCallCenterForCorpoClientPricesTest() throws StaleProxyException, IOException, InterruptedException, UnreadableException {

        //Check price for korpo client (price per kilometr: 2.5)
        CallTaxi cct1 = new CallTaxi(new Position(1000, 1000), new Position(5990, 1010), false, false, false, 1, "korpo");
        ACLMessage message1 = acClient.getO2AInterface(ITestClient.class).runMessage("CallCenter", cct1);
        CallCenterToClient cctc1 = (CallCenterToClient) message1.getContentObject();
        Assertions.assertEquals("12.5", "" + cctc1.getPrice(), "Excepted price (calculated by hand) for order is 12.5 PLN.");
    }

    @Test
    public void quryingCallCenterForVIPClientDifferentPricesTest() throws StaleProxyException, IOException, InterruptedException, UnreadableException {

        //Check price for vip client (price per kilometr: 2.3)
        CallTaxi cct2 = new CallTaxi(new Position(1000, 1000), new Position(5990, 1010), false, false, false, 1, "vip");
        ACLMessage message2 = acClient.getO2AInterface(ITestClient.class).runMessage("CallCenter", cct2);
        CallCenterToClient cctc2 = (CallCenterToClient) message2.getContentObject();
        Assertions.assertEquals("11.5", "" + cctc2.getPrice(), "Excepted price (calculated by hand) for order is 11.5 PLN.");
    }


}
