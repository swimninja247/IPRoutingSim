import java.util.HashMap;

public class LSASeqTable {
    
    HashMap<Integer, Integer> table;

    public LSASeqTable() {
        this.table = new HashMap<>();
    }

    public void add(int port) {
        table.put(port, 0);
    }

    public void increment(int port) {
        table.put(port, table.get(port)+1);
    }

    public int get(int port) {
        if (table.get(port) == null) {
            this.add(port);
        }
        return this.table.get(port);
    }

    @Override
    public String toString() {
        return "LSASeqTable [table=" + table + "]";
    }

}
