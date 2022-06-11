import java.net.DatagramPacket;
import java.net.InetAddress;

public class DVPacket {

    private int source;
    private int dest;
    private DistanceVector vector;
    private boolean special = false;

    // Constructor for normal messages
    public DVPacket(int source, int dest, DistanceVector vector) {
        this.source = source;
        this.dest = dest;
        this.vector = vector;
    }

    // Constructor for triggered link cost message
    public DVPacket(int source, int dest, DistanceVector vector, boolean special) {
        this.source = source;
        this.dest = dest;
        this.vector = vector;
        this.special = true;
    }

    // Constructor that decodes a normal packet
    public DVPacket(DatagramPacket p) {
        
        this.dest = p.getPort();

        byte[] data = p.getData();
        
        this.special = (data[0] == 1) ? true : false;

        this.source = Byte.toUnsignedInt(data[1]);
        this.source <<= 8;
        this.source |= Byte.toUnsignedInt(data[2]);

        byte[] vector = new byte[data.length - 3];
        for (int i = 3; i < data.length; i++) {
            vector[i-3] = data[i];
        }

        this.vector = new DistanceVector(vector);
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
     * packet will be port for 2 bytes then data
     */
    private byte[] createData() {
        
        byte[] encoded = this.vector.encode();
        byte[] data = new byte[3 + encoded.length];

        data[0] = (byte) ((this.special) ? 1 : 0);
        data[1] = (byte) (this.source >> 8);
        data[2] = (byte) (this.source & 0xFF);

        for (int i = 3; i < data.length; i++) {
            data[i] = encoded[i-3];
        }

        return data;
    }

    public int getSource() {
        return this.source;
    }

    public int getDest() {
        return this.dest;
    }

    public DistanceVector getVector() {
        return this.vector;
    }

    public boolean getSpecial() {
        return this.special;
    }

    public static void main(String args[]) {
        DistanceVector poop = new DistanceVector(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        System.out.println(poop);

        DVPacket dvp = new DVPacket(1, 2, poop);
        System.out.println(dvp);

        dvp = new DVPacket(dvp.packet());
        System.out.println(dvp);

    }

    @Override
    public String toString() {
        return "DVPacket [dest=" + dest + ", source=" + source + ", vector=" + vector + ", special=" + this.special + "]";
    }
    
}
