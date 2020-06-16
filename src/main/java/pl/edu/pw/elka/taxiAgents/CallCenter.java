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

public class CallCenter extends Agent {

    static class ProcessingQuery {
        String id;
        CallTaxi query;
        Queue<AID> taxisToCheck;
        //Queue<Object> acceptedMessages;
        Map<String, ACLMessage> acceptedMessages;
        AID customer;
        long waitingStartTime;
        Double timeToPickUp;
        Double price;
        int quetionsSend;
        public ProcessingQuery(String id, CallTaxi query, Queue<AID> taxisToCheck, AID customer, Double timeToPickUp, Double price) {
            this.id = id;
            this.query = query;
            this.taxisToCheck = taxisToCheck;
            this.customer = customer;
            this.waitingStartTime = System.currentTimeMillis();
            this.acceptedMessages = new HashMap<>();
            this.timeToPickUp = timeToPickUp;
            this.price = price;

        }
    }

    static long maxAgentsResponseWaitingTime = 5000L;
    static double maxLongerWaitingTime = 5d;
    static double maxClientWaitingTime = 20.0;
    static double maxTaxiFreeTime = 20d;
    public final static String DRIVER_STATUS_GOES_HOME = "goesHome";
    public final static String CONFIRMING_ORDER="confirming_order-";

    AtomicInteger queriesIdsSource = new AtomicInteger(1);

    Map<String, ProcessingQuery> activeQueries = new HashMap<>();

    Queue<ProcessingQuery> QueriesToProcess = new LinkedList<>();

    Map<AID, String> taxis = new HashMap<>();

    void sendQueryToAllTaxis(ProcessingQuery pq) throws IOException {
        int sent=0;
        for (Map.Entry<AID, String> entry : taxis.entrySet()) {
            sendQueryToNextTaxi(pq, entry.getKey());
            sent++;
        }
        pq.quetionsSend=sent;
    }

