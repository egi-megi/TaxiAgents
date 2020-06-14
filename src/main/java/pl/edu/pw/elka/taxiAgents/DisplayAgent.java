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

            taxis.addAll(Arrays.asList(
                    new TaxiData("Taxi1", TaxiAgent.DRIVER_STATUS_FREE, new Position(30, 30), Collections.emptyList(), false),
                    new TaxiData("Taxi2", TaxiAgent.DRIVER_STATUS_FREE, new Position(900, 900), Collections.emptyList(), false),
                    new TaxiData("Taxi3", TaxiAgent.DRIVER_STATUS_WORKING, new Position(100, 200), Arrays.asList(new Position(100, 500), new Position(500, 500)), false),
                    new TaxiData("Taxi4", TaxiAgent.DRIVER_STATUS_WORKING, new Position(900, 200), Arrays.asList(new Position(900, 300), new Position(300, 300)), true),
                    new TaxiData("Taxi5", TaxiAgent.DRIVER_STATUS_BREAK, new Position(800, 200), Collections.emptyList(), true),
                    new TaxiData("Taxi6", TaxiAgent.DRIVER_STATUS_FREE, new Position(700, 50),Collections.emptyList(), false)
            ));

            clients.addAll(Arrays.asList(
                    new ClientData("Client1", new Position(30, 90), false),
                    new ClientData("Client2", new Position(150, 290),true),
                    new ClientData("Client3", new Position(10, 870), false),
                    new ClientData("Client4", new Position(800, 20), true)
            ));

            for(TaxiData taxi : taxis) {
                g2.setColor(chooseColor(taxi));
                plotTaxi(g2, taxi);
                g2.setColor(DEFAULT_COLOR);
            }

            for(ClientData client : clients) {
                g2.setColor(chooseColor(client));
                plotClient(g2, client);
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
            g2.drawOval(taxi.position.longitude, taxi.position.latitude, 3,3);
            g2.drawString(taxi.id, taxi.position.longitude, taxi.position.latitude);
            if(!taxi.route.isEmpty()) {
                GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, taxi.route.size() + 1);
                polyline.moveTo (taxi.position.longitude, taxi.position.latitude);
                for(Position point : taxi.route) {
                    polyline.lineTo(point.longitude, point.latitude);
                }
                g2.draw(polyline);
            }
        }

        void plotClient(Graphics2D g2, ClientData client) {
            g2.drawOval(client.position.longitude, client.position.latitude, 3,3);
            g2.drawString(client.id, client.position.longitude, client.position.latitude);
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
                        for(TaxiData taxi : taxis) {
                            if(taxi.id.equals(taxiInfo.id)) {
                                taxi = taxiInfo;
                                taxiFound = true;
                                break;
                            }
                        }
                        if(!taxiFound) taxis.add(taxiInfo);
                        window.repaint();
                    }
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            }
            block();
        }
    }
}
