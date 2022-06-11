import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Date;

public class DVNode {
    
    private DVTable table;
    private int port;
    private DatagramSocket sock;
    private int[] neighbors;
    private boolean firstShare = false;  // has the table been shared yet?
    private boolean poison = false;


    public DVNode(int port, int[] neighbors, DistanceVector[] initialVectors) {
        this.port = port;
        
        this.table = new DVTable(port);
        
        this.neighbors = neighbors;
        
        try {
            this.sock = new DatagramSocket(port);
            this.sock.setSoTimeout(2000);
        } catch (Exception e) {
            System.out.println("Error occurred creating socket for Node object.\n" + e);
        }

        for (DistanceVector v : initialVectors) {
            this.table.add(v);
            this.table.addLink(v);
        }
    }

    public DVNode(int port, int[] neighbors, DistanceVector[] initialVectors, boolean poison) {
        this.port = port;
        
        this.table = new DVTable(port);
        
        this.neighbors = neighbors;

        this.poison = poison;
        
        try {
            this.sock = new DatagramSocket(port);
            this.sock.setSoTimeout(2000);
        } catch (Exception e) {
            System.out.println("Error occurred creating socket for Node object.\n" + e);
        }

        for (DistanceVector v : initialVectors) {
            this.table.add(v);
            this.table.addLink(v);
        }
    }

    private void send(DatagramPacket p) {
        Sender sender = new Sender(p);
        Thread sendThread = new Thread(sender);
        sendThread.start();
    }

    private void sendSpecial(int destPort, DistanceVector dv) {
        DVPacket sDV = new DVPacket(this.port, destPort, dv, true);
        DatagramPacket sPack = sDV.packet();
        this.send(sPack);
        Date date = new Date();
        System.out.println("[" + date.getTime() + "] Link value message sent from Node " + port + " to Node " + destPort);
    }

    // helper method to return highest neighboring port
    private int getHighestNeighbor() {
        int highest = 0;
        for (int neighbor : this.neighbors) {
            highest = Math.max(highest, neighbor);
        }
        return highest;
    }

    public void linkCostChange(int cost) {
        int port = this.getHighestNeighbor();
        DistanceVector dv = new DistanceVector(port, cost, port);
        this.sendSpecial(this.port, dv);
        dv = new DistanceVector(this.port, cost, this.port);
        this.sendSpecial(port, dv);
    }

    // Listen for and handle packet
    public void receive() {
        
        byte[] buf = new byte[512];
        DatagramPacket rPack = new DatagramPacket(buf, buf.length);

        try {
            this.sock.receive(rPack);
            PacketHandler handler = new PacketHandler(rPack);
            Thread handleThread = new Thread(handler);
            handleThread.start();

        } catch (SocketTimeoutException e) {
            ;
        } catch (IOException e) {
            System.out.println("Error receiving packet.\n" + e);
        }
    }

    private class PacketHandler implements Runnable {

        DatagramPacket packet;

        public PacketHandler(DatagramPacket p) {
            this.packet = p;
        }

        @Override
        public void run() {

            // parse packet
            DVPacket rDVPack = new DVPacket(this.packet);
            DistanceVector rDV = rDVPack.getVector();
            int sourcePort = rDVPack.getSource();

            // Log receipt message
            // if this is a triggered link cost change print special status message
            // else print normal message
            Date date = new Date();
            long time = date.getTime();
            DistanceVector sDV;

            if (rDVPack.getSpecial()) {
                System.out.println("[" + date.getTime() + "] Link value message received at Node " + port + " from Node " + sourcePort);
                // force table update
                sDV = rDV;
                table.updateLink(rDV);
                System.out.println(table);
                broadcastTable();
                return;
            } else {
                // System.out.println("[" + date.getTime() + "] Message received at Node " + port + " from Node " + sourcePort);
                // update table normally
                sDV = table.updateTable(rDV, sourcePort);
            }

            // if the initial share has not been made share the whole table, otherwise just sDV
            if (!firstShare) {
                System.out.println("[" + time + "] Message received at Node " + port + " from Node " + sourcePort);
                broadcastTable();
                firstShare = true;
            } else if (sDV != null) {
                
                System.out.println("[" + time + "] Message received at Node " + port + " from Node " + sourcePort);

                // Send to all neighbors
                DVPacket sDVPack;
                DatagramPacket sPack;
                for (int neighbor : neighbors) {
                    
                    // Check if this message should be altered for poisoned reverse
                    if (poison && sDV.getNextHop() == neighbor) {
                        DistanceVector poisonSDV = new DistanceVector(sDV.getPort(), Integer.MAX_VALUE, sDV.getNextHop());
                        sDVPack = new DVPacket(port, neighbor, poisonSDV);
                        sPack = sDVPack.packet();
                        send(sPack);

                        // Log message send
                        date = new Date();
                        System.out.println("[" + date.getTime() + "] Message sent from Node " + port + " to Node " + neighbor + " with distance to " + poisonSDV.getPort() + " as inf");
                    } else {
                        sDVPack = new DVPacket(port, neighbor, sDV);
                        sPack = sDVPack.packet();
                        // send(sPack);
                        broadcastTable();/////////////////////////////////////////////////////////////////////////// this is annoying but it makes things work

                        // Log message send
                        // date = new Date();
                        // System.out.println("[" + date.getTime() + "] Message sent from Node " + port + " to Node " + neighbor);
                    }
                }
            }            
        }
    }

    // Sends table to all neighbors
    // Used when this is the last node to be created or for first share
    public void broadcastTable() {

        for (int neighbor : this.neighbors) {
            for (DistanceVector dv : this.table.getNodes()) {
                DVPacket sDVPack = new DVPacket(this.port, neighbor, dv);
                DatagramPacket sPack = sDVPack.packet();
                send(sPack);
            }
            // Log message send
            Date date = new Date();
            System.out.println("[" + date.getTime() + "] Broadcast sent from Node " + port + " to Node " + neighbor);
        }

        this.firstShare = true;
    }

    public DVTable getTable() {
        return this.table;
    }

    public int[] getNeighbors() {
        return this.neighbors;
    }

    public int getPort() {
        return this.port;
    }
        
}
