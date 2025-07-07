import java.io.*;
import java.net.*;
import java.sql.SQLOutput;
import java.util.Random;

public class Client {
    public static void main(String[] args) {
        if (args.length != 1 || args[0].length() != 5 ||
                args[0].chars().anyMatch(e -> e < '0' || e > '9')) {
            throw new IllegalArgumentException("Port number must be a 5-digit number");
        }

        final int port = Integer.parseInt(args[0]);
        String serverIp = discoverService(port);

        if (serverIp != null) {
            communicateWithServer(serverIp, port);
        } else {
            System.out.println("Service discovery failed.");
        }
    }

    private static String discoverService(int port) {
        try (DatagramSocket udpSocket = new DatagramSocket()) {
            udpSocket.setBroadcast(true);

            String discoveryMessage = "CCS DISCOVER";
            byte[] buffer = discoveryMessage.getBytes();

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                    InetAddress.getByName("255.255.255.255"), port);

            System.out.println("Sending UDP broadcast for service discovery...");
            udpSocket.send(packet);

            byte[] responseBuffer = new byte[1400];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);

            udpSocket.setSoTimeout(5000); // Wait for up to 5 seconds for a response
            udpSocket.receive(responsePacket);

            String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
            if (response.equals("CCS FOUND")) {
                System.out.println("Service discovered at: " + responsePacket.getAddress().getHostAddress());
                return responsePacket.getAddress().getHostAddress();
            }
        } catch (IOException e) {
            System.err.println("Error during service discovery: " + e.getMessage());
        }
        return null;
    }

    private static void communicateWithServer(String serverIp, int port) {
        try (Socket tcpSocket = new Socket(serverIp, port);
             BufferedReader br = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(tcpSocket.getOutputStream()))) {

            System.out.println("Connected to server. Starting communication...");
            Random random = new Random();
            String[] operations = {"ADD", "SUB", "MUL", "DIV", "ad"};

            while (true) {
                String oper = operations[random.nextInt(operations.length)];
                int arg1 = random.nextInt(100);
                int arg2 = random.nextInt(100) ; // Avoid division by zero

                String request = String.format("%s %d %d", oper, arg1, arg2);
                System.out.println("Sending request: " + request);
                bw.write(request);
                bw.newLine();
                bw.flush();


                try {
                    System.out.println("Received response: " + Integer.parseInt(br.readLine()));
                } catch (NumberFormatException e) {
                    System.out.println("Received response: ERROR");
                }

                Thread.sleep(random.nextInt(3000) + 1000); // Wait 1-3 seconds between requests

                if(arg1 < 30 && arg1 > 20) {
                    System.out.println("DISCONNECTING");
                    tcpSocket.close();
                }
            }
        } catch (SocketException e) {
            System.out.println("Connection closed by server.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

