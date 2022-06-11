import java.net.DatagramPacket;
import java.net.DatagramSocket;


public class Sender implements Runnable {

    private DatagramSocket sock;
    private DatagramPacket packet;
    
    public Sender(DatagramPacket p) {
        this.packet = p;
        try {
            this.sock = new DatagramSocket();
        } catch (Exception e) {
            System.out.println("Error occurred creating socket for Sender object.\n" + e);
        }
    }

    @Override
    public void run() {
        try {
            this.sock.send(this.packet);
        } catch (Exception e) {
            System.out.println("Error sending packet.\n" + e);
        }
    }
}
