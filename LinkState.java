public class LinkState {

    private int src;      // Node from which this edge points
    private int dest;     // Node to which this edge points
    private int cost;      // Total cost of the edge
    public static final int ENCODING_SIZE = 6; // a LinkState object is encoded into a 6-byte array

    public LinkState(int src, int dest, int cost) {
        this.src = src;
        this.dest = dest;
        this.cost = cost;
    }

    public LinkState(byte[] encoded) {
        this.src = Byte.toUnsignedInt(encoded[0]);
        this.src <<= 8;
        this.src |= Byte.toUnsignedInt(encoded[1]);

        this.dest = Byte.toUnsignedInt(encoded[2]);
        this.dest <<= 8;
        this.dest |= Byte.toUnsignedInt(encoded[3]);

        this.cost = Byte.toUnsignedInt(encoded[4]);
        this.cost <<= 8;
        this.cost |= Byte.toUnsignedInt(encoded[5]);
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int[] getPorts() {
        return new int[]{this.src, this.dest};
    }

    public int getCost() {
        return this.cost;
    }

    public byte[] encode() {
        byte[] output = new byte[6];

        output[0] = (byte) (this.src >> 8);
        output[1] = (byte) (this.src & 0xFF);

        output[2] = (byte) (this.dest >> 8);
        output[3] = (byte) (this.dest & 0xFF);

        output[4] = (byte) (this.cost >> 8);
        output[5] = (byte) (this.cost & 0xFF);

        return output;
    }


    
    @Override
    public String toString() {
        String output = "Node " + this.src + " (" + this.cost + ")" + " <-> Node " + this.dest + ";";
        return output;
    }

    public static void main(String[] args) {
        LinkState poop = new LinkState(1, 2, 3);
        System.out.println(poop);
        LinkState poopoo = new LinkState(poop.encode());
        System.out.println(poopoo);
    }
}