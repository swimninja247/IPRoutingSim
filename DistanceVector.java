public class DistanceVector {

    private int port;      // Node to which this vector points
    private int cost;      // Total cost to reach the node
    private int nextHop;   // Next hop in the path to the node
                           // , will equal port if the next hop is the dest

    public DistanceVector(int port, int cost, int nextHop) {
        this.port = port;
        this.cost = cost;
        this.nextHop = nextHop;
    }

    public DistanceVector(byte[] encoded) {
        this.port = Byte.toUnsignedInt(encoded[0]);
        this.port <<= 8;
        this.port |= Byte.toUnsignedInt(encoded[1]);

        this.cost = Byte.toUnsignedInt(encoded[2]);
        this.cost <<= 8;
        this.cost |= Byte.toUnsignedInt(encoded[3]);

        this.nextHop = Byte.toUnsignedInt(encoded[4]);
        this.nextHop <<= 8;
        this.nextHop |= Byte.toUnsignedInt(encoded[5]);
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public void setNextHop(int nextHop) {
        this.nextHop = nextHop;
    }

    public int getPort() {
        return this.port;
    }

    public int getCost() {
        return this.cost;
    }

    public int getNextHop() {
        return this.nextHop;
    }

    public byte[] encode() {
        byte[] output = new byte[6];

        output[0] = (byte) (this.port >> 8);
        output[1] = (byte) (this.port & 0xFF);

        output[2] = (byte) (this.cost >> 8);
        output[3] = (byte) (this.cost & 0xFF);

        output[4] = (byte) (this.nextHop >> 8);
        output[5] = (byte) (this.nextHop & 0xFF);

        return output;
    }

    @Override
    public String toString() {
        String output = "(" + this.cost + ")" + " -> Node " + this.port + ";";
        if (this.nextHop != this.port) {
            output += " Next hop -> Node " + this.nextHop;
        }
        return output;
    }

    public static void main(String[] args) {
        DistanceVector poop = new DistanceVector(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        System.out.println(poop);
        DistanceVector poopoo = new DistanceVector(poop.encode());
        System.out.println(poopoo);
    }
}