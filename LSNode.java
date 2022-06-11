import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.LinkedList;

public class LSNode {
    
    private LSTable table;
    private LinkedList<LinkState> links;
    private LSASeqTable seqTable;
    private int[] neighbors;
    private int seqNo;  // next sequence number for this node to send

    private int port;
    private DatagramSocket sock;
    private boolean firstShare = false;  // has the table been shared yet?
    private boolean routingIntervalPassed = false; // has the initial routing interval elapsed?

    private static final int ROUTING_INTERVAL = 30;  // interval at which dijkstra's is calculated
    private int UPDATE_INTERVAL;  // in milliseconds


    public LSNode(int port, LinkedList<LinkState> links, int timeout) {
        this.port = port;
        
        this.links = links;

        // initialize LSTable and neighbors array
        this.table = new LSTable(port);
        this.neighbors = new int[links.size()];
        int i = 0;
        for (LinkState link : links) {
            this.table.addLink(link);
            this.neighbors[i++] = link.getPorts()[1];
        }
        System.out.println(this.table);

        this.seqTable = new LSASeqTable();

        this.seqNo = 0;
        
        try {
            this.sock = new DatagramSocket(port);
            this.sock.setSoTimeout(2000);
        } catch (Exception e) {
            System.out.println("Error occurred creating socket for Node object.\n" + e);
        }

        this.UPDATE_INTERVAL = timeout;

        // start routing interval timer
        RoutingTimer timer = new RoutingTimer();
        Thread routeTimerThread = new Thread(timer);
        routeTimerThread.start();

        UpdateTimer updateTimer = new UpdateTimer();
        Thread updateTimerThread = new Thread(updateTimer);
        updateTimerThread.start();
    }

    private void send(DatagramPacket p) {
        Sender sender = new Sender(p);
        Thread sendThread = new Thread(sender);
        sendThread.start();
    }

    // Listen for and handle packet by passing to packet handler object
    public void receive() {
        
        byte[] buf = new byte[512];
        DatagramPacket rPack = new DatagramPacket(buf, buf.length);

        try {
            this.sock.receive(rPack);

            // broadcast to neighbors if first share
            if (!firstShare) {
                broadcastTable();
                firstShare = true;
            }


            // extract data from packet
            LSPacket rLSPack = new LSPacket(rPack);
            int seqNo = rLSPack.getSeqNo();
            int senderPort = rLSPack.getSenderPort();  // sender of the LSA
            int originPort = rLSPack.getOriginPort();  // origin of LSA

            // check if seqNo is correct, log appropriately
            if (seqNo < seqTable.get(originPort)) {
                Date date = new Date();
                System.out.println("[" + date.getTime() + "] DUPLICATE LSA packet received, AND DROPPED:\n" + "- LSA of node " + originPort + "\n- Sequence Number " + seqNo + "\n- Received from " + senderPort); // packet from which LSA is received
                return;
            }

            if (originPort == this.port) {
                return;
            }

            // Log normal packet receipt
            Date date = new Date();
            System.out.println("[" + date.getTime() + "] LSA of Node " + originPort + " with sequence number " + seqNo + " received from Node " + senderPort);

            // update LStable, LSAseqtable
            seqTable.increment(originPort);

            boolean change = false;

            LinkedList<LinkState> links = rLSPack.getLinks();
            for (LinkState link : links) {
                if (table.addLink(link) == false) {
                    // link is already in table, so update it
                    // but can only update if its not one of this node's original links
                    /*boolean cantChange = false;
                    for (LinkState myLink : this.links) {
                        int[] currentPorts = link.getPorts();
                        int[] newPorts = myLink.getPorts();

                        if ((currentPorts[0] == newPorts[0] && currentPorts[1] == newPorts[1]) || 
                                (currentPorts[1] == newPorts[0] && currentPorts[0] == newPorts[1])) {
                            
                            cantChange = true;
                        }
                    }
                    if (cantChange) {
                        continue;
                    }*/

                    if (table.updateLink(link)) {
                        change = true;
                    }
                } else {
                    change = true;
                }
            }


            if (change) {
                System.out.println(table);
            }            

            // forward the table
            for (int neighbor : neighbors) {
                // except the port it received from
                if (neighbor == senderPort) {
                    continue;
                }
                
                LSPacket sLSPacket = new LSPacket(port, neighbor, links, seqNo, originPort);

                send(sLSPacket.packet());
                System.out.println("[" + date.getTime() + "] LSA of Node " + rLSPack.getOriginPort() + " with sequence number " + seqNo + " sent to Node " + neighbor);
            }

            // if topology changed and past routing interval show change
            if (routingIntervalPassed && change) {
                System.out.println(table.djikstra());
            }

            //PacketHandler handler = new PacketHandler(rPack);
            //Thread handleThread = new Thread(handler);
            //handleThread.start();

        } catch (SocketTimeoutException e) {
            ;
        } catch (IOException e) {
            System.out.println("Error receiving packet.\n" + e);
        }
    }

    private class RoutingTimer implements Runnable {

        public RoutingTimer() {
            ;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(ROUTING_INTERVAL * 1000);
            } catch (Exception e) {
                ;
            }
            routingIntervalPassed = true;
            System.out.println(table.djikstra());
        }

    }

    private class UpdateTimer implements Runnable {

        public UpdateTimer() {
            ;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(UPDATE_INTERVAL);
                } catch (Exception e) {
                    System.out.println("Counldn't sleep");
                }
                broadcastTable();
            }
            
        }

    }

    // Sends table to all neighbors
    public void broadcastTable() {

        for (int neighbor : this.neighbors) {
            LSPacket sLSPack = new LSPacket(this.port, neighbor, this.links, this.seqNo, this.port);
            this.send(sLSPack.packet());

            // Log message send
            Date date = new Date();
            System.out.println("[" + date.getTime() + "] LSA of Node " + this.port + " with sequence number " + this.seqNo + " sent to Node " + neighbor);
        }

        this.seqNo++;
        this.firstShare = true;
    }

    public int getPort() {
        return this.port;
    }


    // update the link cost of the link to the highest neighbor
    public void linkCostChange(int cost) {

        // find highest neighbor
        int highest = 0;
        for (int neighbor : this.neighbors) {
            highest = Math.max(highest, neighbor);
        }

        LinkState newLink = new LinkState(this.port, highest, cost);
        this.table.updateLink(newLink);

        // find the link in the instance's list
        for (LinkState link : this.links) {
            int[] currentPorts = link.getPorts();
            int[] newPorts = newLink.getPorts();

            // linearly search list
            if ((currentPorts[0] == newPorts[0] && currentPorts[1] == newPorts[1]) || 
                    (currentPorts[1] == newPorts[0] && currentPorts[0] == newPorts[1])) {
                
                link.setCost(cost);
                
            }
        }

        Date date = new Date();
        System.out.println("[" + date.getTime() + "] Node " + highest + " cost updated to " + cost);
        broadcastTable();
        System.out.println(this.table);
        System.out.println(this.table.djikstra());
    }
        
}
