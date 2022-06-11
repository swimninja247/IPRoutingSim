import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.LinkedList;

public class LSPacket {

    private int senderPort;  // the node that sent the packet (not LSA origin)
    private int originPort;      // the node from which the LSA oringally came from
    private int dest;
    private LinkedList<LinkState> links;
    private int seqNo;

    // Constructor for normal messages
    public LSPacket(int senderPort, int dest, LinkedList<LinkState> links, int seqNo, int originPort) {
        this.senderPort = senderPort;
        this.dest = dest;
        this.links = links;
        this.seqNo = seqNo;
        this.originPort = originPort;
    }

    // Constructor that decodes a normal packet
    public LSPacket(DatagramPacket p) {
        
        this.dest = p.getPort();

        byte[] data = Arrays.copyOfRange(p.getData(), 0, p.getLength());

        // extract senderPort port
        this.senderPort = Byte.toUnsignedInt(data[0]);
        this.senderPort <<= 8;
        this.senderPort |= Byte.toUnsignedInt(data[1]);

        // extract seqNo
        this.seqNo = Byte.toUnsignedInt(data[2]);
        this.seqNo <<= 8;
        this.seqNo |= Byte.toUnsignedInt(data[3]);

        // extract origin port
        this.originPort = Byte.toUnsignedInt(data[4]);
        this.originPort <<= 8;
        this.originPort |= Byte.toUnsignedInt(data[5]);

        // extract linked list of links
        this.links = new LinkedList<>();
        byte[] encodedLink;
        for (int i = 6; i < data.length; i += 6) {
            encodedLink = Arrays.copyOfRange(data, i, i + LinkState.ENCODING_SIZE);
            this.links.add(new LinkState(encodedLink));
        }
    }

    public DatagramPacket packet() {
        
        InetAddress destIP = null;
        byte[] data = this.createData();

        try {
            destIP = InetAddress.getLocalHost();
        } catch (Exception e) {
            System.out.println("[X] Could not resolve localhost.");
            return null;
        }

        return new DatagramPacket(data, data.length, destIP, dest);
    }


    /*
     * packet will be senderPort for 2 bytes, seq for two bytes, , origin port for two bytes, then list of link states (6 bytes each)
     */
    private byte[] createData() {
        
        byte[] data = new byte[2 + 2 + 2 + this.links.size() * LinkState.ENCODING_SIZE];

        // senderPort port
        data[0] = (byte) (this.senderPort >> 8);
        data[1] = (byte) (this.senderPort & 0xFF);

        // sequence Number
        data[2] = (byte) (this.seqNo >> 8);
        data[3] = (byte) (this.seqNo & 0xFF);

        // origin Port
        data[4] = (byte) (this.originPort >> 8);
        data[5] = (byte) (this.originPort & 0xFF);

        // links list
        int i = 6;
        byte[] encodedLink;
        for (LinkState link : this.links) {
            encodedLink = link.encode();

            // add the encoded link to the list
            for (byte n : encodedLink) {
                data[i++] = n;
            }
        }

        return data;
    }

    public int getSenderPort() {
        return this.senderPort;
    }

    public int getDest() {
        return this.dest;
    }

    public LinkedList<LinkState> getLinks() {
        return this.links;
    }

    public int getSeqNo() {
        return this.seqNo;
    }

    public int getOriginPort() {
        return this.originPort;
    }

    public static void main(String args[]) {
        LSTable table = new LSTable(1);
        table.addLink(new LinkState(1, 2, 1));
        table.addLink(new LinkState(1, 3, 50));
        table.addLink(new LinkState(2, 3, 2));
        table.addLink(new LinkState(2, 4, 8));
        table.addLink(new LinkState(3, 4, 5));

        LSPacket lsp = new LSPacket(1, 2, table.getLinks(), 3, 5);
        System.out.println(lsp);

        lsp = new LSPacket(lsp.packet());
        System.out.println(lsp);

    }

    @Override
    public String toString() {
        return "LSPacket [dest=" + dest + ", links:\n" + links + ",\nseqNo=" + seqNo + ", senderPort=" + senderPort + ", originPort " + originPort + "]";
    }
}
