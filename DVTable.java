import java.util.LinkedList;
import java.util.Date;


public class DVTable {

    private int node;
    private LinkedList<DistanceVector> nodes; // represents distance vectors for best path
    private LinkedList<DistanceVector> links; // represents physical links

    public DVTable(int node) {
        this.node = node;
        this.nodes = new LinkedList<>();
        this.links = new LinkedList<>();
    }

    public void addLink(DistanceVector dv) {
        this.links.add(dv);
    }

    public void add(DistanceVector dv) {
        this.nodes.add(dv);
        // Sort the list
        this.nodes.sort((dv1, dv2) -> dv1.getPort() - dv2.getPort());
    }

    public void updateLink(DistanceVector dv) {
        int oldCost = this.getLink(dv.getPort()).getCost();
        int newCost = dv.getCost();
        int port = dv.getPort();
        this.updateLinkCost(dv);
        
        // update any existing paths using this link
        for (DistanceVector node: this.nodes) {
            if (node.getNextHop() == port) {
                node.setCost(node.getCost() + newCost - oldCost);
                
                // if it has a link, make sure its not shorter
                if (this.getLink(node.getPort()) != null) {
                    if (node.getCost() > this.getLink(node.getPort()).getCost()) {
                        node.setCost(this.getLink(node.getPort()).getCost());
                        node.setNextHop(port);
                    }
                }
            }
        }
    }

    // Updates table given a vector and source for the vector
    // returns a list of vectors affected
    public DistanceVector updateTable(DistanceVector dv, int src) {

        int dest = dv.getPort();

        if (dest == this.node) {
            return null;
        }

        int newCost = dv.getCost() + this.get(src).getCost();

        // newNextHop is the nextHop to the source of the vector
        int newNextHop = this.get(src).getNextHop();
        DistanceVector newVector = new DistanceVector(dv.getPort(), newCost, newNextHop);

        // if the node currently has no vector to the destination make new vector
        if (this.get(dest) == null) {
            this.add(newVector);
            System.out.println(this);
            return newVector;
        }

        int currentCost = this.get(dest).getCost();

        // Here if vector to dest exists

        // if the vector exists with same path but with a different cost overwrite and return
        // only return not null if costs arent equal
        if (this.get(dv.getPort()).getNextHop() == src) {
            newVector = new DistanceVector(dv.getPort(), newCost, src);
            
            // if there is a physical link thats shorter use this
            if (this.getLink(dv.getPort()) != null) {
                if (this.getLink(dv.getPort()).getCost() < newCost) {
                    newVector = this.getLink(dv.getPort());
                }
            }

            // check that its actually a new vector
            if (newCost == currentCost) {
                return null;
            } else {
                this.updateVector(newVector);
                System.out.println("Overwriting old path" + this);
                return newVector;
            }
        }

        // if the new cost is shorter return the new cost otherwise null
        if (newCost < currentCost) {
            this.updateVector(newVector);
            System.out.println(this);
            return newVector;
        } else {
            return null;
        }
        
    }

    // Updates a vectors fields given a vector with the same port
    public void updateVector(DistanceVector dv) {
        for (int i = 0; i < this.nodes.size(); i++) {
            if (nodes.get(i).getPort() == dv.getPort()) {
                this.nodes.set(i, dv);
            }
        }
    }

    // Updates a link given distance vector
    private void updateLinkCost(DistanceVector dv) {
        for (int i = 0; i < this.links.size(); i++) {
            if (links.get(i).getPort() == dv.getPort()) {
                this.links.set(i, dv);
            }
        }
    }

    // return the distance vector pointing to the specified port or null
    public DistanceVector get(int port) {
        for (int i = 0; i < this.nodes.size(); i++) {
            if (nodes.get(i).getPort() == port) {
                return this.nodes.get(i);
            }
        }
        return null;
    }

    private DistanceVector getLink(int port) {
        for (int i = 0; i < this.links.size(); i++) {
            if (links.get(i).getPort() == port) {
                return this.links.get(i);
            }
        }
        return null;
    }

    public int getNode() {
        return this.node;
    }

    public synchronized LinkedList<DistanceVector> getNodes() {
        return this.nodes;
    }

    @Override
    public synchronized String toString() {

        Date date = new Date();
        String output = "[" + date.getTime() + "] Node " + this.node + " Routing Table\n";

        for (DistanceVector node : this.nodes) {
            output += "- " + node.toString() + '\n';
        }

        return output;
    }

    public static void main(String[] args) {
        DVTable table = new DVTable(1234);
        DistanceVector poop = new DistanceVector(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        table.add(poop);
        System.out.println(table);
    }

}
