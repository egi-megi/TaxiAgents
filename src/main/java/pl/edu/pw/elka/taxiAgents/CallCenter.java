package pl.edu.pw.elka.taxiAgents;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;

//import jade.domain.AMSService;
//import jade.domain.FIPAAgentManagement.*;

import jade.lang.acl.*;
import pl.edu.pw.elka.taxiAgents.messages.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CallCenter extends Agent
{

    static class ProcessingQuery{
        String id;
        CallTaxi query;
        Queue<AID> taxisToCheck;
        //Queue<Object> acceptedMessages;
        Map<String, ACLMessage> acceptedMessages;
        AID customer;
        long waitingStartTime;

        public ProcessingQuery(String id, CallTaxi query, Queue<AID> taxisToCheck, AID customer) {
            this.id = id;
            this.query = query;
            this.taxisToCheck = taxisToCheck;
            this.customer = customer;
            this.waitingStartTime = System.currentTimeMillis();
            this.acceptedMessages = new HashMap<>();
        }
    }
    static long maxWaitingTime = 5000L;
    static double maxLongerWaitingTime = 100d;

    AtomicInteger queriesIdsSource=new AtomicInteger(1);

    Map<String,ProcessingQuery> activeQueries=new HashMap<>();

    Queue<ProcessingQuery> QueriesToProcess = new LinkedList<>();

    Set<AID> taxis=new HashSet<>();

    void sendQueryToAllTaxis(ProcessingQuery pq) throws IOException {
        for(AID taxi : taxis)
        {
            sendQueryToNextTaxi(pq, taxi);
        }
    }

    void sendQueryToNextTaxi(ProcessingQuery pq, AID taxi) throws IOException {
        ACLMessage mesg=new ACLMessage(ACLMessage.REQUEST);
        CallCenterToTaxi cct=new CallCenterToTaxi(pq.query.getFrom(), pq.query.getTo(),pq.query.isIfBabySeat(), pq.query.isIfHomePet(), pq.query.isIfLargeLuggage(), pq.query.getNumberOFPassengers(), pq.query.getKindOfClient(), pq.id);
        mesg.setContentObject(cct);
        mesg.addReceiver(taxi);
        send(mesg);
    }

    ACLMessage chooseBestTaxi(ProcessingQuery pq) throws IOException {
        TaxiToCallCenter bestTaxi = null;
        TaxiToCallCenter thisTaxi;
        ACLMessage bestTaxiMessage = null;
        Object thisMesg;
        double shortestTime = Double.MAX_VALUE;
        double thisTaxiMeanIncome, bestTaxiMeanIncome = 0;
        try{
            if(!pq.acceptedMessages.isEmpty()){
                for (Map.Entry<String, ACLMessage> entry : pq.acceptedMessages.entrySet()) {
                    thisMesg = entry.getValue().getContentObject();
                    thisTaxi = (TaxiToCallCenter) thisMesg;
                    if (bestTaxi == null) {
                        bestTaxi = thisTaxi;
                        bestTaxiMessage = entry.getValue();
                        bestTaxiMeanIncome = bestTaxi.getTodayEarnings()/bestTaxi.getWorkingTimeInThisDay();
                    }
                    if (thisTaxi.getTimeToPickUpClient() < shortestTime) {
                        shortestTime = thisTaxi.getTimeToPickUpClient(); }

                    thisTaxiMeanIncome = thisTaxi.getTodayEarnings()/thisTaxi.getWorkingTimeInThisDay();
                    if (thisTaxiMeanIncome < bestTaxiMeanIncome)
                    {
                        if (thisTaxi.getTimeToPickUpClient() - maxLongerWaitingTime < shortestTime)
                        {
                            bestTaxi = thisTaxi;
                            bestTaxiMessage = entry.getValue();
                            bestTaxiMeanIncome = thisTaxiMeanIncome;
                        }
                    } else if (thisTaxi.getTimeToPickUpClient() < bestTaxi.getTimeToPickUpClient()){
                        bestTaxi = thisTaxi;
                        bestTaxiMessage = entry.getValue();
                        bestTaxiMeanIncome = thisTaxiMeanIncome;
                    }
                }
            }
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        return bestTaxiMessage;
    }


    //TODO  while (!pq.acceptedMessages.isEmpty())
    //        {
    //            thisTaxi = (TaxiToCallCenter) pq.acceptedMessages.poll();
    //            if (thisTaxi.getTimeToPickUp() < bestTaxi.getTimeToPickUp())
    //            {
    //                bestTaxi = thisTaxi;
    //            }
    //        }: dodawac do nie j na podstawie wiadomosci2
    protected void setup()
    {

        System.out.println("Agent centrali "+getAID().getName()+" jest gotowy.");

        addBehaviour(new CyclicBehaviour(this)
        {
            public void action() {

                //this counter is used when we send client info to taxis
                ACLMessage msgI = receive();
                do {
                    //System.out.println("next message to call center");
                    if (msgI != null) {
                        try {
                            Object mesg = msgI.getContentObject();
                            if (mesg instanceof CallTaxi) {
                                System.out.println("klient query");

                                CallTaxi ct = (CallTaxi) mesg;
                                if (taxis.isEmpty()) {
                                    System.out.println("Nie mamy żadnej taksówki zarejestrowanej.");
                                } else {
                                    ProcessingQuery pq = new ProcessingQuery("" + (queriesIdsSource.getAndIncrement()),
                                            ct,
                                            new LinkedList<>(taxis),
                                            msgI.getSender());
                                    QueriesToProcess.add(pq);
                                    activeQueries.put(pq.id, pq);
                                    try {
                                        //We sends info about query to all taxis;
                                        sendQueryToAllTaxis(pq);
                                        System.out.println("We've just send info to all taxis ");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            if (mesg instanceof TaxiToCallCenter) {
                                System.out.println("Taxi to callcenter");
                                TaxiToCallCenter tcc = (TaxiToCallCenter) mesg;
                                ProcessingQuery pq = activeQueries.get(tcc.getQueryID());
                                if(pq == null)
                                {
                                    System.out.println("Takie zapytanie nie istnieje?");
                                    break;
                                }
                                System.out.println(pq.id);
                                pq.acceptedMessages.put(msgI.getSender().getName(), msgI);
                                //activeQueries.remove(tcc.getQueryID());
                                System.out.println("== Answer" + " <- "
                                        + tcc.getTimeToPickUpClient() + " from "
                                        + msgI.getSender().getName());
                            }

                            if (mesg instanceof TaxiConfirmTakingOrder)
                            {
                                TaxiConfirmTakingOrder tco = (TaxiConfirmTakingOrder) mesg;
                                if(tco.isIfTake())
                                {
                                    System.out.println("Great - taxi is realizing query NO: "+tco.getIdQuery());
                                }
                                else {
                                    System.out.println("Ooops! Specific taxi cannot handle this order");
                                    ProcessingQuery pq = activeQueries.get(tco.getIdQuery());
                                    pq.acceptedMessages.remove(msgI.getSender().getName());
                                    QueriesToProcess.add(pq);
                                }
                            }

                            if (mesg instanceof TaxiRegister) {
                                taxis.add(msgI.getSender());
                                System.out.println("Rejestruje taksówkę " + msgI.getSender().getName());
                            }

                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }

                    }
                } while ((msgI = receive()) != null);

                if (!QueriesToProcess.isEmpty()) {
                    ACLMessage bestTaxi;
                    ProcessingQuery pq = QueriesToProcess.peek();
                    if (System.currentTimeMillis() - pq.waitingStartTime > maxWaitingTime) {
                        try {
                            bestTaxi = chooseBestTaxi(pq);
                            if (bestTaxi != null) {
                                System.out.println("Wybralem najszybsza taksowke: " + bestTaxi.getSender());
                                ACLMessage confirmTaxi = new ACLMessage(ACLMessage.INFORM);
                                confirmTaxi.addReceiver(bestTaxi.getSender());
                                try {
                                    confirmTaxi.setContentObject(new CallCenterConfirmTaxi(pq.query.getFrom(), pq.query.getTo(), pq.query.getKindOfClient(), pq.id));
                                    send(confirmTaxi);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                QueriesToProcess.remove();
                            }
                            else {
                                System.out.println("Brak taksówek - proszę czekać");
                                try {
                                    //We sends info about query to all taxis;
                                    sendQueryToAllTaxis(pq);
                                    System.out.println("We've just send info to all taxis ");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                pq.waitingStartTime = System.currentTimeMillis();
                                QueriesToProcess.add(QueriesToProcess.poll());
                            }
                            //Here is creating message to best Taxi that they will have
                                /*
                                ACLMessage confirmTaxi = new ACLMessage(ACLMessage.INFORM);
                                System.out.println("Informuję taxi o tym, że bierze przejazd:");
                                System.out.println(msgI.getSender().getName());
                                confirmTaxi.addReceiver(msgI.getSender());
                                try {
                                    confirmTaxi.setContentObject(new CallCenterConfirmTaxi(pq.query.getFrom(), pq.query.getTo(), pq.id));
                                    send(confirmTaxi);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } */
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });


        System.out.println("Edded setup with success");
    }
}