import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Master {
    DatagramSocket socket;
    InetAddress broadcastAddress = null;

    private final AtomicBoolean running = new AtomicBoolean(false);

    ExecutorService executor = Executors.newCachedThreadPool();

    ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();

    public Master(DatagramSocket socket) {
        this.socket = socket;
    }

    public static InetAddress findBroadcastAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface networkInterface : Collections.list(interfaces)) {
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }

                for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = address.getBroadcast();
                    if (broadcast != null) {
                        return broadcast;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void run() {
        running.set(true);
        broadcastAddress = findBroadcastAddress();
        if (broadcastAddress == null) {
            System.err.println("Couldn't find broadcast address. Try connecting to the internet.");
            System.exit(1);
        }
        final DatagramPacket packet = new DatagramPacket(new byte[UDP.MAX_DATAGRAM_SIZE], UDP.MAX_DATAGRAM_SIZE);
        while (true) {
            try{
                socket.receive(packet);
                if(!running.get()) break;
                executor.execute(() -> handle(packet));
            } catch (SocketException | RejectedExecutionException e) {
                break;
            } catch (IOException e) {
                System.err.println("Got IOException");
            }
        }
    }

    public void broadcast(int number) {
        byte[] buf = String.valueOf(number).getBytes();
        if(socket.isClosed()) return;
        DatagramPacket packet = new DatagramPacket(buf, 0, buf.length, broadcastAddress, socket.getLocalPort());
        try {
            socket.send(packet);
        } catch (IOException e) {
        }
    }

    public void exit(int code) {
        if(!running.get()) return;
        running.set(false);
        executor.shutdownNow();
        socket.close();
        System.out.println("terminated");
        System.exit(code);
    }

    public void handle(DatagramPacket packet) {
        if (!running.get()) return;
        String s = new String(packet.getData(), 0, packet.getLength());
        int number;
        try {
            number = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            System.out.println("Bad number");
            return;
        }

        switch (number) {
            case 0 -> {
                int average = getAverage();
                System.out.println(average);
                broadcast(average);
            }
            case -1 -> {
                if(!running.get()) break;
                System.out.println(number);
                broadcast(number);
                exit(1);
            }
            default -> {
                System.out.println(number);
                addValue(number);
            }
        }
    }

    public void addValue(int number) {
        queue.offer(number);
    }

    public int getAverage() {
        int sum = queue.stream().mapToInt(i -> i).sum();
        return sum / queue.size();
    }
}
