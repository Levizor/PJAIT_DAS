import java.net.DatagramSocket;
import java.net.SocketException;

public class DAS {
    static int port;
    public static void main(String[] args) {
        if(args.length!=2){
            System.err.println("Usage: java Main <port> <number>");
            System.exit(1);
        }
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Couldn't parse port number: " + args[0]);
            System.exit(1);
        }

        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.printf("Running master on port %d%n", port);
            Master m = new Master(socket);

            if (args.length>1) {
                try{
                    int number = Integer.parseInt(args[1]);
                    m.addValue(number);
                }catch(NumberFormatException e){
                    System.err.println("Couldn't parse number: " + args[1]);
                    System.exit(1);
                }
            }else{
                System.out.println("No starting value provided. Starting with empty list.");
            }
            m.run();
        } catch (SocketException e) {
            System.out.println("The socket with this port is taken.\n" +
                    "Working in a slave mode.");
            if(args.length<2){
                System.err.println("Number not provided");
                System.exit(1);
            }

            int number = Integer.parseInt(args[1]);

            new Slave(port, number).run();
        }
    }
}