    void sendQueryToNextTaxi(ProcessingQuery pq, AID taxi) throws IOException {
        ACLMessage mesg = new ACLMessage(ACLMessage.REQUEST);
        CallCenterToTaxi cct = new CallCenterToTaxi(pq.query.getFrom(), pq.query.getTo(), pq.query.isIfBabySeat(), pq.query.isIfHomePet(), pq.query.isIfLargeLuggage(), pq.query.getNumberOFPassengers(), pq.query.getKindOfClient(), pq.id);
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
        try {
            if (!pq.acceptedMessages.isEmpty()) {
                for (Map.Entry<String, ACLMessage> entry : pq.acceptedMessages.entrySet()) {
                    thisMesg = entry.getValue().getContentObject();
                    thisTaxi = (TaxiToCallCenter) thisMesg;
                    thisDriverStatus = taxis.get(entry.getValue().getSender());
                    if (thisDriverStatus.startsWith(CONFIRMING_ORDER)) {
                        continue;
                    }
                    assert thisDriverStatus != null;
                    if (bestTaxi == null) {
                        bestTaxi = thisTaxi;
                        bestTaxiMessage = entry.getValue();
                        bestTaxiMeanIncome = bestTaxi.getTodayEarnings() / bestTaxi.getWorkingTimeInThisDay();
                    }
                    if (thisTaxi.getTimeToPickUpClient() < shortestTime) {
                        shortestTime = thisTaxi.getTimeToPickUpClient();
                    }
                    if (thisDriverStatus.equalsIgnoreCase(DRIVER_STATUS_GOES_HOME) && thisTaxi.getTimeToPickUpClient() < maxClientWaitingTime) {
                        bestTaxi = thisTaxi;
                        bestTaxiMessage = entry.getValue();
                        break;
                    }
                    thisTaxiMeanIncome = thisTaxi.getTodayEarnings() / thisTaxi.getWorkingTimeInThisDay();
                    if (thisTaxiMeanIncome < bestTaxiMeanIncome) {
                        if (thisTaxi.getTimeToPickUpClient() - maxLongerWaitingTime < shortestTime) {
                            bestTaxi = thisTaxi;
                            bestTaxiMessage = entry.getValue();
                            bestTaxiMeanIncome = thisTaxiMeanIncome;
                        }
                    } else if (bestTaxiMeanIncome == thisTaxiMeanIncome) {
                        if (bestTaxi.getTimeToPickUpClient() > thisTaxi.getTimeToPickUpClient()) {
                            bestTaxi = thisTaxi;
                            bestTaxiMessage = entry.getValue();
                            bestTaxiMeanIncome = thisTaxiMeanIncome;
                        } else if (bestTaxi.getTimeToPickUpClient() == thisTaxi.getTimeToPickUpClient()) {
                            if (bestTaxi.getTimeFromLastClient() < thisTaxi.getTimeFromLastClient()) {
                                bestTaxi = thisTaxi;
                                bestTaxiMessage = entry.getValue();
                                bestTaxiMeanIncome = thisTaxiMeanIncome;
                                pq.timeToPickUp = bestTaxi.getTimeToPickUpClient();
                            }
                        }
                    } else if (bestTaxi.getTimeToPickUpClient() > thisTaxi.getTimeToPickUpClient()) {
                        if (bestTaxi.getTimeToPickUpClient() - maxLongerWaitingTime < shortestTime) {

                        } else {
                            bestTaxi = thisTaxi;
                            bestTaxiMessage = entry.getValue();
                            bestTaxiMeanIncome = thisTaxiMeanIncome;
                        }
                    }
                    System.out.println("Taxi: " + entry.getValue().getSender() + " from last " + thisTaxi.getTimeFromLastClient());
                    if (thisTaxi.getTimeFromLastClient() > maxTaxiFreeTime && thisTaxi.getTimeToPickUpClient() < maxClientWaitingTime) {
                        bestTaxi = thisTaxi;
                        bestTaxiMessage = entry.getValue();
                        break;
                    }
                }
            }
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        if (bestTaxi == null) {
            return null;
        }
        pq.price = bestTaxi.getPriceForAllDistance();
        pq.timeToPickUp = bestTaxi.getTimeToPickUpClient();
        return bestTaxiMessage;
    }


    long cleanProcessingQueue(){
        LinkedList<ProcessingQuery> queriesToProcess2 = new LinkedList<>();
        long maxTimeToWait=maxAgentsResponseWaitingTime;
        while (!QueriesToProcess.isEmpty()) {
            ACLMessage bestTaxi;
            ProcessingQuery pq = QueriesToProcess.peek();
            long waitedTime=System.currentTimeMillis() - pq.waitingStartTime;
            if ((pq.acceptedMessages.size()>= pq.quetionsSend) || waitedTime > maxAgentsResponseWaitingTime) {
                try {
                    bestTaxi = chooseBestTaxi(pq);
                    if (bestTaxi != null) {
                        System.out.println("Query: "+pq.id+" Best taxi has been chosen " + bestTaxi.getSender().getLocalName());
                        String oldStatus = taxis.get(bestTaxi.getSender());
                        taxis.put(bestTaxi.getSender(), CONFIRMING_ORDER + oldStatus);
                        ACLMessage confirmTaxi = new ACLMessage(ACLMessage.INFORM);
                        confirmTaxi.addReceiver(bestTaxi.getSender());
                        try {
                            confirmTaxi.setContentObject(new CallCenterConfirmTaxi(pq.query.getFrom(), pq.query.getTo(), pq.query.getKindOfClient(), pq.id));
                            send(confirmTaxi);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        QueriesToProcess.remove();
                    } else {
                        System.out.println("There is no taxis - please wait");
                        try {
                            //We sends info about query to all taxis;
                            sendQueryToAllTaxis(pq);
                            System.out.println("We've just resend info to all taxis ");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        pq.waitingStartTime = System.currentTimeMillis();

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                queriesToProcess2.add(QueriesToProcess.poll());
                maxTimeToWait=Math.min(waitedTime,maxTimeToWait);
            }
        }
        QueriesToProcess = queriesToProcess2;
        return maxTimeToWait;
    }

    protected void setup() {

        System.out.println("Central agent " + getAID().getName() + " is ready");

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {

                //this counter is used when we send client info to taxis
                ACLMessage msgI;
                while ((msgI = receive()) != null) {
                    //System.out.println("next message to call center");
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
                                        msgI.getSender(), 0d, 0d);
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
                            if (taxis.get(msgI.getSender()).startsWith(CONFIRMING_ORDER)) {
                                taxis.put(msgI.getSender(), CONFIRMING_ORDER + tcc.getDriverStatus());
                            } else {
                                taxis.put(msgI.getSender(),tcc.getDriverStatus());
                            }
                            ProcessingQuery pq = activeQueries.get(tcc.getQueryID());
                            if (pq == null) {
                                System.out.println("There is no such query like this taxi answered");
                                break;
                            }
                            pq.acceptedMessages.put(msgI.getSender().getName(), msgI);
                            //activeQueries.remove(tcc.getQueryID());
                            System.out.println("==Query: "+pq.id+" == Answer" + " <- "
                                    + tcc.getTimeToPickUpClient() + " from "
                                    + msgI.getSender().getName());
                        }

                        if (mesg instanceof TaxiConfirmTakingOrder) {
                            TaxiConfirmTakingOrder tco = (TaxiConfirmTakingOrder) mesg;
                            if (tco.isIfTake()) {
                                System.out.println("Great - taxi is realizing query NO: " + tco.getIdQuery() + " taxi "+msgI.getSender().getLocalName());
                                String status = taxis.get(msgI.getSender());
                                if (status.contains("-")) {
                                    taxis.put(msgI.getSender(), status.substring(status.indexOf("-")));
                                }
                                ProcessingQuery pq = activeQueries.get(tco.getIdQuery());
                                for (ProcessingQuery pqs:activeQueries.values()) {
                                    if (pq!=pqs && pqs.acceptedMessages.containsKey(msgI.getSender().getName())) {
                                        pqs.acceptedMessages.remove(msgI.getSender().getName());
                                        pqs.quetionsSend--;
                                    }
                                }
                                ACLMessage informClient = new ACLMessage(ACLMessage.INFORM);
                                informClient.addReceiver(pq.customer);
                                try {
                                    informClient.setContentObject(new CallCenterToClient(msgI.getSender().getName(), pq.timeToPickUp, pq.price));
                                    send(informClient);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                System.out.println("Ooops! Specific taxi cannot handle this order query NO: " + tco.getIdQuery() +
                                                " taxi "+msgI.getSender().getLocalName());
                                String status = taxis.get(msgI.getSender());
                                if (status.contains("-")) {
                                    taxis.put(msgI.getSender(), status.substring(status.indexOf("-")));
                                }
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



                    cleanProcessingQueue();

                }
                long mt=cleanProcessingQueue();
                block(mt);
            }

        });


        System.out.println("Endded setup with success");
    }
}