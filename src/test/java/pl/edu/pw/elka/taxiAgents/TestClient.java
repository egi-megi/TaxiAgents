package pl.edu.pw.elka.taxiAgents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import pl.edu.pw.elka.taxiAgents.messages.CallCenterToClient;
import pl.edu.pw.elka.taxiAgents.messages.CallTaxi;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TestClient extends Agent implements ITestClient{
    public TestClient() {
        registerO2AInterface(ITestClient.class,this);
    }

    BlockingQueue<ACLMessage> responseQ=new ArrayBlockingQueue<>(10);

    protected void setup()
    {

        addBehaviour(new CyclicBehaviour(this)
        {
            public void action() {

                ACLMessage msgI = receive();
                if (msgI != null) {
                    responseQ.add(msgI);
                }
            }
        });

    }


    @Override
    public ACLMessage runMessage(String dest, Serializable o) throws IOException, InterruptedException {

        ACLMessage msg= new ACLMessage(ACLMessage.INFORM);

        msg.addReceiver(new AID(dest, AID.ISLOCALNAME));


        msg.setContentObject(o);
        send(msg);



        return responseQ.take();
    }

}
