import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;

public class ClientDiscovery {
    public static void discover(int port, ExecutorService execPool) {

        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buf = new byte[1400];

            DatagramPacket packetUDP = new DatagramPacket(buf, buf.length);

            while (CCS.flag) {
                socket.receive(packetUDP);

                execPool.submit(() -> {
                    try {

                        if (new String(packetUDP.getData(), 0,
                                packetUDP.getLength()).startsWith("CCS DISCOVER")) {

                            byte[] bufResponse = ("CCS FOUND").getBytes();
                            InetAddress receiverAddr = packetUDP.getAddress();
                            int receiverPort = packetUDP.getPort();

                            DatagramPacket packet = new DatagramPacket(
                                    bufResponse, bufResponse.length, receiverAddr, receiverPort);
                            socket.send(packet);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                Thread.sleep(5);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
