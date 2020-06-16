package pl.edu.pw.elka.taxiAgents;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import pl.edu.pw.elka.taxiAgents.messages.TaxiStatus;

import java.io.*;

public class EfficiencyTest {


    protected Runtime rt;
    protected String[] taxisNames = new String[0];
    ContainerController ccAgent;


    public void setup() throws StaleProxyException {
        // Get a hold on JADE runtime
        rt = Runtime.instance();
        // Create a default profile
        Profile p = new ProfileImpl();
        // Create a new non-main container, connecting to the default
        rt.createMainContainer(new ProfileImpl(true));
        ccAgent = rt.createAgentContainer(p);
        // Create a new agent

        AgentController ac = ccAgent.createNewAgent("CallCenter", "pl.edu.pw.elka.taxiAgents.CallCenter", new Object[0]);
        ac.start();

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ccAgent.createNewAgent("display", "pl.edu.pw.elka.taxiAgents.EfTestDisplay", new Object[0]).start();
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


    void initTaxis(int numTaxis) throws StaleProxyException {
        Object[][] taxisData = getTaxisData(numTaxis);
        taxisNames = new String[taxisData.length];
        for (int i = 0; i < taxisData.length; i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            taxisNames[i] = "t-" + i;
            AgentController dummy = ccAgent.createNewAgent(taxisNames[i], "pl.edu.pw.elka.taxiAgents.TaxiAgent", taxisData[i]);
            // Fire up the agent
            dummy.start();
        }
    }


    protected Object[][] getTaxisData(int numTaxis) {
        Object[][] taxisList = new Object[numTaxis][];
        for (int i = 0; i < numTaxis; i++) {
            int pos = i * 50;
            taxisList[i] = new Object[]{new Position(0 + pos, 2000), new Position(33+ pos, 33), true, true, "van", 8, true, true, "free", 0.0, 0, 0, 0, 6000};
        }

        return taxisList;
    }



    void runClient(int num) throws StaleProxyException, IOException, InterruptedException {
        AgentController acClient = ccAgent.createNewAgent("client-"+num, "pl.edu.pw.elka.taxiAgents.Client", new Object[0]);
        // Fire up the agent
        acClient.start();
        acClient.getO2AInterface(ClientI.class).doQuery(new Position(0 + num*50, 2000), new Position(3000, 3000), false, false, false, 1, "normal",true);

        acClient.kill();
    }

    void runClients(int numClients){
        //uruchom klientow neich zadadza zapytania i poczekaj na odpowiedzi...

        Thread[] threads=new Thread[numClients];
        for (int i=0; i<numClients; i++) {
            final int num=i;
            threads[i]=new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        runClient(num);
                    } catch (StaleProxyException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            threads[i].start();
        }
        for (int i=0 ; i<numClients; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    public static void main(String[] args) throws StaleProxyException, FileNotFoundException {
            EfficiencyTest et=new EfficiencyTest();

            int numTaxis=200;
            int numClients=200;
            System.out.println(args);
        if (args.length == 2) {
            numTaxis = Integer.parseInt(args[0]);
            numClients = Integer.parseInt(args[1]);
        }

            et.setup();
            et.initTaxis(numTaxis);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
            long startTime=System.currentTimeMillis();
            et.runClients(numClients);
            long endTime=System.currentTimeMillis();
            System.out.println("\n\n\n\nWorking time with "+numTaxis+" taxis and "+numClients+" clients is "+(endTime-startTime)+"ms");
            File output=new File("ef-test-"+numTaxis+"-"+numClients+".txt");
            try (PrintStream ps=new PrintStream(new FileOutputStream(output,true))) {
                ps.println("Working time with;"+numTaxis+"; taxis and ;"+numClients+"; clients is ;"+(endTime-startTime)+";ms");
            }
            et.rt.shutDown();
            System.exit(0);
    }
}
