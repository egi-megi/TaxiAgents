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
    static long maxAgentsResponseWaitingTime = 5000L;
    static double maxLongerWaitingTime = 5d;
    static double maxClientWaitingTime = 20d;
    static double maxTaxiFreeTime = 20d;
    public final static String DRIVER_STATUS_GOES_HOME = "goesHome";

    AtomicInteger queriesIdsSource=new AtomicInteger(1);

    Map<String,ProcessingQuery> activeQueries=new HashMap<>();

    Queue<ProcessingQuery> QueriesToProcess = new LinkedList<>();

    Map<AID, String> taxis=new HashMap<>();

    void sendQueryToAllTaxis(ProcessingQuery pq) throws IOException {
        for(Map.Entry<AID, String> entry : taxis.entrySet())
        {
            sendQueryToNextTaxi(pq, entry.getKey());
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
        String thisDriverStatus;
        boolean isBestGoingHome = false;
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
                    thisDriverStatus = taxis.get(thisTaxi);
                    if (bestTaxi == null) {
                        bestTaxi = thisTaxi;
                        bestTaxiMessage = entry.getValue();
                        bestTaxiMeanIncome = bestTaxi.getTodayEarnings()/bestTaxi.getWorkingTimeInThisDay();
                    }
                    if (thisTaxi.getTimeToPickUpClient() < shortestTime) {
                        shortestTime = thisTaxi.getTimeToPickUpClient(); }
                    if(thisDriverStatus == DRIVER_STATUS_GOES_HOME && thisTaxi.getTimeToPickUpClient()<maxClientWaitingTime)
                    {
                        bestTaxi = thisTaxi;
                        bestTaxiMessage = entry.getValue();
                        break;
                    }
                    thisTaxiMeanIncome = thisTaxi.getTodayEarnings()/thisTaxi.getWorkingTimeInThisDay();
                    if (thisTaxiMeanIncome < bestTaxiMeanIncome)
                    {
                        if (thisTaxi.getTimeToPickUpClient() - maxLongerWaitingTime < shortestTime)
                        {
                            bestTaxi = thisTaxi;
                            bestTaxiMessage = entry.getValue();
                            bestTaxiMeanIncome = thisTaxiMeanIncome;
                        }
                    } else if ( bestTaxi.getTimeToPickUpClient() > thisTaxi.getTimeToPickUpClient()) {
                        if (bestTaxi.getTimeToPickUpClient() - maxLongerWaitingTime < shortestTime) {

                        } else {
                            bestTaxi = thisTaxi;
                            bestTaxiMessage = entry.getValue();
                            bestTaxiMeanIncome = thisTaxiMeanIncome;
                        }
                    }

                    if(thisTaxi.getTimeFromLastClient() > maxTaxiFreeTime && thisTaxi.getTimeToPickUpClient() < maxClientWaitingTime)
                    {
                        bestTaxi = thisTaxi;
                        bestTaxiMessage = entry.getValue();
                        break;
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

        System.out.println("Central agent "+getAID().getName()+" is ready");

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
                                CallTaxi ct = (CallTaxi) mesg;
                                if (taxis.isEmpty()) {
                                    System.out.println("There's no registered taxi");
                                } else {
                                    ProcessingQuery pq = new ProcessingQuery("" + (queriesIdsSource.getAndIncrement()),
                                            ct,
                                            new LinkedList<>(taxis.keySet()),
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
                                TaxiToCallCenter tcc = (TaxiToCallCenter) mesg;
                                taxis.put(msgI.getSender(), tcc.getDriverStatus());
                                ProcessingQuery pq = activeQueries.get(tcc.getQueryID());
                                if(pq == null)
                                {
                                    System.out.println("There is no such query like this taxi answeared");
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
                                taxis.put(msgI.getSender(), "free");
                                System.out.println("Taxi is registered " + msgI.getSender().getName());
                            }

                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }

                    }
                } while ((msgI = receive()) != null);

                if (!QueriesToProcess.isEmpty()) {
                    ACLMessage bestTaxi;
                    ProcessingQuery pq = QueriesToProcess.peek();
                    if (System.currentTimeMillis() - pq.waitingStartTime > maxAgentsResponseWaitingTime) {
                        try {
                            bestTaxi = chooseBestTaxi(pq);
                            if (bestTaxi != null) {
                                System.out.println("Best taxi has been chosen " + bestTaxi.getSender());
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
                                System.out.println("There is no taxis - please wait");
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

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else { block(); }
            }
        });


        System.out.println("Edded setup with success");
    }
}