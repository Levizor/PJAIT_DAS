import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class Slave {
    int port;
    int number;
    public Slave(int port, int number){
        this.number = number;
        this.port = port;
    }

    public void run(){
        try (DatagramSocket socket = new DatagramSocket()){
            byte[] buf = String.valueOf(number).getBytes();
            DatagramPacket packet = new DatagramPacket(buf, 0, buf.length, InetAddress.getLocalHost(), port);
            socket.send(packet);
            System.out.printf("Data (%d) was sent on port %d%n", number, port);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch(IOException e){
            System.err.println("Catched an IO exception");
        }
    }

}
