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
    //TODO: zaprojektowac cztery pierwsze typy wiadomosci:
    //1. client wzywa taksowke
    //2. taksowka zglasza sie ze jest aktywna
    //3.  taksowka odpowiada call center na temat zgloszenie
    // te trzy pierwsze wiadomosci musza byc rozruznaine w action call center
    //4. call center pyta taksowke o obsuzenie zlecenie
    // 5. call center odpowiada clientowi
    //
    //TODO: dodac liste aktywnych taksowek
    //TODO: dodac mape przetwarzanych zapytan
    // identyfikowac zapytanie z klientem, zeby wiedziec komu odpowiedziec
    // dodac

    String name = "callCenter" ;
    AID customer = new AID( name, AID.ISLOCALNAME );

    private String longitude = "11";

    static class ProcessingQuery{
        String id;
        CallTaxi query;
        Queue<AID> taxisToCheck;
        AID customer;

        public ProcessingQuery(String id, CallTaxi query, Queue<AID> taxisToCheck, AID customer) {
            this.id = id;
            this.query = query;
            this.taxisToCheck = taxisToCheck;
            this.customer = customer;
        }
    }
    AtomicInteger queriesIdsSource=new AtomicInteger(1);

    Map<String,ProcessingQuery> activeQueries=new HashMap<>();

    Set<AID> taxis=new HashSet<>();

    void sendQueryToNextTaxi(ProcessingQuery pq) throws IOException {
        if (!pq.taxisToCheck.isEmpty()){
            AID taxi=pq.taxisToCheck.poll();
            ACLMessage mesg=new ACLMessage(ACLMessage.REQUEST);
            CallCenterToTaxi cct=new CallCenterToTaxi(pq.query.getFromLatitude(), pq.query.getToLatitude(), pq.id);
            mesg.setContentObject(cct);
            mesg.addReceiver(taxi);
            send(mesg);
        } else {
            System.out.println("Brak dostępnych taksówek.");
        }
    }


    //TODO: dodawac do nie j na podstawie wiadomosci2
    protected void setup()
    {



        System.out.println("Agent centrali "+getAID().getName()+" jest gotowy.");
        // Get the longitude of customer
        /*Object[] args = getArguments();
        if(args != null && args.length > 0)
        {longitude = (String) args[0];
        System.out.println("Położenie klienta: "+longitude);
        }else{// Make the agent terminate immediately
             System.out.println("Nie podano położenia klienta");
             doDelete();
        }*/

        // First set-up answering behaviour

        addBehaviour(new CyclicBehaviour(this)
        {
            public void action() {
                // Send messages to "a1" and "a2"
// TODO zrobic casa po mastepijacych wiadomosciach
                ACLMessage msgI= receive();
                // TODO case 1: zapytanie od klienta
                // TODO case 2: zgloszenie taksowki
                // TODO case 3: odpowiedz taksowki kiedy bedzie

                //System.out.println("next message to call center");

                if (msgI!=null) {
                    try {
                        Object mesg=msgI.getContentObject();
                        if (mesg instanceof CallTaxi) {
                            System.out.println("klient query");
                            // TODO: client pyta o taksowke
                            CallTaxi ct=(CallTaxi) mesg;
                            if (taxis.isEmpty()) {
                                System.out.println("Nie mamy żadnej taksówki zarejestrowanej.");
                            } else {
                                ProcessingQuery pq = new ProcessingQuery("" + (queriesIdsSource.getAndIncrement()),
                                        ct,
                                        new LinkedList<>(taxis),
                                        msgI.getSender());
                                activeQueries.put(pq.id, pq);
                                try {
                                    sendQueryToNextTaxi(pq);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (mesg instanceof TaxiToCallCenter) {
                            //System.out.println("Taxi to callcenter");
                            TaxiToCallCenter tcc = (TaxiToCallCenter) mesg;
                            System.out.println("== Answer" + " <- "+tcc.isIfAccepts());
                            if (tcc.isIfAccepts()) {
                                ProcessingQuery pq=activeQueries.get(tcc.getQueryID());
                                activeQueries.remove(tcc.getQueryID());
                                System.out.println("== Answer" + " <- "
                                        + tcc.getTimeToPickUp() + " from "
                                        + tcc.getTaxiPlace() + msgI.getSender().getName());
                                ACLMessage confirmTaxi = new ACLMessage(ACLMessage.INFORM);
                                System.out.println("Informuję taxi o tym, że bierze przejazd:");
                                System.out.println(msgI.getSender().getName());
                                confirmTaxi.addReceiver(msgI.getSender());
                                try {
                                    confirmTaxi.setContentObject(new CallCenterConfirmTaxi(pq.query.getFrom(), pq.query.getTo(), pq.id));
                                    send(confirmTaxi);
                                } catch(IOException e) {
                                    e.printStackTrace();
                                }

                                ACLMessage register=new ACLMessage(ACLMessage.INFORM);
                                register.addReceiver(pq.customer);
                                try {
                                    register.setContentObject(new CallCenterToClient(msgI.getSender().getName(), tcc.getTimeToPickUp()));
                                    send(register);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                /**ACLMessage rmesg=new ACLMessage(ACLMessage.INFORM);
                                rmesg.setContent(tcc.getTimeToPickUp() + " from "
                                        + tcc.getTaxiPlace() + msgI.getSender().getName());
                                rmesg.addReceiver(pq.customer);
                                send(rmesg);**/
                            } else {
                                try {
                                    sendQueryToNextTaxi(activeQueries.get(tcc.getQueryID()));
                                    //System.out.println("Szukalem nastepnej taksowki");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (mesg instanceof TaxiRegister) {
                            taxis.add(msgI.getSender());
                            System.out.println("Rejestruje taksówkę "+msgI.getSender().getName());
                        }

                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }



                }
                block();
            }
        });



    }
}