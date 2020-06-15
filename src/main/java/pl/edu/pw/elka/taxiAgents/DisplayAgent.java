package pl.edu.pw.elka.taxiAgents;

import jade.core.*;
import jade.core.Runtime;
import jade.core.behaviours.*;

import jade.wrapper.AgentController;

import jade.lang.acl.*;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import pl.edu.pw.elka.taxiAgents.messages.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class DisplayAgent extends Agent {
    Frame image;
    DisplayFrame window;
    final int TRANSFORMATION_SCALE = 10;
    final int TAXI_TIMEOUT = 10000;

    List<TaxiData> taxis = new ArrayList<>();
    List<ClientData> clients = new ArrayList<>();

    protected void setup() {
        System.out.println("DisplayAgent is running.");

        image = new Frame("City map");
        image.setSize(1000, 1000);
        window = new DisplayFrame();
        image.setBackground(window.getBackgroundColor());
        image.add(window);
        image.setVisible(true);
        image.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        addBehaviour(new Receiver(this));
    }

    void removeTakenClients() {
        long currentTime = System.currentTimeMillis();
        clients.removeIf(client -> client.isTaxiAssigned && client.pickupTime < currentTime);
    }

    void removeMissingTaxis() {
        long currentTime = System.currentTimeMillis();
        taxis.removeIf(taxi -> currentTime - taxi.lastMessageTime > TAXI_TIMEOUT);
    }

    public static void main(String[] args) throws StaleProxyException, IOException {
        // Get a hold on JADE runtime
        Runtime rt = Runtime.instance();
        // Create a default profile
        Profile p = new ProfileImpl();
        // Create a new non-main container, connecting to the default
        // main container (i.e. on this host, port 1099)
        ContainerController cc = rt.createAgentContainer(p);
        // Create a new agent, a DummyAgent
        // and pass it a reference to an Object
        AgentController controller = cc.createNewAgent("display", "pl.edu.pw.elka.taxiAgents.DisplayAgent", null);
        controller.start();
    }

    class DisplayFrame extends Applet {
        final Color TAXI_FREE = Color.green;
        final Color TAXI_GOING_FOR_CLIENT = Color.pink;
        final Color TAXI_WITH_CLIENT = Color.magenta;
        final Color TAXI_OTHER = Color.lightGray;

        final Color CLIENT_UNASSIGNED = Color.yellow;
        final Color CLIENT_ASSIGNED = Color.orange;
        final Color CLIENT_OTHER = Color.lightGray;

        final Color BACKGROUND = Color.darkGray;
        final Color DEFAULT_COLOR = Color.white;




        public void paint (Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(DEFAULT_COLOR);

            for(int i = 0; i < taxis.size(); ++i) {
                g2.setColor(chooseColor(taxis.get(i)));
                plotTaxi(g2, taxis.get(i));
                g2.setColor(DEFAULT_COLOR);
            }

            for(int i = 0; i < clients.size(); ++i) {
                g2.setColor(chooseColor(clients.get(i)));
                plotClient(g2, clients.get(i));
                g2.setColor(DEFAULT_COLOR);
            }

        }

        Color chooseColor(TaxiData taxi) {
            if(taxi.status.equals(TaxiAgent.DRIVER_STATUS_FREE)) return TAXI_FREE;
            else if(taxi.status.equals(TaxiAgent.DRIVER_STATUS_WORKING)) {
                if(taxi.isWithClient) return TAXI_WITH_CLIENT;
                else return TAXI_GOING_FOR_CLIENT;
            }
            else return TAXI_OTHER;
        }

        Color chooseColor(ClientData client) {
            if(client.isTaxiAssigned) return CLIENT_ASSIGNED;
            else return CLIENT_UNASSIGNED;
        }

        void plotTaxi(Graphics2D g2, TaxiData taxi) {
            Position taxiPositionTransformed = new Position(taxi.position.longitude / TRANSFORMATION_SCALE, taxi.position.latitude / TRANSFORMATION_SCALE); //image is 10x smaller than working area
            g2.drawOval(taxiPositionTransformed.longitude, taxiPositionTransformed.latitude, 3,3);
            g2.drawString(taxi.id, taxiPositionTransformed.longitude, taxiPositionTransformed.latitude);
            if(!taxi.route.isEmpty()) {
                GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, taxi.route.size() + 1);
                polyline.moveTo (taxiPositionTransformed.longitude, taxiPositionTransformed.latitude);
                for(Position point : taxi.route) {
                    polyline.lineTo((float)point.longitude / TRANSFORMATION_SCALE, (float)point.latitude / TRANSFORMATION_SCALE); //also transformed
                }
                g2.draw(polyline);
            }
        }

        void plotClient(Graphics2D g2, ClientData client) {
            Position clientPositionTransformed = new Position(client.position.longitude / TRANSFORMATION_SCALE, client.position.latitude / TRANSFORMATION_SCALE);
            g2.drawOval(clientPositionTransformed.longitude, clientPositionTransformed.latitude, 3,3);
            g2.drawString(client.id, clientPositionTransformed.longitude, clientPositionTransformed.latitude);
        }

        public Color getBackgroundColor() {return BACKGROUND;}


    }

    static class TaxiData {
        public String id;
        public String status;
        public Position position;
        public List<Position> route;
        boolean isWithClient;
        long lastMessageTime = 0;

        public TaxiData() {}
        public TaxiData(String id, String status, Position position, List<Position> route, boolean isWithClient) {
            this.id = id;
            this.status = status;
            this.position = position;
            this.route = route;
            this.isWithClient = isWithClient;
        }
    }

    static class ClientData {
        public String id;
        public Position position;
        boolean isTaxiAssigned;
        long pickupTime;

        public ClientData() {}
        public ClientData(String id, Position position, boolean isTaxiAssigned) {
            this.id = id;
            this.position = position;
            this.isTaxiAssigned = isTaxiAssigned;
        }
    }

    class Receiver extends CyclicBehaviour {
        public Receiver(Agent a) {
            super(a);
        }
        public void action() {
            ACLMessage msgI = receive();
            if(msgI != null) {
                try {
                    Object msg = msgI.getContentObject();
                    if(msg instanceof TaxiStatus) {
                        TaxiStatus status = (TaxiStatus) msg;
                        TaxiData taxiInfo = new TaxiData(msgI.getSender().getLocalName(), status.status, status.position, status.route, status.isWithClient);
                        taxiInfo.lastMessageTime = System.currentTimeMillis();
                        boolean taxiFound = false;
                        for(int i = 0; i < taxis.size(); ++i) {
                            if(taxis.get(i).id.equals(taxiInfo.id)) {
                                taxis.set(i, taxiInfo);
                                taxiFound = true;
                                break;
                            }
                        }
                        if(!taxiFound) taxis.add(taxiInfo);
                    }
                    else if(msg instanceof ClientStatus) {
                        ClientStatus status = (ClientStatus) msg;
                        ClientData clientInfo = new ClientData(msgI.getSender().getLocalName(), status.position, status.isTaxiAssigned);
                        clientInfo.pickupTime = System.currentTimeMillis() + (status.timeToPickup + 3) * 1000; //+3 - time for pickup, turnings and arrival
                        boolean clientFound = false;
                        for(int i = 0; i < clients.size(); ++i) {
                            if(clients.get(i).id.equals(clientInfo.id)) {
                                clients.set(i, clientInfo);
                                clientFound = true;
                                break;
                            }
                        }
                        if(!clientFound) clients.add(clientInfo);
                    }
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            }
            removeTakenClients();
            removeMissingTaxis();
            window.repaint();
            block(3000);
        }
    }
}
