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

/**
 * Agent responsible for displaying taxis and clients. During start it creates 1000x1000 window, that represents
 * working area in scale 1:10.
 * Each taxi or client appears as soon as it sends its status to DisplayAgent. Client is removed from display when it's
 * picked up by a taxi. Taxi is removed if it hasn't sent it's status for at least 10 seconds.
 * DisplayAgent is closed when the window is closed.
 */
public class DisplayAgent extends Agent {
    Frame image; // displayed image
    DisplayFrame window; // displayed window
    final int TRANSFORMATION_SCALE = 10; // scale 1:TRANSFORMATION_SCALE
    final int TAXI_TIMEOUT = 10000; // time (in ms) after which "dead" taxi is removed

    List<TaxiData> taxis = new ArrayList<>(); // list with shown taxis
    List<ClientData> clients = new ArrayList<>(); // list with shown clients

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

    /**
     * Client is removed, when it's pickup time is less than taxi arrival.
     */
    void removeTakenClients() {
        long currentTime = System.currentTimeMillis();
        clients.removeIf(client -> client.isTaxiAssigned && client.pickupTime < currentTime);
    }

    /**
     * Taxi is removed if time since last status message is larger than specified
     */
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
        ContainerController cc = rt.createAgentContainer(p);
        // Create a new agent
        AgentController controller = cc.createNewAgent("display", "pl.edu.pw.elka.taxiAgents.DisplayAgent", null);
        controller.start();
    }

    /**
     * Class responsible for graphics
     */
    class DisplayFrame extends Applet {
        // definitions of colors
        final Color TAXI_FREE = Color.green;
        final Color TAXI_GOING_FOR_CLIENT = Color.pink;
        final Color TAXI_WITH_CLIENT = Color.magenta;
        final Color TAXI_OTHER = Color.lightGray;

        final Color CLIENT_UNASSIGNED = Color.yellow;
        final Color CLIENT_ASSIGNED = Color.orange;
        final Color CLIENT_OTHER = Color.lightGray;

        final Color BACKGROUND = Color.darkGray;
        final Color DEFAULT_COLOR = Color.white;



        // method invoked while repaint()
        public void paint (Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(DEFAULT_COLOR);

            for(int i = 0; i < taxis.size(); ++i) { // enhanced for loop causes memory access exceptions
                g2.setColor(chooseColor(taxis.get(i)));
                plotTaxi(g2, taxis.get(i));
                g2.setColor(DEFAULT_COLOR);
            }

            for(int i = 0; i < clients.size(); ++i) { // enhanced for loop causes memory access exceptions
                g2.setColor(chooseColor(clients.get(i)));
                plotClient(g2, clients.get(i));
                g2.setColor(DEFAULT_COLOR);
            }

        }

        /**
         * Choosing proper color for given taxi, according to its status.
         * @param taxi Given taxi.
         * @return Color.
         */
        Color chooseColor(TaxiData taxi) {
            if(taxi.status.equals(TaxiAgent.DRIVER_STATUS_FREE)) return TAXI_FREE;
            else if(taxi.status.equals(TaxiAgent.DRIVER_STATUS_WORKING)) {
                if(taxi.isWithClient) return TAXI_WITH_CLIENT;
                else return TAXI_GOING_FOR_CLIENT;
            }
            else return TAXI_OTHER;
        }

        /**
         * Choosing proper color for given client, according to its status.
         * @param client Given client.
         * @return Color.
         */
        Color chooseColor(ClientData client) {
            if(client.isTaxiAssigned) return CLIENT_ASSIGNED;
            else return CLIENT_UNASSIGNED;
        }

        /**
         * Plots taxi position, taxi id and taxi route (if moving).
         * @param g2 Graphics.
         * @param taxi Given taxi.
         */
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

        /**
         * Plots client position and id.
         * @param g2 Graphics.
         * @param client Given client.
         */
        void plotClient(Graphics2D g2, ClientData client) {
            Position clientPositionTransformed = new Position(client.position.longitude / TRANSFORMATION_SCALE, client.position.latitude / TRANSFORMATION_SCALE);
            g2.drawOval(clientPositionTransformed.longitude, clientPositionTransformed.latitude, 3,3);
            g2.drawString(client.id, clientPositionTransformed.longitude, clientPositionTransformed.latitude);
        }

        /**
         * Returns predefined background color.
         * @return Background color.
         */
        public Color getBackgroundColor() {return BACKGROUND;}
    }

    /**
     * Structure containing single taxi data.
     */
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

    /**
     * Structure containing single client data.
     */
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

    /**
     * Class responsible for receiving messages.
     */
    class Receiver extends CyclicBehaviour {
        public Receiver(Agent a) {
            super(a);
        }
        public void action() {
            ACLMessage msgI = receive();
            if(msgI != null) {
                try {
                    Object msg = msgI.getContentObject();
                    if(msg instanceof TaxiStatus) { // taxi status received
                        TaxiStatus status = (TaxiStatus) msg;
                        TaxiData taxiInfo = new TaxiData(msgI.getSender().getLocalName(), status.status, status.position, status.route, status.isWithClient);
                        taxiInfo.lastMessageTime = System.currentTimeMillis();
                        boolean taxiFound = false;
                        for(int i = 0; i < taxis.size(); ++i) { // deciding if taxi is new or already known
                            if(taxis.get(i).id.equals(taxiInfo.id)) {
                                taxis.set(i, taxiInfo);
                                taxiFound = true;
                                break;
                            }
                        }
                        if(!taxiFound) taxis.add(taxiInfo);
                    }
                    else if(msg instanceof ClientStatus) { // client status received
                        ClientStatus status = (ClientStatus) msg;
                        ClientData clientInfo = new ClientData(msgI.getSender().getLocalName(), status.position, status.isTaxiAssigned);
                        clientInfo.pickupTime = System.currentTimeMillis() + (status.timeToPickup + 3) * 1000; //+3 - time for pickup, turnings and arrival
                        boolean clientFound = false;
                        for(int i = 0; i < clients.size(); ++i) { // deciding if client is new or already known
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
            removeTakenClients(); // removing taken clients
            removeMissingTaxis(); // removing "dead" taxis
            window.repaint(); // refreshing image
            block(3000); // delay is specified in order to remove "dead" taxis while no messages arriving
        }
    }
}
