import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

public class LSPathTable {

    int source;
    LinkedList<Integer> nodes;
    HashMap<Integer, LinkedList<Integer>> paths;
    HashMap<Integer, Integer> costs;

    public LSPathTable(int source) {
        this.source = source;
        this.paths = new HashMap<>();
        this.costs = new HashMap<>();
        this.nodes = new LinkedList<>();
        this.costs.put(source, 0);
        this.paths.put(source, new LinkedList<>());
    }

    public void initPath(int dest) {
        this.paths.put(dest, new LinkedList<>());
    }

    public void setCost(int dest, int cost) {
        this.costs.put(dest, cost);
    }

    public int getCost(int dest) {
        Integer cost = this.costs.get(dest);

        if (cost == null) {
            this.setCost(dest, Integer.MAX_VALUE);
            this.nodes.add(dest);
        }

        return this.costs.get(dest);
    }

    public LinkedList<Integer> getPath(int dest) {
        LinkedList<Integer> path = this.paths.get(dest);

        if (path == null) {
            this.initPath(dest);
        }

        return this.paths.get(dest);
    }

    public void setPath(int dest, LinkedList<Integer> path) {
        this.paths.put(dest, path);
    }

    @Override
    public String toString() {

        Date date = new Date();
        String output = "[" + date.getTime() + "] Node " + this.source + " Routing Table\n";

        // sort nodes
        Collections.sort(this.nodes);

        for (int node : this.nodes) {
            output += "- (" + this.costs.get(node) + ") -> Node " + node + "; ";
            
            // check if next hop
            if (this.getPath(node).size() > 1) {
                output += "Next hop -> Node " + this.getPath(node).get(0);
            }

            output += "\n";
        }

        return output;
    }
    
}
