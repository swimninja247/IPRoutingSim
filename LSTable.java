import java.util.Date;
import java.util.LinkedList;


public class LSTable {

    private int port;
    private LinkedList<Integer> nodes;
    private LinkedList<LinkState> links; // represents edges in the graph

    public LSTable(int port) {
        this.port = port;
        this.nodes = new LinkedList<>();
        this.links = new LinkedList<>();
    }

    public void sortLinks() {
        // Sort the list
        this.nodes.sort((node1, node2) -> node1.compareTo(node2));
    }
    
    // Updates a links fields given a link with the same ports, returns whether the link was updated
    public boolean updateLink(LinkState link) {
        for (int i = 0; i < this.links.size(); i++) {
            int[] currentPorts = links.get(i).getPorts();
            int[] newPorts = link.getPorts();

            // linearly search list
            if ((currentPorts[0] == newPorts[0] && currentPorts[1] == newPorts[1]) || 
                    (currentPorts[1] == newPorts[0] && currentPorts[0] == newPorts[1])) {
                
                // check if the link was the same cost
                if (links.get(i).getCost() == link.getCost()) {
                    return false;
                } else {
                    this.links.set(i, link);
                    return true;
                }
                
            }
        }

        return false;
    }

    // returns a link if it has identical endpoints (regardless of order) as input, null otherwise
    public LinkState getLink(LinkState link) {
        for (int i = 0; i < this.links.size(); i++) {
            int[] currentPorts = links.get(i).getPorts();
            int[] newPorts = link.getPorts();

            if ((currentPorts[0] == newPorts[0] && currentPorts[1] == newPorts[1]) || 
                    (currentPorts[1] == newPorts[0] && currentPorts[0] == newPorts[1])) {
                
                return this.links.get(i);
            }
        }

        return null;
    }

    public Integer getNode(int node) {
        for (int i = 0; i < this.nodes.size(); i++) {
            if (node == this.nodes.get(i)) {
                
                return this.nodes.get(i);
            }
        }

        return null;
    }

    // adds link and node to both, returns whether not anything new was added
    public boolean addLink(LinkState link) {
        if (this.getLink(link) == null) {
            this.links.add(link);

            int[] ports = link.getPorts();

            if (this.getNode(ports[0]) == null) {
                this.nodes.add(ports[0]);
            }
            if (this.getNode(ports[1]) == null) {
                this.nodes.add(ports[1]);
            }

            return true;
        }

        return false;
    }

    // the logic of LSA
    public LSPathTable djikstra() {
        LSPathTable output = new LSPathTable(this.port);

        LinkedList<Integer> queue = new LinkedList<>();
        LinkedList<Integer> processed = new LinkedList<>();

        queue.offer(this.port);
        int currentNode;

        while (queue.size() > 0) {
            currentNode = queue.poll();

            // don't process if processed already
            boolean done = false;
            for (int num : processed) {
                if (num == currentNode) {
                    done = true;
                }
            }
            if (done) {
                continue;
            }

            processed.add(currentNode);

            LinkedList<LinkState> currentEdges = getEdgesWithNode(currentNode, this.links);
            currentEdges.sort((link1, link2) -> link1.getCost() - link2.getCost());

            for (LinkState edge : currentEdges) {
                
                int dest = edge.getPorts()[0] == currentNode ? edge.getPorts()[1] : edge.getPorts()[0]; // destination node for this edge
                
                // make sure edge doesn't point to source
                if (dest == this.port) {
                    continue;
                }

                // add dest to queue if it hasn't been processed yet
                done = false;
                for (int num : processed) {
                    if (num == dest) {
                        done = true;
                    }
                }
                if (!done)
                {
                    queue.add(dest);
                }
                
                int currentCost = output.getCost(dest);
                int newCost = output.getCost(currentNode) + edge.getCost();

                if (newCost < currentCost) {
                    output.setCost(dest, newCost);
                    LinkedList<Integer> newPath = new LinkedList<>(output.getPath(currentNode));
                    newPath.add(dest);
                    output.setPath(dest, newPath);
                }
            }
        }
        
        return output;
    }

    //  helper method that returns edges with a node given the node and a list of edges
    public static LinkedList<LinkState> getEdgesWithNode(int node, LinkedList<LinkState> edges) {
        LinkedList<LinkState> output = new LinkedList<>();

        for (LinkState edge : edges) {
            int[] ports = edge.getPorts();
            if (node == ports[0] || node == ports[1]) {
                output.add(edge);
            }
        }

        return output;
    }

    public LinkedList<LinkState> getLinks() {
        return this.links;
    }


    public static void main(String[] args) {
        //test 
        LSTable table = new LSTable(1);
        table.addLink(new LinkState(1, 2, 1));
        table.addLink(new LinkState(1, 3, 50));
        table.addLink(new LinkState(2, 3, 2));
        table.addLink(new LinkState(2, 4, 8));
        table.addLink(new LinkState(3, 4, 5));
        
        System.out.println(table.djikstra());

    }

    @Override
    public String toString() {
        Date date = new Date();
        String output = "[" + date.getTime() + "] Node " + this.port + " Network Topology\n";
        for (LinkState link : this.links) {
            output += "- (" + link.getCost() + ") from Node " + link.getPorts()[0] + " to Node " + link.getPorts()[1] + "\n";
        }
        return output;
    }

}
