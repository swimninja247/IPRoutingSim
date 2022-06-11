import java.util.LinkedList;

public class routenode {
    
    public static void main(String[] args) {

        // Needs at least six args
        if (args.length < 6) {
            System.out.println("Usage: routenode dv <r/p> <update-interval> <local-port> <neighbor1-port> <cost-1> <neighbor2-port> <cost-2> ... [last] [<cost-change>]");
            System.exit(0);
        }

        if (args[0].equals("dv")) {
            int port = Integer.parseInt(args[3]);
            boolean last = false;
            boolean trigger = false;
            int triggerCost = 0;

            // Find number of neighbors in args
            int numNeighbors = 0;
            for (int i = 4; i < args.length; i += 2) {
                if (args[i].equals("last")) {
                    last = true;

                    // check if triggered cost change
                    if (i+1 < args.length) {
                        trigger = true;
                        triggerCost = Integer.parseInt(args[i+1]);
                    }
                    break;
                }
                numNeighbors++;
            }

            // Generate arrays of neighbors and distance vectors
            int[] neighbors = new int[numNeighbors];
            DistanceVector[] initialVectors = new DistanceVector[numNeighbors];
            for (int i = 0; i < numNeighbors; i++) {
                neighbors[i] = Integer.parseInt(args[i*2 + 4]);
                int cost = Integer.parseInt(args[i*2 + 5]);
                initialVectors[i] = new DistanceVector(neighbors[i], cost, neighbors[i]);
            }

            DVNode node = null;
            // Create node and initialize table
            if (args[1].equals("r")) {
                node = new DVNode(port, neighbors, initialVectors);
            } else if (args[1].equals("p")) {
                node = new DVNode(port, neighbors, initialVectors, true);
            } else {
                System.out.println("bruh");
                System.exit(1);
            }
            

            System.out.println(node.getTable());


            if (last) {
                node.broadcastTable();

                if (trigger) {
                    // loop for thirty seconds and trigger cost change
                    long startTime = System.currentTimeMillis();
                    long currentTime = System.currentTimeMillis();
                    while (currentTime - startTime < 30 * 1000) {
                        node.receive();
                        currentTime = System.currentTimeMillis();
                    }

                    // enter final loop
                    node.linkCostChange(triggerCost);
                    while (true) {
                        node.receive();
                    }
                }
            }

            // Start main loop
            while (true) {
                node.receive();
            }
        } else if (args[0].equals("ls")) {
            
            int port = Integer.parseInt(args[3]);
            boolean last = false;
            boolean trigger = false;
            int triggerCost = 0;

            // Find number of neighbors in args
            int numNeighbors = 0;
            for (int i = 4; i < args.length; i += 2) {
                if (args[i].equals("last")) {
                    last = true;

                    // check if triggered cost change
                    if (i+1 < args.length) {
                        trigger = true;
                        triggerCost = Integer.parseInt(args[i+1]);
                    }
                    break;
                }
                numNeighbors++;
            }

            // Generate arrays of neighbors and links
            int[] neighbors = new int[numNeighbors];
            LinkedList<LinkState> links = new LinkedList<>();
            for (int i = 0; i < numNeighbors; i++) {
                neighbors[i] = Integer.parseInt(args[i*2 + 4]);
                int cost = Integer.parseInt(args[i*2 + 5]);
                links.add(new LinkState(port, neighbors[i], cost));
            }

            // create link node object
            int UPDATE_INTERVAL = Integer.parseInt(args[2]) * 1000 + (int) (Math.random() * 1000);
            LSNode node = new LSNode(port, links, UPDATE_INTERVAL);

            // if last broadcast table
            if (last) {
                node.broadcastTable();

                if (trigger) {
                    // loop for thirty seconds and trigger cost change
                    long startTime = System.currentTimeMillis();
                    long currentTime = System.currentTimeMillis();
                    while (currentTime - startTime < 30 * 1000) {
                        node.receive();
                        currentTime = System.currentTimeMillis();
                    }

                    // enter final loop
                    node.linkCostChange(triggerCost);
                    while (true) {
                        node.receive();
                    }
                }
            }

            while (true) {
                node.receive();
            }

        }

    }

}
